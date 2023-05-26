package org.zowe.zdevops.utils

import hudson.util.FormValidation
import org.kohsuke.stapler.QueryParameter

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