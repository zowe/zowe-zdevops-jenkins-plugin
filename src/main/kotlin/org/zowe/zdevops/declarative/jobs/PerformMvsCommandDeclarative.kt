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

package org.zowe.zdevops.declarative.jobs

import hudson.AbortException
import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.model.TaskListener
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.AbstractZosmfActionWithResult
import org.zowe.zdevops.logic.performMvsCommand

/**
 * Class that represents an action to perform an MVS command with a result in a declarative pipeline.
 * This class extends {@code AbstractZosmfActionWithResult} and is designed to execute an MVS command
 * via Zowe z/OSMF and return the command's output.
 *
 * @param command the MVS command to be executed.
 */
class PerformMvsCommandDeclarative
@DataBoundConstructor
constructor(
  val command: String,
) : AbstractZosmfActionWithResult() {

  override fun run(
    workspace: FilePath,
    listener: TaskListener,
    envVars: EnvVars,
    zoweZOSConnection: ZOSConnection
  ): String {
    return performMvsCommand(zoweZOSConnection, listener, command)
      ?: throw AbortException("MVS command execution returned an empty result")
  }

  @Extension
  class DescriptorImpl : Companion.DefaultStepDescriptor(functionName = "performMvsCommand")
}
