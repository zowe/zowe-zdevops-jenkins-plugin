package org.zowe.zdevops.logic

import hudson.AbortException
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery

class WriteToDatasetOperation {
    companion object {
        fun writeToDataset(listener: TaskListener,
                           zosConnection: ZOSConnection,
                           dsn:String,
                           text: String,
                           ) {
            if (text != "") {
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
                } else {
                    val textByteArray = text.replace("\r","").toByteArray()
                    runMFTryCatchWrappedQuery(listener) {
                        ZosDsn(zosConnection).writeDsn(dsn, textByteArray)
                    }
                    listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
                }
            } else {
                listener.logger.println(Messages.zdevops_declarative_writing_skip())
            }
        }
    }
}