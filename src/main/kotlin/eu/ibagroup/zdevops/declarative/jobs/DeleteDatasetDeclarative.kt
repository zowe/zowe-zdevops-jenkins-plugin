package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsn
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsnList
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.input.ListParams
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import eu.ibagroup.zdevops.utils.runMFTryCatchWrappedQuery
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter


class DeleteDatasetDeclarative @DataBoundConstructor constructor(
) : AbstractZosmfAction() {

    private var dsn: String = ""
    private var member: String = ""
    private var mask: String = ""

    @DataBoundSetter
    fun setDsn(dsn: String) { this.dsn = dsn }

    @DataBoundSetter
    fun setMember(member: String) { this.member = member }

    @DataBoundSetter
    fun setMask(mask: String) { this.mask = mask }

    override val exceptionMessage: String = zMessages.zdevops_declarative_deleting_ds_fail()
    private val successMessage: String = zMessages.zdevops_declarative_deleting_ds_success()

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {

        if (dsn.isNotEmpty() && mask.isNotEmpty()) {
            throw AbortException(zMessages.zdevops_declarative_deleting_ds_fail_both_params())
        }
        if (dsn.isEmpty() && mask.isEmpty()) {
            throw AbortException(zMessages.zdevops_declarative_deleting_ds_fail_none_params())
        }
        if (dsn.isNotEmpty()) {
            val memberNotEmpty = member.isNotEmpty()
            val logMessage = if (memberNotEmpty) zMessages.zdevops_declarative_deleting_ds_member(member, dsn, zosConnection.host, zosConnection.zosmfPort)
                             else zMessages.zdevops_declarative_deleting_ds(dsn, zosConnection.host, zosConnection.zosmfPort)
            listener.logger.println(logMessage)
            runMFTryCatchWrappedQuery(listener) {
                val response = if (memberNotEmpty) ZosDsn(zosConnection).deleteDsn(dsn, member)
                               else ZosDsn(zosConnection).deleteDsn(dsn)
            }
            listener.logger.println(successMessage)
            return
        }
        if (mask.isNotEmpty()) {
            listener.logger.println(zMessages.zdevops_declarative_deleting_ds_by_mask(mask))
            val dsnList = ZosDsnList(zosConnection).listDsn(mask, ListParams())
            if (dsnList.items.isEmpty()) {
                throw AbortException(zMessages.zdevops_declarative_deleting_ds_fail_no_matching_mask())
            }
            dsnList.items.forEach {
                runMFTryCatchWrappedQuery(listener) {
                    listener.logger.println(zMessages.zdevops_declarative_deleting_ds(it.name, zosConnection.host, zosConnection.zosmfPort))
                    ZosDsn(zosConnection).deleteDsn(it.name)
                }
            }
            listener.logger.println(successMessage)
            return
        }
    }


    @Symbol("deleteDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Delete Dataset or Dataset member Declarative")

}

