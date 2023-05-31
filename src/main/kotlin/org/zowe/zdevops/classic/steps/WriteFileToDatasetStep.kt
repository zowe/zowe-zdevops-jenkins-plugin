package org.zowe.zdevops.classic.steps

import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.bind.JavaScriptMethod
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.logic.WriteOperation.Companion.writeToDataset
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateFieldIsNotEmpty
import java.io.File

class WriteFileToDatasetStep
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    var fileOption: String?,
): AbstractBuildStep(connectionName) {

    private var localFilePath: String? = null
    private var workspacePath: String? = null

    @DataBoundSetter
    fun setLocalFilePath(localFilePath: String?) {
        this.localFilePath = localFilePath
    }
    @DataBoundSetter
    fun setWorkspacePath(workspacePath: String?) {
        this.workspacePath = workspacePath
    }
    fun getLocalFilePath(): String? {
        return this.localFilePath
    }
    fun getWorkspacePath(): String? {
        return this.workspacePath
    }

    override fun perform(
        build: AbstractBuild<*, *>,
        launcher: Launcher,
        listener: BuildListener,
        zosConnection: ZOSConnection
    ) {
        val workspace = build.executor?.currentWorkspace
        val file = when (fileOption) {
            DescriptorImpl().localFileOption -> File(localFilePath)
            DescriptorImpl().workspaceFileOption ->  {
                val fileWorkspacePath = workspace?.remote?.replace(workspace.name, "") + workspacePath
                File(fileWorkspacePath)
            }
            else        -> throw AbortException(Messages.zdevops_classic_write_options_invalid())
        }
        listener.logger.println(Messages.zdevops_declarative_writing_DS_from_file(dsn, file.name, zosConnection.host, zosConnection.zosmfPort))
        val fileContent = file.readText()
        writeToDataset(listener, zosConnection, dsn, fileContent)
    }


    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeFileToDatasetStep_display_name()) {

        private var lastStepId = 0
        private val marker: String = "WFTD"

        val chooseFileOption = "choose"
        val localFileOption = "local"
        val workspaceFileOption = "workspace"

        @JavaScriptMethod
        @Synchronized
        fun createStepId(): String {
            return marker + lastStepId++.toString()
        }

        fun doFillFileOptionItems(): ListBoxModel {
            val result = ListBoxModel()

            result.add(Messages.zdevops_classic_write_options_choose(), chooseFileOption)
            result.add(Messages.zdevops_classic_write_options_local(), localFileOption)
            result.add(Messages.zdevops_classic_write_options_workspace(), workspaceFileOption)

            return result
        }

        fun doCheckFileOption(@QueryParameter fileOption: String): FormValidation? {
            if (fileOption == chooseFileOption || fileOption.isEmpty()) return FormValidation.error(Messages.zdevops_classic_write_options_required())
            return FormValidation.ok()
        }

        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDatasetName(dsn)
        }

        fun doCheckLocalFilePath(@QueryParameter localFilePath: String,
                                 @QueryParameter fileOption:    String): FormValidation? {
            return if (fileOption == localFileOption) validateFieldIsNotEmpty(localFilePath)
            else FormValidation.ok()
        }

        fun doCheckWorkspacePath(@QueryParameter workspacePath: String,
                                 @QueryParameter fileOption:    String): FormValidation? {
            return if (fileOption == workspaceFileOption) validateFieldIsNotEmpty(workspacePath)
            else FormValidation.ok()
        }

    }
}