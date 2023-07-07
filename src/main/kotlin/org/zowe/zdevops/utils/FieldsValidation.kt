/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2023
 */

package org.zowe.zdevops.utils

import hudson.util.FormValidation

fun validateDatasetName(dsn: String): FormValidation? {
    val dsnPattern = Regex("^[a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}([.][a-zA-Z#\$@][a-zA-Z0-9#\$@-]{0,7}){0,21}$")

    return if (dsn.isNotBlank()) {
        if (!dsn.matches(dsnPattern)) {
            FormValidation.warning("It seems the dataset name is invalid")
        } else {
            FormValidation.ok()
        }
    } else {
        FormValidation.error("Field must not be empty")
    }
}

fun validateMemberName(member: String): FormValidation? {
    val memberPattern = Regex("^(?:[A-Z#@\$][A-Z0-9#@\$]{0,7}|[a-z#@\$][a-zA-Z0-9#@\$]{0,7})\$")

    return if (member.length > 8 || member.isEmpty()) {
        FormValidation.error("The field must be between 1 and 8 characters in length")
    } else if(!member.matches(memberPattern)) {
        FormValidation.warning("It seems the member name is invalid")
    } else {
        FormValidation.ok()
    }
}

fun validateFieldIsNotEmpty(value: String): FormValidation? {
    return if (value == "") {
        FormValidation.error("Field must not be empty")
    } else {
        FormValidation.ok()
    }
}