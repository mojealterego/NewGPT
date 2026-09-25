package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalRuntimeTest {
    @Test fun modelCatalogRoundTrip() {
        val catalog = ModelCatalog()
        catalog.upsert(ModelCatalog.Model("local-test", "local", "GGUF", "Q4_K_M", 8192, local = true))
        assertEquals(1, catalog.find("GGUF").size)
        catalog.remove("local-test")
        assertTrue(catalog.all().isEmpty())
    }

    @Test fun routerChoosesRegisteredEndpoint() = runBlocking {
        val router = ModelRouter3(clock = { 1_000L })
        router.register(ModelEndpoint("local", "llama.cpp", "local-gguf", setOf(RouteMode.LOCAL), priority = 1))
        assertEquals("local", router.choose(RouteMode.LOCAL)?.id)
    }

    @Test fun workflowRejectsUnknownDependency() {
        val errors = WorkflowEngine().validate(
            WorkflowDefinition("w", "test", listOf(
                WorkflowStep("a", WorkflowStepKind.MODEL, "x", dependsOn = setOf("missing"))
            ))
        )
        assertTrue("UNKNOWN_DEPENDENCY" in errors)
    }
}
