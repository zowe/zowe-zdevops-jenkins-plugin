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

import hudson.model.TaskListener
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.*
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.zowe.kotlinsdk.zowe.client.sdk.core.ZOSConnection
import org.zowe.zdevops.config.ZOSConnectionList
import org.zowe.zdevops.model.ResolvedZOSConnection
import org.zowe.zdevops.utils.getZoweZosConnection
import org.zowe.zdevops.utils.validateConnection

class ZosmfStepDeclarativeSpec : ShouldSpec({
  context("declarative module: ZosmfStepDeclarative") {
    should("check the zosmf declarative step is run, connection is formed and validated") {
      val zosmfStepDeclarative = ZosmfStepDeclarative("test")

      val taskListener = mockk<TaskListener>()

      val stepContext = mockk<StepContext>()
      every { stepContext.get(TaskListener::class.java) } returns taskListener

      val resolvedZosConnection = mockk<ResolvedZOSConnection>()
      every { resolvedZosConnection.url } returns "https://test.com:1234"
      every { resolvedZosConnection.username } returns "test_user"
      every { resolvedZosConnection.password } returns "test_pass"

      mockkObject(ZOSConnectionList)
      every { ZOSConnectionList.resolve("test") } returns resolvedZosConnection

      mockkStatic(::validateConnection)
      every { validateConnection(any<ZOSConnection>()) } returns Unit

      zosmfStepDeclarative.start(stepContext)
      verify(exactly = 1) { validateConnection(any<ZOSConnection>()) }
      verify(exactly = 1) { getZoweZosConnection("test", taskListener) }
    }
  }
})
