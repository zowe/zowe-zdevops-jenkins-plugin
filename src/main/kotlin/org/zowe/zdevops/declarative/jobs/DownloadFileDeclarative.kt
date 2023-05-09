/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

package org.zowe.zdevops.declarative.jobs

import org.zowe.kotlinsdk.DatasetOrganization
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnDownload
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.DownloadParams
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import org.zowe.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.model.Run
import hudson.model.TaskListener
import org.apache.commons.io.IOUtils
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import java.io.File
import java.io.StringWriter

class DownloadFileDeclarative @DataBoundConstructor constructor(private val dsn: String) :
    AbstractZosmfAction() {

    private var vol: String? = null
    private var getETag: Boolean? = null

    @DataBoundSetter
    fun setVol(vol: String) { this.vol = vol }

    @DataBoundSetter
    fun setGetETag(getETag: Boolean) { this.getETag = getETag }

    override val exceptionMessage: String = zMessages.zdevops_declarative_DSN_downloaded_fail(dsn)

    fun downloadDS(dsn: String,
                   zosConnection: ZOSConnection,
                   workspace: FilePath,
                   listener: TaskListener) {
        val downloadedDSN = ZosDsnDownload(zosConnection).downloadDsn(dsn, DownloadParams(dsn,getETag,vol))

        val writer = StringWriter()
        IOUtils.copy(downloadedDSN, writer, "UTF-8")
        val workspacePath = workspace.remote.replace(workspace.name,"")
        val file = File("$workspacePath$dsn")
        file.writeText(writer.toString())
        listener.logger.println(zMessages.zdevops_declarative_DSN_downloaded_success(dsn))
    }

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        listener.logger.println(zMessages.zdevops_declarative_DSN_downloading(dsn, vol, zosConnection.host, zosConnection.zosmfPort))

        if (dsn.contains(Regex("[\\w#\$@.-]{1,}\\([\\w#\$@]{1,8}\\)"))) { //means it's a PDS member
            downloadDS(dsn,zosConnection,workspace,listener)
        } else {
            when (ZosDsnList(zosConnection).listDsn(dsn, ListParams(vol)).items.first().datasetOrganization) {
                DatasetOrganization.PS -> downloadDS(dsn,zosConnection,workspace,listener)
                DatasetOrganization.PO -> {
                    listener.logger.println(zMessages.zdevops_declarative_DSN_downloading_members(dsn))
                    ZosDsnList(zosConnection).listDsnMembers(dsn,ListParams(vol)).items.forEach {
                        downloadDS("${dsn}(${it.name})",zosConnection,workspace,listener)
                    }
                }
                else -> listener.logger.println(zMessages.zdevops_declarative_DSN_downloading_invalid_dsorg())
            }
        }
    }


    @Symbol("downloadDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Download File Declarative")
}
