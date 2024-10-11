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

package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosconsole.ConsoleResponse
import org.zowe.kotlinsdk.zowe.client.sdk.zosconsole.IssueCommand

/**
 * Executes an MVS command on a z/OS system using the provided z/OS connection.
 *
 * This function allows you to send an MVS command to a z/OS system, and capture the response
 *
 * @param zosConnection The z/OS connection through which the MVS command will be executed.
 * @param listener The Jenkins build listener for logging and monitoring the execution.
 * @param command The MVS command to be executed.
 * @return the command output.
 * @throws AbortException if the MVS command execution fails, with the error message indicating
 *                       the reason for the failure.
 */
fun performMvsCommand(
  zosConnection: ZOSConnection,
  listener: TaskListener,
  command: String,
): String? {
  listener.logger.println("[Perform MVS command] - Issuing command : $command")
  val commandResponseObj: ConsoleResponse
  try {
    commandResponseObj = IssueCommand(zosConnection).issueSimple(command)
    listener.logger.println(commandResponseObj.commandResponse)
  } catch (ex: Exception) {
    listener.logger.println("[Perform MVS command] - MVS command execution failed")
    throw ex
  }
  listener.logger.println("[Perform MVS command] - The command has been successfully executed")
  return commandResponseObj.commandResponse
}