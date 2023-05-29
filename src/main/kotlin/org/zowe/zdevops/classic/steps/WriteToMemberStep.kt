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
import org.zowe.zdevops.logic.WriteOperation.Companion.writeToMember
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateFieldIsNotEmpty
import org.zowe.zdevops.utils.validateMemberName

class WriteToMemberStep
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val member: String,
    val text: String
) : AbstractBuildStep(connectionName) {

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        writeToMember(listener, zosConnection, dsn, member, text)
    }

    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeToMemberStep_display_name()) {
        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDatasetName(dsn)
        }

        fun doCheckText(@QueryParameter text: String): FormValidation? {
            return validateFieldIsNotEmpty(text)
        }

        fun doCheckMember(@QueryParameter member: String): FormValidation? {
            return validateMemberName(member) ?: validateFieldIsNotEmpty(member)
        }
    }
}