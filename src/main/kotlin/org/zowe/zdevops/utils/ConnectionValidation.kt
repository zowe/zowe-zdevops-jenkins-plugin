/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2024
 */

package org.zowe.zdevops.utils

import hudson.AbortException
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import org.zowe.zdevops.Messages

/**
 * Gets a list of datasets
 * Calls the listDsn function of ZosDsnList to list data set names.
 * Passes a test data set name ('HELLO.THERE').
 *
 * @param zosConnection The ZOSConnection object representing the connection to the z/OS system.
 */
fun getTestDatasetList(zosConnection: ZOSConnection) {
  ZosDsnList(zosConnection).listDsn(Messages.zdevops_config_ZOSConnection_validation_testDS(), ListParams())
}

/**
 * Validates a z/OS connection.
 *
 * @param zosConnection The ZOSConnection object representing the connection to the z/OS system.
 */
fun validateConnection(zosConnection: ZOSConnection) {
  try {
    getTestDatasetList(zosConnection)
  } catch (connectException: Exception) {
    val connExMessage = "Failed to connect to z/OS (${zosConnection.user}@${zosConnection.host}:${zosConnection.zosmfPort}): ${connectException.message}"
    throw AbortException(connExMessage)
  }
}