/*
 * Copyright (c) 2024 IBA Group.
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

package org.zowe.zdevops.classic.steps

import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.performMvsCommand
import org.zowe.zdevops.utils.validateFieldIsNotEmpty


/**
 * A Jenkins Pipeline step for performing a MVS command on a z/OS system via freestyle job.
 */
class PerformMvsCommandStep
/**
 * Data-bound constructor for the {@code PerformMvsCommandStep} step.
 *
 * @param connectionName The name of the z/OS connection to be used for executing the MVS command.
 * @param command The MVS command to be executed.
 */
@DataBoundConstructor
constructor(
    connectionName: String,
    val command: String,
) : AbstractBuildStep(connectionName) {

    /**
     * Performs the MVS command execution step within a Jenkins Pipeline build.
     *
     * @param build The current Jenkins build.
     * @param launcher The build launcher.
     * @param listener The build listener.
     * @param zosConnection The z/OS connection to execute the MVS command.
     */
    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        performMvsCommand(zosConnection, listener, command)
    }


    /**
     * Descriptor for the {@code PerformMvsCommandStep} step.
     *
     * This descriptor provides information about the step and makes it available for use
     * within Jenkins Pipelines.
     */
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_performMvsCommandStep_display_name()) {

        /**
         * Performs form validation for the 'command' parameter to ensure it is not empty.
         *
         * @param command The MVS command field value to validate.
         * @return A {@link FormValidation} object indicating whether the field is valid or contains an error.
         */
        fun doCheckCommand(@QueryParameter command: String): FormValidation? {
            return validateFieldIsNotEmpty(command)
        }

    }
}