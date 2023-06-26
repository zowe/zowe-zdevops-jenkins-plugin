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

import hudson.model.TaskListener
import org.zowe.kotlinsdk.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.zdevops.Messages

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