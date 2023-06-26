/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery

class WriteOperation {
    companion object {

        private fun validateTextForDataset(
            listener: TaskListener,
            zosConnection: ZOSConnection,
            dsn: String,
            text: String,
            ) {
            if(text == "") {
                listener.logger.println(Messages.zdevops_declarative_writing_skip())
                return
            }
            listener.logger.println(Messages.zdevops_declarative_writing_DS_from_input(dsn, zosConnection.host, zosConnection.zosmfPort))

            val stringList = text.split('\n')
            val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
            if (targetDS.recordLength == null) {
                throw AbortException(Messages.zdevops_declarative_writing_DS_no_info(dsn))
            }
            var ineligibleStrings = 0
            stringList.forEach {
                if (it.length > targetDS.recordLength!!) {
                    ineligibleStrings++
                }
            }
            if (ineligibleStrings > 0) {
                throw AbortException(Messages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings,dsn))
            }
        }

        fun writeToDataset(listener: TaskListener,
                           zosConnection: ZOSConnection,
                           dsn: String,
                           text: String,
                           ) {
            validateTextForDataset(listener, zosConnection, dsn, text)
            val textByteArray = text.replace("\r","").toByteArray()
            runMFTryCatchWrappedQuery(listener) {
                ZosDsn(zosConnection).writeDsn(dsn, textByteArray)
            }
            listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
        }

        fun writeToMember(listener: TaskListener,
                          zosConnection: ZOSConnection,
                          dsn: String,
                          member: String,
                          text: String,) {
            validateTextForDataset(listener, zosConnection, dsn, text)
            val textByteArray = text.replace("\r","").toByteArray()
            runMFTryCatchWrappedQuery(listener) {
                ZosDsn(zosConnection).writeDsn(dsn, member, textByteArray)
            }
            listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
        }
    }

}