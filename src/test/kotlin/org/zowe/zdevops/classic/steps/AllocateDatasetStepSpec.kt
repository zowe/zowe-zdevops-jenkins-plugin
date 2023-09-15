/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.classic.steps

import hudson.model.Item
import hudson.util.FormValidation
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
import org.zowe.zdevops.Messages
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

    val descriptor = AllocateDatasetStep.DescriptorImpl()
    context("classic/steps module: AllocateDatasetStepDescriptor") {
        should("validate primary allocation size") {
            descriptor.doCheckPrimary("") shouldBe FormValidation.ok()
            descriptor.doCheckPrimary("100") shouldBe FormValidation.ok()
            descriptor.doCheckPrimary("0") shouldBe FormValidation.error(Messages.zdevops_classic_allocateDatasetStep_primary_is_zero_validation())
            descriptor.doCheckPrimary("abc") shouldBe FormValidation.error(Messages.zdevops_value_is_not_number_validation())
        }

        should("validate secondary allocation size") {
            descriptor.doCheckSecondary("") shouldBe FormValidation.ok()
            descriptor.doCheckSecondary("200") shouldBe FormValidation.ok()
            descriptor.doCheckSecondary("xyz") shouldBe FormValidation.error(Messages.zdevops_value_is_not_number_validation())
        }

        should("validate block size") {
            descriptor.doCheckBlkSize("", "") shouldBe FormValidation.ok()
            descriptor.doCheckBlkSize("80", "240") shouldBe FormValidation.ok()
            descriptor.doCheckBlkSize("240","80") shouldBe FormValidation.warning(Messages.zdevops_classic_allocateDatasetStep_blksize_smaller_than_lrecl_validation())
            descriptor.doCheckBlkSize("80","abc") shouldBe FormValidation.warning(Messages.zdevops_value_is_not_number_validation())
            descriptor.doCheckBlkSize("80","200") shouldBe FormValidation.warning(Messages.zdevops_classic_allocateDatasetStep_blksize_validation_warning())
        }

        should("validate dataset name") {
            descriptor.doCheckDsn("") shouldBe FormValidation.error(Messages.zdevops_value_must_not_be_empty_validation())
            descriptor.doCheckDsn("MY_DATASET") shouldBe FormValidation.error(Messages.zdevops_dataset_name_is_invalid_validation())
        }

        should("convert string and validate positive integer") {
            descriptor.convertStringAndValidateIntPositive("") shouldBe FormValidation.ok()
            descriptor.convertStringAndValidateIntPositive("100") shouldBe FormValidation.ok()
            descriptor.convertStringAndValidateIntPositive("0") shouldBe FormValidation.ok()
            descriptor.convertStringAndValidateIntPositive("-10") shouldBe FormValidation.error(Messages.zdevops_value_must_be_positive_number_validation())
            descriptor.convertStringAndValidateIntPositive("abc") shouldBe FormValidation.error(Messages.zdevops_value_is_not_number_validation())
        }
    }
})