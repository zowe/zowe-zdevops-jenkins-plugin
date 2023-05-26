package org.zowe.zdevops.logic

import hudson.model.TaskListener
import org.jenkinsci.remoting.SerializableOnlyOverRemoting
import org.zowe.kotlinsdk.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery

class AllocateOperation {

    companion object {
        fun allocateDataset(listener: TaskListener,
                            zosConnection: ZOSConnection,
                            dsn: String,
                            volser: String?,
                            unit: String?,
                            dsOrg: DatasetOrganization,
                            alcUnit: AllocationUnit?,
                            primary: Int,
                            secondary: Int,
                            dirBlk: Int?,
                            recFm: RecordFormat,
                            blkSize: Int?,
                            lrecl: Int?,
                            storClass: String?,
                            mgntClass: String?,
                            dataClass: String?,
                            avgBlk: Int?,
                            dsnType: DsnameType?,
                            dsModel: String?

        ) {
            listener.logger.println(Messages.zdevops_declarative_DSN_allocating(dsn, zosConnection.host, zosConnection.zosmfPort))
            val alcParms = CreateDataset(
                volser,
                unit,
                dsOrg,
                alcUnit,
                primary,
                secondary,
                dirBlk,
                recFm,
                blkSize,
                lrecl,
                storClass,
                mgntClass,
                dataClass,
                avgBlk,
                dsnType,
                dsModel
            )
            ZosDsn(zosConnection).createDsn(dsn, alcParms)
            listener.logger.println(Messages.zdevops_declarative_DSN_allocated_success(dsn))
        }
    }
}