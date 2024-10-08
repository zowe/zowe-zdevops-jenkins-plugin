/*
 * Copyright (c) 2023-2024 IBA Group.
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
import org.zowe.zdevops.logic.performTsoCommand

/**
 * Class that represents an action to perform a TSO command with a result in a declarative pipeline.
 * This class extends {@code AbstractZosmfActionWithResult} and is designed to execute a TSO command
 * via Zowe z/OSMF and return the command's output.
 *
 * @param acct the TSO account number.
 * @param command the TSO command to be executed.
 */
class PerformTsoCommandDeclarative
@DataBoundConstructor
constructor(
  val acct: String,
  val command: String,
) : AbstractZosmfActionWithResult() {

  override fun run(
    workspace: FilePath,
    listener: TaskListener,
    envVars: EnvVars,
    zoweZOSConnection: ZOSConnection
  ): String {
    return performTsoCommand(zoweZOSConnection, listener, acct, command)
      ?: throw AbortException("TSO command execution returned an empty result")
  }

  @Extension
  class DescriptorImpl : Companion.DefaultStepDescriptor(functionName = "performTsoCommandWithResult")
}
