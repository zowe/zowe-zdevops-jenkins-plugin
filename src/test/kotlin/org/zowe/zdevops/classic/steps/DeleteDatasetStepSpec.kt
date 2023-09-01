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
import org.zowe.zdevops.declarative.jobs.TestBuildListener
import org.zowe.zdevops.declarative.jobs.TestLauncher
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class DeleteDatasetStepSpec : ShouldSpec({
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
    context("classic/steps module: DeleteDatasetStep") {
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
        should("perform DeleteDatasetStep operation that deletes a dataset") {
            var isDeletingDataset = false
            var isSuccessfullyDeleted = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Deleting dataset")) {
                            isDeletingDataset = true
                        } else if (firstArg<String>().contains("Successfully deleted")) {
                            isSuccessfullyDeleted = true
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
                { it?.requestLine?.contains(Regex("DELETE /zosmf/restfiles/ds/.* HTTP/.*")) ?: false },
                { MockResponse().setBody("{}") }
            )

            val deleteDatasetStep = spyk(
                DeleteDatasetStep("test", "TEST.IJMP.DATASET1", member = null)
            )
            deleteDatasetStep.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )

            assertSoftly { isDeletingDataset shouldBe true }
            assertSoftly { isSuccessfullyDeleted shouldBe true }
        }
    }
})