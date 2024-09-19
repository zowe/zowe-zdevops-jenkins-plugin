/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2024
 */

package org.zowe.zdevops.declarative.jobs

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.model.TaskListener
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfActionWithResult
import org.zowe.zdevops.logic.submitJobSync

/**
 * Class that represents an action to submit a z/OS job and retrieve the return code in a declarative pipeline.
 * This class extends {@code AbstractZosmfActionWithResult} and is designed to submit a job via Zowe z/OSMF
 * and return the job's return code.
 *
 * @param fileToSubmit the path to the file containing the JCL to be submitted.
 */
class SubmitJobStepWithResultDeclarative
@DataBoundConstructor
constructor(val fileToSubmit: String)
  : AbstractZosmfActionWithResult() {

  override fun run(
    workspace: FilePath,
    listener: TaskListener,
    envVars: EnvVars,
    zoweZOSConnection: ZOSConnection
  ): String {
      val workspacePath = FilePath(null, workspace.remote.replace(workspace.name,""))
      val linkBuilder: (String?, String, String) -> String = { buildUrl, jobName, jobId ->
        "$buildUrl/execution/node/3/ws/${jobName}.${jobId}/*view*/"
      }
      return submitJobSync(fileToSubmit, zoweZOSConnection,
        listener, workspacePath, envVars["BUILD_URL"], linkBuilder)
  }

  @Extension
  class DescriptorImpl : Companion.DefaultStepDescriptor(functionName = "submitJobSyncWithResult")
}
