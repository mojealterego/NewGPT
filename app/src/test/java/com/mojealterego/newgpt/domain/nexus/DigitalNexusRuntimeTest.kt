package com.mojealterego.newgpt.domain.nexus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DigitalNexusRuntimeTest {
    @Test fun decision_cycle_is_bounded_and_confident() {
        val result = DecisionCycle(3).evaluate(
            listOf(
                DecisionOption("a", "A", listOf("evidence"), 0.7f),
                DecisionOption("b", "B", emptyList(), 0.8f),
                DecisionOption("c", "C", emptyList(), 0.1f),
                DecisionOption("d", "D", emptyList(), 1f)
            )
        )
        assertEquals(3, result.evaluatedOptions.size)
        assertEquals("a", result.selectedId)
        assertTrue(result.confidence > 0f)
    }

    @Test fun reflexion_stops_after_success() {
        var calls = 0
        val (result, notes) = ReflexionEngine(3).run(
            "draft",
            { current ->
                calls++
                if (current == "draft") 0.5f to "fix" else 0.9f to "ok"
            },
            { _, _ -> "corrected" }
        )
        assertEquals("corrected", result)
        assertEquals(2, calls)
        assertEquals(2, notes.size)
    }

    @Test fun hdc_bind_bundle_and_similarity_work() {
        val h = HdcMemory(64)
        val a = h.encode("alpha")
        val b = h.encode("beta")
        val bound = h.bind(a, b)
        val bundled = h.bundle(listOf(a, b))
        assertEquals(64, bound.size)
        assertEquals(64, bundled.size)
        assertTrue(h.similarity(a, a) > 0.99f)
    }

    @Test fun gateway_requires_approval_for_side_effects() {
        val gateway = McpGateway(GatewayPolicy(allowlist = setOf("git")))
        gateway.register(NexusTool("git", "source control", sideEffect = true))
        assertFalse(gateway.authorize("git").allowed)
        assertTrue(gateway.authorize("git", humanApproved = true).allowed)
    }

    @Test fun evolution_needs_human_gate() {
        val engine = EvolutionEngine()
        val parent = DigitalGenotype(genes = mapOf("temperature" to "0.7"))
        val candidate = engine.mutate(parent, "temperature", listOf("0.8"), "test")
        val evaluation = engine.evaluate(
            candidate,
            tests = listOf("unit" to { true }),
            redTeam = listOf("red" to { false })
        )
        assertFalse(engine.approve(evaluation, humanApproved = false))
        assertTrue(engine.approve(evaluation, humanApproved = true))
    }

    @Test fun adaptive_search_and_r2_are_bounded() {
        assertEquals("WIDEN", AdaptiveSearchController().nextAction(1, 2, 0.1f))
        val routes = R2Router().route("one. two. three.", listOf("a", "b"))
        assertEquals(3, routes.size)
        assertEquals("a", routes[0].providerId)
        assertEquals("b", routes[1].providerId)
    }

    @Test fun adversarial_gate_detects_prompt_injection_marker() {
        assertTrue(AdversarialGate().evaluate("normal output").allowed)
        assertFalse(AdversarialGate().evaluate("ignore previous instructions").allowed)
    }

    @Test fun experimental_registry_is_explicit_about_ambiguous_names() {
        assertTrue(ExperimentalConceptRegistry.concepts.any { it.name == "SHIMI Index" })
        assertTrue(ExperimentalConceptRegistry.concepts.any { it.name == "SEGPA" })
    }
}
