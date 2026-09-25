package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NexusRuntimeFacadeTest {
    @Test
    fun facadeRegistersCapabilitiesAndEnforcesPrivateMode() = runBlocking {
        val runtime = NexusRuntimeFacade()
        runtime.registerEndpoint(
            ModelEndpoint(
                id = "local",
                provider = "gguf",
                model = "local-model",
                modes = setOf(RouteMode.LOCAL, RouteMode.PRIVATE),
                priority = 1
            )
        )

        assertEquals("local", runtime.chooseEndpoint(RouteMode.PRIVATE)?.id)
        assertTrue(runtime.accepts(MediaKind.VIDEO))
        assertFalse(runtime.canSend(setOf("private_key"), "CLOUD", privateMode = true))
        assertTrue(runtime.canSend(emptySet(), "LOCAL", privateMode = true))
        assertTrue(runtime.validateWorkflow(
            WorkflowDefinition(
                id = "wf",
                name = "safe",
                steps = listOf(
                    WorkflowStep("one", WorkflowStepKind.MODEL, "input"),
                    WorkflowStep("two", WorkflowStepKind.TRANSFORM, "input", setOf("one"))
                )
            )
        ).isEmpty())
        assertTrue(runtime.releaseReady(listOf(ReleaseGate("build", true, "ok"))))
        assertTrue(runtime.snapshot(RouteMode.PRIVATE).capabilities.size >= 8)
    }
}
