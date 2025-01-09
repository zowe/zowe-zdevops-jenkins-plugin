/*
 * Copyright (c) 2025 IBA Group.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBA Group
 *   Zowe Community
 */
package org.zowe.zdevops.declarative.files.dsn

import hudson.AbortException
import hudson.EnvVars
import hudson.FilePath
import hudson.model.Item
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
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
import org.zowe.zdevops.declarative.*
import org.zowe.zdevops.logic.validatePathExists
import org.zowe.zdevops.logic.validatePathLeadsToDirectory
import org.zowe.zdevops.logic.writeFileToDataset
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths


class WriteDirToDatasetDeclarativeSpec : ShouldSpec({
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
  context("declarative/files/dsn module: WriteDirToDatasetDeclarativeSpec") {
    val virtualChannel = TestVirtualChannel()
    val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
    val rootDir = Paths.get("").toAbsolutePath().toString()
    val trashDir = tempdir()
    val itemGroup = object : TestItemGroup() {
      override fun getRootDirFor(child: Item?): File {
        return trashDir
      }
    }
    val job = TestJob(itemGroup, "test")
    val run = TestRun(job)
    val mockDir = Paths.get(rootDir, "src", "test", "resources", "mock", "here").toString()
    val workspace = FilePath(File(mockDir))
    val env = EnvVars()

    afterEach {
      responseDispatcher.removeAllEndpoints()
    }

    should("should throw exception if directory path does not exist") {
      val nonExistentPath = File("non-existent")

      shouldThrow<AbortException> {
        validatePathExists(nonExistentPath)
      }
    }

    should("should not throw exception as directory path exists") {
      val actualPath = File(".")

      shouldNotThrow<AbortException> {
        validatePathExists(actualPath)
      }
    }

    should("should throw exception if path is not a directory") {
      val file = File.createTempFile("temp", null)

      shouldThrow<AbortException> {
        validatePathLeadsToDirectory(file)
      }
      file.delete()
    }

    should("should throw exception if file contains lines exceeding LRECL") {
      val tempFile = File.createTempFile("temp", null).apply {
        writeText("This is a very long line exceeding the LRECL limit.")
      }

      try {
        val exception = shouldThrow<AbortException> {
          writeFileToDataset(tempFile, "TEST.DATASET", 10, zosConnection)
        }
        exception.message shouldBe "Error: File '${tempFile.name}' contains lines exceeding record length '10' of dataset 'TEST.DATASET'. Exceeding line numbers: [1]"
      } finally {
        tempFile.delete()
      }
    }

    should("perform WriteDirToDatasetDeclarativeSpec operation to write directory to a dataset") {
      var isWritingToDataset = false
      var isWritten = false
      val file1Name = "file1.txt"
      val file2Name = "file2.txt"
      var isFile1Processed = false
      var isFile2Processed = false
      val taskListener = object : TestBuildListener() {
        override fun getLogger(): PrintStream {
          val logger = mockk<PrintStream>()
          every {
            logger.println(any<String>())
          } answers {
            if (firstArg<String>().contains("Writing to dataset")) {
              isWritingToDataset = true
            } else if (firstArg<String>().contains("Data has been written to dataset")) {
              isWritten = true
            } else if (firstArg<String>().contains("Processing entry: '$file1Name'")) {
              isFile1Processed = true
            } else if (firstArg<String>().contains("Processing entry: '$file2Name'")) {
              isFile2Processed = true
          }
          }
          return logger
        }
      }
      val launcher = TestLauncher(taskListener, virtualChannel)

      responseDispatcher.injectEndpoint(
        "${this.testCase.name.testName}_listDataSets",
        { it?.requestLine?.contains("zosmf/restfiles/ds") ?: false },
        { MockResponse().setBody(responseDispatcher.readMockJson("listDataSets") ?: "") }
      )

      val tempDir = tempdir()
      File(tempDir, file1Name).apply { writeText("content1") }
      File(tempDir, file2Name).apply { writeText("content2") }

      val writeDirToDSDecl = spyk(
        WriteDirToDatasetDeclarative("TEST.IJMP.DATASET1", tempDir.path, true)
      )
      writeDirToDSDecl.perform(
        run,
        workspace,
        env,
        launcher,
        taskListener,
        zosConnection
      )

      assertSoftly { isWritingToDataset shouldBe true }
      assertSoftly { isFile1Processed shouldBe true }
      assertSoftly { isFile2Processed shouldBe true }
      assertSoftly { isWritten shouldBe true }
    }
  }
})