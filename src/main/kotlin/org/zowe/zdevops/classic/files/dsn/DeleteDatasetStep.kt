/*
 * Copyright (c) 2022-2024 IBA Group.
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

package org.zowe.zdevops.classic.files.dsn

import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.core.AbstractBuildStep
import org.zowe.zdevops.logic.deleteDatasetOrMember
import org.zowe.zdevops.utils.validateDsnOrDsnMemberName

class DeleteDatasetStep
/**
 * Constructs a new instance of the DeleteDatasetStep.
 *
 * @param connectionName The name of the z/OS connection to use for deleting the dataset.
 * @param dsn            The name of the dataset to delete in the form ZOSMFAD.TEST(MEMNAME).
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val failOnNotExist: Boolean = false ,
) : AbstractBuildStep(connectionName) {

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        deleteDatasetOrMember(dsn, zosConnection, listener, failOnNotExist)
    }

    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_deleteDatasetStep_display_name()) {

        /**
         * Checks if the dataset name or dataset member name is valid
         *
         * @param dsn The dataset name or dataset member name
         * @return FormValidation.ok() if the dataset name is valid, or an error message otherwise
         */
        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDsnOrDsnMemberName(dsn)
        }

    }
}