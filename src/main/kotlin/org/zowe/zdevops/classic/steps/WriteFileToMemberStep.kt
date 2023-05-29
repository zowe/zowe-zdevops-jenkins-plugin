package org.zowe.zdevops.classic.steps

import hudson.AbortException
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import org.kohsuke.stapler.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages
import org.zowe.zdevops.classic.AbstractBuildStep
import org.zowe.zdevops.utils.*
import java.io.File
import java.util.*


class WriteFileToMemberStep
@DataBoundConstructor
constructor(
    connectionName: String,
    val dsn: String,
    val member: String,
    val fileOption: String,
): AbstractBuildStep(connectionName) {

    private var fileContent: String? = null
    private var workspacePath: String? = null

    @DataBoundSetter
    fun setFileContent(fileContent: String?) {
        this.fileContent = fileContent
        this.workspacePath = null
    }
    @DataBoundSetter
    fun setWorkspacePath(workspacePath: String?) {
        this.workspacePath = workspacePath
        this.fileContent = null
    }
    fun getFileContent(): String? {
        return this.fileContent
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
        val fileNameForLog = if (workspacePath == null) "" else workspacePath
        listener.logger.println(Messages.zdevops_declarative_writing_DS_from_file(dsn, fileNameForLog, zosConnection.host, zosConnection.zosmfPort))

        val currentWorkspace: FilePath? = build.getExecutor()?.currentWorkspace
        val currentWorkspacePath = currentWorkspace?.remote?.replace(currentWorkspace.name, "")
        val randomName = UUID.randomUUID().toString().replace("-", "")
        val textFile: File =  if (fileOption == "path") {
            File("$currentWorkspacePath$workspacePath")
        } else {
            createFileAndWriteContent("$currentWorkspacePath/$randomName", fileContent!!)
        }

        val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
        val targetDSLRECL =
            targetDS.recordLength ?: throw AbortException(Messages.zdevops_declarative_writing_DS_no_info(dsn))
        val ineligibleStrings = textFile
            .readLines()
            .map { it.length }
            .fold(0) { result, currStrLength -> if (currStrLength > targetDSLRECL) result + 1 else result }
        if (ineligibleStrings > 0) {
            throw AbortException(Messages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings, dsn))
        }
        val textString = textFile.readText().replace("\r", "")
        runMFTryCatchWrappedQuery(listener) {
            ZosDsn(zosConnection).writeDsn(dsn, member, textString.toByteArray())
        }

        if (fileOption == "file") {
            deleteFile("$currentWorkspacePath/$randomName", listener.logger)
        }

        listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))

    }


    @Extension
    class DescriptorImpl :
        Companion.DefaultBuildDescriptor(Messages.zdevops_classic_writeFileToMemberStep_display_name()) {

        override fun save() {
            super.save()
        }
        fun doFillFileOptionItems(): ListBoxModel {
            val result = ListBoxModel()

            result.add("Choose file option", "")
            result.add("Choose Local File", "file")
            result.add("Specify Workspace Path", "path")

            return result
        }

        fun doCheckFileOption(@QueryParameter fileOption: String): FormValidation? {
            if (fileOption.isEmpty()) return FormValidation.error("Select field is required")
            return FormValidation.ok()
        }

        fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
            return validateDatasetName(dsn)
        }

        fun doCheckMember(@QueryParameter member: String): FormValidation? {
            return validateMemberName(member)?: validateFieldIsNotEmpty(member)
        }

        fun doCheckFile(@QueryParameter file: String): FormValidation? {
            return validateFieldIsNotEmpty(file)
        }

    }
}