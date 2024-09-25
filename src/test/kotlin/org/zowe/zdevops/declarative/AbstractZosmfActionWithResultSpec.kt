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
import hudson.model.TaskListener
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.testutils.getPrivateFieldValue
import org.zowe.zdevops.utils.getZoweZosConnection
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Future

class AbstractZosmfActionWithResultSpec : ShouldSpec({
  var runCalledCount = 0

  val zosmfActionWithResult = object : AbstractZosmfActionWithResult() {
    override fun run(
      workspace: FilePath,
      listener: TaskListener,
      envVars: EnvVars,
      zoweZOSConnection: ZOSConnection
    ): String {
      runCalledCount++
      return "test"
    }
  }

  afterEach {
    runCalledCount = 0
  }

  context("declarative module: AbstractZosmfActionWithResult") {
    should("check execution runs the step provided when the task is started and finished successfully") {
      val workspace = mockk<FilePath>()
      every { workspace.read() } returns ByteArrayInputStream("test".toByteArray(StandardCharsets.UTF_8))

      val stepContext = mockk<StepContext>()
      every { stepContext.get(FilePath::class.java) } returns workspace
      every { stepContext.get(TaskListener::class.java) } returns mockk<TaskListener>()
      every { stepContext.get(EnvVars::class.java) } returns mockk<EnvVars>()
      every { stepContext.onSuccess(any()) } just Runs

      mockkStatic(::getZoweZosConnection)
      every { getZoweZosConnection("test", any()) } returns mockk<ZOSConnection>()

      val execution = zosmfActionWithResult.start(stepContext) as AbstractZosmfActionWithResult.Companion.Execution
      execution.start()
      val executionTask: Future<*> = getPrivateFieldValue(
        execution,
        SynchronousNonBlockingStepExecution::class.java,
        "task"
      ) as Future<*>
      executionTask.get()

      verify(exactly = 1) { stepContext.onSuccess(any()) }
      assertSoftly { runCalledCount shouldBe 1 }
    }
  }
})