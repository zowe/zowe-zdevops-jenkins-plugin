package org.zowe.zdevops.classic.steps

import hudson.FilePath
import hudson.model.Executor
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

class WriteFileToDatasetStepSpec : ShouldSpec({
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
    context("classic/steps module: WriteFileToDatasetStep") {
        val virtualChannel = TestVirtualChannel()
        val zosConnection = ZOSConnection(mockServer.hostName, mockServer.port.toString(), "test", "test", "https")
        val rootDir = Paths.get("").toAbsolutePath().toString()
        val trashDir = Paths.get(rootDir, "src", "test", "resources", "trash").toString()
        val itemGroup = object : TestItemGroup() {
            override fun getRootDirFor(child: Item?): File {
                return File(trashDir)
            }
        }
        val project = TestProject(itemGroup, "test")
        val build = object:TestBuild(project) {
            override fun getExecutor(): Executor {
                val mockInstance = mockk<Executor>()
                val mockDir = Paths.get(rootDir, "src", "test", "resources", "mock", "test_file.txt").toString()
                every { mockInstance.currentWorkspace } returns FilePath(virtualChannel, mockDir)
                return mockInstance
            }
        }

        afterEach {
            responseDispatcher.removeAllEndpoints()
        }
        should("perform WriteFileToDatasetStep operation to write a file to a dataset") {
            var isWritingToDataset = false
            var isWritten = false
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
                        } else {
                            fail("Unexpected logger message: ${firstArg<String>()}")
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

            val writeFileToDatasetDecl = spyk(
                WriteFileToDatasetStep("test", "TEST.IJMP.DATASET1", "workspace")
            )
            writeFileToDatasetDecl.setWorkspacePath("test_file.txt")
            writeFileToDatasetDecl.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )
            assertSoftly { isWritingToDataset shouldBe true }
            assertSoftly { isWritten shouldBe true }
        }
    }
})