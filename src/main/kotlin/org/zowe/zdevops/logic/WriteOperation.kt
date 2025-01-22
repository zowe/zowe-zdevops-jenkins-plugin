/*
 * Copyright (c) 2023-2025 IBA Group.
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
import hudson.FilePath
import hudson.model.TaskListener
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.kotlinsdk.zowe.client.sdk.zosfiles.ZosDsn
import org.zowe.kotlinsdk.zowe.client.sdk.zosuss.ZosUssFile
import org.zowe.zdevops.Messages
import org.zowe.zdevops.declarative.jobs.zMessages
import org.zowe.zdevops.utils.runMFTryCatchWrappedQuery
import java.io.File


/**
 * Parse dataset name and member name out of full dataset name
 *
 * @param fullName The name of a dataset in the form `DATASET.NAME` or `DATASET.NAME(MEMBER)`
 * @return (dataset, member) pair, where member name can be null
 */
fun parseDatasetName(fullName: String): Pair<String, String?> {
  val dataset = fullName.substringBefore("(")
  val member = if (fullName.contains("(")) {
    fullName.substringAfter("(").substringBefore(")").takeIf { it.isNotEmpty() }
  } else {
    null
  }
  return dataset to member
}

/**
 * Jenkins method that writes a text to a dataset
 *
 * @param listener      The listener for logging messages
 * @param zosConnection The ZOSConnection for interacting with z/OS
 * @param dsn           The name of a dataset in the form `DATASET.NAME` or `DATASET.NAME(MEMBER)`
 * @param text          The text content to be written
 * @throws AbortException if the text is not valid for the dataset or an error occurs during the write operation
 */
fun writeTextToDatasetJenkins(listener: TaskListener,
                              zosConnection: ZOSConnection,
                              dsn: String,
                              text: String,
                   ) {
  val (dataset, member) = parseDatasetName(dsn)
  validateExceedingLines(zosConnection, dataset, text.split('\n'))
  if (member.isNullOrBlank()) {
    writeToPS(text, dataset, zosConnection)
  } else {
    writeToPDS(text, dataset, member, zosConnection)
  }
  listener.logger.println(Messages.zdevops_declarative_writing_DS_success(dsn))
}


//TODO: docs
fun writeToFile(listener: TaskListener,
                zosConnection: ZOSConnection,
                destFile: String,
                textBytes: ByteArray,
                binary: Boolean?,) {
    if (textBytes.isNotEmpty()) {
        runMFTryCatchWrappedQuery(listener) {
            if (binary == true) {
                ZosUssFile(zosConnection).writeToFileBin(destFile, textBytes)
            } else {
                ZosUssFile(zosConnection).writeToFile(destFile, textBytes)
            }
        }
        listener.logger.println(Messages.zdevops_declarative_writing_file_success(destFile))
    } else {
        listener.logger.println(Messages.zdevops_declarative_writing_skip())
    }
}

/**
 * Finds the line numbers in a file where the line length exceeds a specified record length (LRECL).
 *
// * @param file the file to be read and analyzed.
 * @param text the text to validate for lines exceeding the target dataset's record length
 * @param lrecl the maximum allowed record length for each line.
 * @return a list of line numbers where the line length exceeds the specified LRECL.
 */
fun findLinesExceedingRecordLength(text: List<String>, lrecl: Int): List<Int> {
  return text.mapIndexedNotNull { index, line ->
    if(line.length > lrecl) index + 1 else null
  }
}

/**
 * Jenkins Step that writes the contents of a specified directory to a PDS/E dataset on z/OS.
 *
 * @param dsn the dataset name (DSN) where the contents of the directory will be written.
 * @param dir the path to the directory containing the files to be written.
 * @param isLocalPath a flag indicating if the provided directory path is local (true) or relative to the workspace (false).
 * @param workspace the root workspace path used to resolve relative paths if `isLocalPath` is false.
 * @param listener the task listener used for logging and user feedback during execution.
 * @param zosConnection the connection information for interacting with the z/OS system.
 * @throws AbortException if the dataset information cannot be retrieved.
 * @throws IllegalArgumentException if the provided directory path does not exist or is invalid.
 */
fun writeDirectoryToPdsJenkins(
  dsn: String,
  dir: String,
  isLocalPath: Boolean,
  workspace: FilePath,
  listener: TaskListener,
  zosConnection: ZOSConnection,
) {
  listener.logger.println(zMessages.zdevops_declarative_writing_DS_from_dir(dsn, dir, zosConnection.host, zosConnection.zosmfPort))

  val localOrWsDirPath = resolveDirectoryPath(dir, isLocalPath, listener, workspace)
  validatePathExists(localOrWsDirPath)
  validatePathLeadsToDirectory(localOrWsDirPath)
  val directoryEntries = localOrWsDirPath.listFiles().orEmpty()
  if (directoryEntries.isEmpty()) {
    listener.logger.println("Warning: The directory '$localOrWsDirPath' is empty.")
    return
  }

  listener.logger.println("Found ${directoryEntries.size} entries in the directory:")
  directoryEntries.forEachIndexed { index, entry ->  processEntry(index, entry, dsn, listener, zosConnection) }

  listener.logger.println(zMessages.zdevops_declarative_writing_DS_success(dsn))
}


