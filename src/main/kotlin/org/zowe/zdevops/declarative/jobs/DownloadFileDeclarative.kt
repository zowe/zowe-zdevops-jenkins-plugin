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

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfAction
import org.zowe.zdevops.logic.downloadDSOrDSMemberByType

class DownloadFileDeclarative @DataBoundConstructor constructor(val dsn: String) :
    AbstractZosmfAction() {

    private var vol: String? = null
    private var returnEtag: Boolean? = null

    @DataBoundSetter
    fun setVol(vol: String) { this.vol = vol }

    @DataBoundSetter
    fun setReturnEtag(returnEtag: Boolean) { this.returnEtag = returnEtag }

    fun getVol(): String? = this.vol

    fun getReturnEtag(): Boolean? = this.returnEtag

    override val exceptionMessage: String = zMessages.zdevops_declarative_DSN_downloaded_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        val workspacePath = FilePath(null, workspace.remote.replace(workspace.name,""))
        val jenkinsJobUrl = env["BUILD_URL"] + "/execution/node/3/"
        downloadDSOrDSMemberByType(dsn, vol, returnEtag, listener, zosConnection, workspacePath, jenkinsJobUrl)
    }


    @Symbol("downloadDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Download File Declarative")
}
