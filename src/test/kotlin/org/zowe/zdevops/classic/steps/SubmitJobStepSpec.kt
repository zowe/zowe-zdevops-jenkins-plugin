/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.classic.steps

import hudson.model.Item
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.fail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class SubmitJobStepSpec : ShouldSpec({
  lateinit var mockServer: MockWebServer
  lateinit var responseDispatcher: MockResponseDispatcher
  val mockServerFactory = MockServerFactory()

  beforeSpec {
    mockServer = mockServerFactory.startMockServer(MOCK_SERVER_HOST)
    responseDispatcher = mockServerFactory.responseDispatcher
  }
  afterSpec {
    mockServerFactory.stopMockServer()
  }
  context("classic/steps module: SubmitJobStep") {
    val rootDir = Paths.get("").toAbsolutePath().toString()
    val trashDir = Paths.get(rootDir, "src", "test", "resources", "trash").toString()
    val itemGroup = object : TestItemGroup() {
      override fun getRootDirFor(child: Item?): File {
        return File(trashDir)
      }
    }
    val project = TestProject(itemGroup, "test")
    val build = TestBuild(project)
    val virtualChannel = TestVirtualChannel()
    val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }
    should("perform SubmitJobStep operation") {
      var isJobSubmitting = false
      var isJobSubmitted = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Submitting a JOB")) {
              isJobSubmitting = true
            } else if (firstArg<String>().contains("JOB submitted successfully")) {
              isJobSubmitted = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("zosmf/restjobs/jobs") ?: false },
        { MockResponse().setBody(responseDispatcher.readMockJson("submitJobResponse") ?: "") }
      )

      val submitJobStepInst = spyk(
        SubmitJobStep(
          "test",
          "test"
        )
      )
      submitJobStepInst.perform(
        build,
        launcher,
        taskListener,
        zosConnection
      )
      assertSoftly { isJobSubmitting shouldBe true }
      assertSoftly { isJobSubmitted shouldBe true }
    }
    should("fail SubmitJobStep operation") {
      var isJobSubmitting = false
      var isJobFailLogged = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Submitting a JOB")) {
              isJobSubmitting = true
            } else if (firstArg<String>().contains("HTTP status code")) {
              isJobFailLogged = true
            } else {
              fail("Unexpected logger message: ${firstArg<String>()}")
            }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("zosmf/restjobs/jobs") ?: false },
        { MockResponse()
          .setResponseCode(500)
          .setBody(responseDispatcher.readMockJson("submitJobErrorResponse") ?: "") }
      )

      val submitJobStepInst = spyk(
        SubmitJobStep(
          "test",
          "test"
        )
      )
      runCatching {
        submitJobStepInst.perform(
          build,
          launcher,
          taskListener,
          zosConnection
        )
      }
        .onSuccess {
          fail("The 'perform' operation will fail")
        }
        .onFailure {
          assertSoftly { isJobSubmitting shouldBe true }
          assertSoftly { isJobFailLogged shouldBe true }
        }
    }
  }
})
