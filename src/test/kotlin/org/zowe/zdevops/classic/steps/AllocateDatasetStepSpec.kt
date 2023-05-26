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
import org.zowe.kotlinsdk.DatasetOrganization
import org.zowe.kotlinsdk.RecordFormat
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.MOCK_SERVER_HOST
import org.zowe.zdevops.MockResponseDispatcher
import org.zowe.zdevops.MockServerFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Paths

class AllocateDatasetStepSpec : ShouldSpec({
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
    context("classic/steps module: AllocateDatasetStep") {
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
        should("perform AllocateDatasetStep operation") {
            var isDatasetAllocating = false
            var isDatasetAllocated = false
            val taskListener = object : TestBuildListener() {
                override fun getLogger(): PrintStream {
                    val logger = mockk<PrintStream>()
                    every {
                        logger.println(any<String>())
                    } answers {
                        if (firstArg<String>().contains("Allocating dataset")) {
                            isDatasetAllocating = true
                        } else if (firstArg<String>().contains("has been allocated successfully")) {
                            isDatasetAllocated = true
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
                { it?.requestLine?.contains("/zosmf/restfiles/ds/") ?: false },
                { MockResponse().setBody("{}") }
            )

            val allocateDatasetStepInst = spyk(
                AllocateDatasetStep(
                    "test",
                    "TEST.IJMP.DATASET1",
                    DatasetOrganization.PS,
                    1,
                    0,
                    RecordFormat.F
                )
            )
            allocateDatasetStepInst.perform(
                build,
                launcher,
                taskListener,
                zosConnection
            )
            assertSoftly { isDatasetAllocating shouldBe true }
            assertSoftly { isDatasetAllocated shouldBe true }
        }
    }
})