/*
 * Copyright (c) 2022-2025 IBA Group.
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

package org.zowe.zdevops.declarative.files.dsn

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.core.AbstractZosmfAction
import org.zowe.zdevops.declarative.jobs.zMessages
import org.zowe.zdevops.logic.writeTextToDatasetJenkins

class WriteToDatasetDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                  private val text: String) :
    AbstractZosmfAction() {

    override val exceptionMessage: String = zMessages.zdevops_declarative_writing_DS_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        writeTextToDatasetJenkins(listener, zosConnection, dsn, text)
    }


    @Symbol("writeToDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write to Dataset Declarative")
}
