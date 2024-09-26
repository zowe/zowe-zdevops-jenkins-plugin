/*
 * Copyright (c) 2024 IBA Group.
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

package org.zowe.zdevops.declarative

import hudson.EnvVars
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.plugins.workflow.steps.*
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.utils.getZoweZosConnection
import java.nio.charset.StandardCharsets

/**
 * Abstract class that represents an action that performs some work with a String result in a Jenkins pipeline.
 * This class is designed to be extended by other classes that need to perform specific actions
 * and return a result. In order to return the result, the method must be wrapped in `script` tag.
 */
abstract class AbstractZosmfActionWithResult : Step() {

  /**
   * Executes the action using the provided context and returns the result as a String.
   *
   * @param workspace the workspace where the action is executed.
   * @param listener the listener used to log messages during execution.
   * @param envVars the environment variables that may influence the execution.
   * @param zoweZOSConnection the connection to z/OSMF used for the action execution.
   * @return the result of the action as a String.
   */
  abstract fun run(workspace: FilePath, listener: TaskListener, envVars: EnvVars, zoweZOSConnection: ZOSConnection): String

  /**
   * Starts the execution of this step.
   *
   * @param context the context in which the step is executed.
   * @return a StepExecution instance that will manage the execution of this step.
   */
  override fun start(context: StepContext): StepExecution {
    return Execution(this, context)
  }

  companion object {
    /**
     * Default descriptor for steps that extend AbstractZosmfActionWithResult.
     * Provides metadata about the step for Jenkins, which in our case is a name for the method in declarative pipeline.
     */
    open class DefaultStepDescriptor(private val functionName: String): StepDescriptor() {

      /**
       * Provides the context to the Step.
       */
      override fun getRequiredContext(): Set<Class<*>> {
        return setOf<Class<*>>(
          Run::class.java,
          FilePath::class.java,
          TaskListener::class.java,
          EnvVars::class.java
        )
      }

      /**
       * The name for a declarative method returning result.
       */
      override fun getFunctionName(): String = functionName

      /**
       * We do not expect a block argument as an input for a declarative method returning result.
       */
      override fun takesImplicitBlockArgument(): Boolean = false
    }

    /**
     * Synchronous step execution class for actions that extend AbstractZosmfActionWithResult.
     * Manages the execution of the step and retrieves the necessary context information.
     */
    class Execution(@Transient private val step: AbstractZosmfActionWithResult, context: StepContext)
      : SynchronousNonBlockingStepExecution<String>(context) {
      /**
       * Prepares everything for the actual step run.
       */
      override fun run(): String {
        val workspace: FilePath = getClassFromContext(context, FilePath::class.java)
        val listener = getClassFromContext(context, TaskListener::class.java)
        val env = getClassFromContext(context, EnvVars::class.java)

        val connectionName: String = workspace.read().readBytes().toString(StandardCharsets.UTF_8)
        val zoweConnection = getZoweZosConnection(connectionName, listener)

        return step.run(workspace, listener, env, zoweConnection)
      }

      /**
       * Gets necessary context classes for the execution and ensures they are not empty.
       */
      private fun <T> getClassFromContext(context: StepContext, clazz: Class<T>): T {
        return context.get(clazz) ?: throw RuntimeException("Couldn't get ${clazz.simpleName}")
      }

    }
  }
}