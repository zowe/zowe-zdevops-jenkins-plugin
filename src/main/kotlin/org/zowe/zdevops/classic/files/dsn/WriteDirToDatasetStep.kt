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
package org.zowe.zdevops.classic.files.dsn

import hudson.AbortException
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.util.FormValidation
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.classic.core.AbstractBuildStep
import org.zowe.zdevops.logic.writeDirectoryToPdsJenkins
import org.zowe.zdevops.utils.validateDatasetName
import org.zowe.zdevops.utils.validateFieldIsNotEmpty


/**
 * A freestyle job Jenkins class for writing the contents of a directory to a PDS/E dataset.
 * @see org.zowe.zdevops.declarative.files.dsn.WriteDirToDatasetDeclarative
 */
class WriteDirToDatasetStep
@DataBoundConstructor
constructor(
  connectionName: String,
  val dir: String,
  val dsn: String,
  var isLocalPath: Boolean,
) : AbstractBuildStep(connectionName) {

  fun getIsLocalPath(): Boolean {
    return this.isLocalPath
  }

  @DataBoundSetter
  fun setIsLocalPath(isLocalPath: Boolean) {
    this.isLocalPath = isLocalPath
  }

  override fun perform(
    build: AbstractBuild<*, *>,
    launcher: Launcher,
    listener: BuildListener,
    zosConnection: ZOSConnection
  ) {
    val workspace = build.executor?.currentWorkspace ?: throw AbortException("'build.executor' was null")
    writeDirectoryToPdsJenkins(dsn, dir, isLocalPath, workspace, listener, zosConnection)
  }

  @Extension
  class DescriptorImpl : Companion.DefaultBuildDescriptor("[z/OS] - Write Dir to PDS/E") {

    /**
     * Checks if the path to the directory is not empty
     *
     * @param dir The path to the directory
     * @return FormValidation.ok() if the path name is not empty, or an error message otherwise
     */
    fun doCheckDir(@QueryParameter dir: String): FormValidation? {
      return validateFieldIsNotEmpty(dir)
    }

    /**
     * Checks if the dataset name is valid
     *
     * @param dsn The dataset name
     * @return FormValidation.ok() if the dataset name is valid, or an error message otherwise
     */
    fun doCheckDsn(@QueryParameter dsn: String): FormValidation? {
      return validateDatasetName(dsn)
    }

  }
}