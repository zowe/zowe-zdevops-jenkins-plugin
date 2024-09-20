/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2024
 */

package org.zowe.zdevops.classic.steps

import hudson.EnvVars
import hudson.FilePath
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import org.zowe.zdevops.declarative.jobs.PerformTsoCommandDeclarative
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class PerformTsoCommandDeclarativeSpec : ShouldSpec({
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
  context("declarative/jobs module: PerformTsoCommandStep") {
    val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
    val trashDir = tempdir()
    val trashDirWithInternal = Paths.get(trashDir.absolutePath, "test_name").toString()
    val workspace = FilePath(File(trashDirWithInternal))
    val env = EnvVars()

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }
    should("perform PerformTsoCommandDeclarative operation and return its result") {
      var isPreExecuteStage = false
      var isCommandExecuted = false
      val expectedTsoTimeCommandOutput = "IKJ56650I TIME-02:20:29 PM. CPU-00:00:00 SERVICE-448 SESSION-00:00:39 OCTOBER 4,2023"
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Issuing command")) {
              isPreExecuteStage = true
            } else if (firstArg<String>().contains("The command has been successfully executed")) {
              isCommandExecuted = true
            }
          }
          return logger
        }
      }


      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("POST /zosmf/tsoApp/tso") ?: false },
        { MockResponse()
          .setResponseCode(200)
          .setBody(responseDispatcher.readMockJson("startTsoResponse") ?: "") }
      )
      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("GET /zosmf/tsoApp/tso/") ?: false },
        { MockResponse()
          .setResponseCode(200)
          .setBody(responseDispatcher.readMockJson("getTsoResponse") ?: "") }
      )
      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("PUT /zosmf/tsoApp/tso/") ?: false },
        { MockResponse()
          .setResponseCode(200)
          .setBody(responseDispatcher.readMockJson("sendTsoResponse") ?: "") }
      )
      responseDispatcher.injectEndpoint(
        this.testCase.name.testName,
        { it?.requestLine?.contains("DELETE /zosmf/tsoApp/tso/") ?: false },
        { MockResponse()
          .setResponseCode(200)
          .setBody(responseDispatcher.readMockJson("endTsoResponse") ?: "") }
      )

      val performTsoCommandInst = spyk(
        PerformTsoCommandDeclarative(
          "test",
          "TIME",
        )
      )
      val tsoCommandResult = performTsoCommandInst.run(workspace, taskListener, env, zosConnection)

      assertSoftly { tsoCommandResult shouldContain expectedTsoTimeCommandOutput }
      assertSoftly { isPreExecuteStage shouldBe true }
      assertSoftly { isCommandExecuted shouldBe true }
    }
  }
})