/**
 * Resolves the directory path based on whether it is a local path or a Jenkins workspace-relative path.
 *
 * @param dir the directory path to resolve.
 * @param isLocalPath a flag indicating if the provided path is local (true) or relative to the workspace (false).
 * @param listener the task listener used for logging information about the resolution process.
 * @param workspace the root workspace path used to construct the resolved path if `isLocalPath` is false.
 * @return the resolved directory as a `File` object.
 */
private fun resolveDirectoryPath(dir: String, isLocalPath: Boolean, listener: TaskListener, workspace: FilePath): File {
  return if (isLocalPath) {
    listener.logger.println("Using local path.")
    File(dir)
  } else {
    val workspacePath = "${workspace.parent}${File.separator}$dir"
    listener.logger.println("Using workspace path: '$workspacePath'.")
    File(workspacePath)
  }
}

/**
 * Validates that the specified file path exists.
 *
 * @param path the file or directory to check for existence.
 * @throws AbortException if the specified path does not exist.
 */
fun validatePathExists(path: File) {
  if (!path.exists()) {
    throw AbortException("The path '$path' does not exist.")
  }
}

/**
 * Validates that the specified file path is a directory.
 *
 * @param dir the file to check.
 * @throws AbortException if the specified path is not a directory.
 */
fun validatePathLeadsToDirectory(dir: File) {
  if (!dir.isDirectory) {
    throw AbortException("The path '$dir' is not a directory.")
  }
}

/**
 * Processes a single file or directory entry from a directory listing during iteration.
 *
 * @param index the current index of the entry in the directory listing.
 * @param entry the file or directory being processed.
 * @param dsn the dataset name (DSN) where the file will be written.
 * @param listener the task listener used for logging progress and feedback.
 * @param zosConnection the connection information for interacting with z/OS system.
 *
 * Logs details about the entry and writes the file to the dataset if the entry is a regular file.
 * Skips processing if the entry is a directory.
 */
private fun processEntry(
  index: Int,
  entry: File,
  dsn: String,
  listener: TaskListener,
  zosConnection: ZOSConnection
) {
  if (entry.isDirectory) {
    listener.logger.println("[$index] - Skipping directory entry: '${entry.name}'.")
    return
  }
  listener.logger.println("[$index] - Processing entry: '${entry.name}'")
  writeFileToPDS(entry, dsn, zosConnection)
}

/**
 * Writes the contents of a file to a member of the specified dataset on z/OS.
 *
 * @param file the file whose contents will be written to the dataset.
 * @param dsn the dataset name (DSN) where the file content will be written.
 * @param zosConnection the connection information for interacting with the z/OS system.
 *
 * @throws AbortException if the file contains lines exceeding the dataset's record length.
 *
 * The file content is written to a member in the specified dataset,
 * using the file's name (without extension) as the member name.
 */
fun writeFileToPDS(
  file: File,
  dsn: String,
  zosConnection: ZOSConnection,
) {
  validateExceedingLines(zosConnection, dsn, file.readLines())
  writeToPDS(file.readText(), dsn, file.nameWithoutExtension, zosConnection)
}

/**
 * This function validates that text conform to the specified record length (LRECL).
 * If any line exceeds this length, an error is logged, and the operation is aborted.
 *
 * @param dsn the dataset name (DSN) where the file content will be written.
 * @param text the text to validate for lines exceeding the target dataset's record length
 * @param zosConnection the connection information for interacting with the z/OS system.
 * @throws AbortException if the file contains lines exceeding the dataset's record length.
 */
fun validateExceedingLines(
  zosConnection: ZOSConnection,
  dsn: String,
  text: List<String>
) {
  val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
  val lrecl = targetDS.recordLength ?: throw AbortException(zMessages.zdevops_declarative_writing_DS_no_info(dsn))

  val exceedingLines = findLinesExceedingRecordLength(text, lrecl)
  if (exceedingLines.isNotEmpty()) {
    throw AbortException(
      "Error: Text contains lines exceeding record length '$lrecl' of dataset '$dsn'. " +
              "Exceeding line numbers: $exceedingLines"
    )
  }
}

/**
 * Writes the provided text to a specific member of a Partitioned Data Set (PDS) on z/OS.
 *
 * @param text the content to write to the dataset member.
 * @param dsn the dataset name (DSN) where the member resides.
 * @param member the name of the member within the dataset to write to.
 * @param zosConnection the connection information for interacting with the z/OS system.
 */
fun writeToPDS(text: String, dsn: String, member: String, zosConnection: ZOSConnection) {
  ZosDsn(zosConnection).writeDsn(dsn, member, prepareTextForWritingToDS(text))
}

/**
 * Writes the provided text to a Sequential Data Set (PS) on z/OS.
 *
 * @param text the content to write to the dataset.
 * @param dsn the dataset name (DSN).
 * @param zosConnection the connection information for interacting with the z/OS system.
 */
fun writeToPS(text: String, dsn: String, zosConnection: ZOSConnection) {
  ZosDsn(zosConnection).writeDsn(dsn, prepareTextForWritingToDS(text))
}

/**
 * Prepares the provided text for writing to a dataset by removing carriage return characters and converting it to a byte array.
 *
 * @param text the text to be prepared for writing to the dataset.
 * @return a byte array representing the prepared text.
 *
 * Carriage return characters create additional empty records after each line. That's why they should be removed
 */
fun prepareTextForWritingToDS(text: String): ByteArray {
  return text.replace("\r","").toByteArray()
}