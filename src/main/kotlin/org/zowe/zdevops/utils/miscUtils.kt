package org.zowe.zdevops.utils

import hudson.AbortException
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintStream

fun createFileAndWriteContent(filepath: String, fileContent: String): File {
    val textFile = File(filepath)
    try {
        val writer = BufferedWriter(FileWriter(textFile))
        writer.write(fileContent)
        writer.close()
    } catch (e: Exception) {
        throw AbortException("Error occurred while creating file: ${e.message}")
    }
    return textFile
}

fun deleteFile(filepath: String, logger: PrintStream) {
    val file = File(filepath)
    if (file.exists()) {
        if (file.delete()) {
            logger.println("File deleted successfully.")
        } else {
            logger.println("Failed to delete the file.")
        }
    } else {
        logger.println("File does not exist.")
    }
}