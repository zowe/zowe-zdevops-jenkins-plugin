/*
 * Copyright (c) 2022-2024 IBA Group.
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
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsnList
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.input.ListParams
import org.zowe.zdevops.declarative.jobs.zMessages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery
import javax.naming.InvalidNameException

private val successMessage: String = zMessages.zdevops_deleting_ds_success()

/**
 * Deletes datasets matching the provided mask
 *
 * @param mask The mask used to filter datasets.
 * @param zosConnection The z/OS connection to be used for dataset deletion.
 * @param listener The task listener to log information and handle exceptions.
 * @throws AbortException If the mask is empty or no matching datasets are found.
 */
fun deleteDatasetsByMask(mask: String, zosConnection: ZOSConnection, listener: TaskListener, failOnNotExist: Boolean) {
    if (mask.isEmpty()) {
        throw AbortException(zMessages.zdevops_deleting_datasets_by_mask_but_mask_is_empty())
    }
    listener.logger.println(zMessages.zdevops_deleting_ds_by_mask(mask))
    try {
        val dsnList = ZosDsnList(zosConnection).listDsn(mask, ListParams())
        if (dsnList.items.isEmpty()) {
            throw AbortException(zMessages.zdevops_deleting_ds_fail_no_matching_mask())
        }
        dsnList.items.forEach {
            runMFTryCatchWrappedQuery(listener) {
                listener.logger.println(zMessages.zdevops_deleting_ds(it.name, zosConnection.host, zosConnection.zosmfPort))
                ZosDsn(zosConnection).deleteDsn(it.name)
            }
        }
        listener.logger.println(successMessage)
    } catch (doesNotExistEx: Exception) {
        if(failOnNotExist) {
            throw doesNotExistEx
        }
        listener.logger.println("Reason: $doesNotExistEx")
        // TODO I wanna have the dataset name here - it's inside exception message?
        listener.logger.println("Dataset deletion failed, but the `failOnNotExist` option is set to false. Continuing with execution.")
    }
}

/**
 * Deletes a dataset or member
 *
 * @param dsnWithMemName The dataset name in the form ZOSMFAD.TEST(MEMNAME).
 * @param zosConnection The z/OS connection to be used for dataset deletion.
 * @param listener The task listener to log information and handle exceptions.
 * @throws AbortException If the dataset name is empty or the member name is invalid.
 */
fun deleteDatasetOrMember(dsnWithMemName: String, zosConnection: ZOSConnection, listener: TaskListener, failOnNotExist: Boolean) {
    if (dsnWithMemName.isEmpty()) {
        throw AbortException(zMessages.zdevops_deleting_ds_fail_dsn_param_empty())
    }
    val dsnMemberPattern = Regex("[\\w#\$@.-]{1,}\\([\\w#\$@]+\\)") //means it's a PDS member
    var member: String? = null
    if (dsnWithMemName.contains(dsnMemberPattern)) {
        member = dsnWithMemName.substringAfter('(').substringBefore(')')
    }
    val logMessage = if (!member.isNullOrEmpty()) zMessages.zdevops_deleting_ds_member(dsnWithMemName, zosConnection.host, zosConnection.zosmfPort)
                     else zMessages.zdevops_deleting_ds(dsnWithMemName, zosConnection.host, zosConnection.zosmfPort)
    listener.logger.println(logMessage)
    try {
        if (!member.isNullOrEmpty()) {
            isMemberNameValid(member)
            ZosDsn(zosConnection).deleteDsn(dsnWithMemName, member)
        } else {
            ZosDsn(zosConnection).deleteDsn(dsnWithMemName)
        }
        listener.logger.println(successMessage)
    } catch (e: InvalidNameException) {
        throw e
    } catch (doesNotExistEx: Exception) {
        if(failOnNotExist) {
            throw doesNotExistEx
        }
        listener.logger.println("Reason: $doesNotExistEx")
        listener.logger.println("Dataset deletion failed, but the `failOnNotExist` option is set to false. Continuing with execution.")
    }
}

/**
 * Validates a member name.
 *
 * @param member The member name to validate.
 * @throws Exception If the member name is invalid.
 */
private fun isMemberNameValid(member: String) {
    if (member.length > 8 || member.isEmpty())
        throw InvalidNameException(zMessages.zdevops_member_name_invalid())
}

