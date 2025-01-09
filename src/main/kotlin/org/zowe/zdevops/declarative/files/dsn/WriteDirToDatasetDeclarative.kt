/*
 * Copyright (c) 2025 IBA Group.
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

package org.zowe.zdevops.declarative.files.dsn

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.declarative.core.AbstractZosmfAction
import org.zowe.zdevops.declarative.jobs.zMessages
import org.zowe.zdevops.logic.writeDirectoryToDataset

/**
 * A declarative class for writing the contents of a directory to a PDS/E dataset.
 *
 * This class is designed to be used within a Jenkins pipeline as a step that writes files from a directory
 * to a specified z/OS dataset. It supports both local and build workspace-relative directory paths.
 *
 * @param dsn the dataset name (DSN) where the contents of the directory will be written.
 * @param dir the path to the directory containing the files to be written.
 * @param isLocalPath a flag indicating if the directory path is local (true) or relative to the build workspace (false).
 */
class WriteDirToDatasetDeclarative
@DataBoundConstructor constructor(
  private val dsn: String,
  private val dir: String,
  private val isLocalPath: Boolean = false,
) : AbstractZosmfAction() {

  fun getDsn(): String {
    return dsn
  }

  fun getDir(): String {
    return dir
  }

  fun getIsLocalPath(): Boolean {
    return isLocalPath
  }

  override val exceptionMessage: String = zMessages.zdevops_declarative_writing_DS_fail(dsn)


  override fun perform(
    run: Run<*, *>,
    workspace: FilePath,
    env: EnvVars,
    launcher: Launcher,
    listener: TaskListener,
    zosConnection: ZOSConnection
  ) {
    writeDirectoryToDataset(dsn, dir, isLocalPath, workspace, listener, zosConnection)
  }

  @Symbol("writeDirToDS")
  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("Write directory to PDS Dataset Declarative")
}
