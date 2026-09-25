package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetaArchitectureRuntimeTest {
    @Test
    fun alignmentGateRejectsChangedPolicyHash() {
        val gate = GovernanceInvariantGate()
        val policy = gate.policy("1", setOf("human-safety"), setOf("deception"))
        val proposal = MutationProposal(
            id = "m1",
            changedComponents = listOf("router"),
            invariantChecks = listOf("safety", "privacy"),
            policyHashBefore = policy.immutableHash,
            policyHashAfter = "changed"
        )
        assertFalse(gate.evaluate(policy, proposal).passed)
    }

    @Test
    fun confidenceGateEscalatesHighImpactUncertainty() {
        val gate = ConfidenceGate()
        assertFalse(gate.decide(0.97, highImpact = true))
        assertTrue(gate.decide(0.99, highImpact = true))
    }

    @Test
    fun dagRunsIndependentNodesConcurrentlyAndDependentNodesAfterwards() = runBlocking {
        val planner = DagPlanner()
        val result = planner.run(
            listOf(
                WorkflowNode("a", emptySet()) { "A" },
                WorkflowNode("b", emptySet()) { "B" },
                WorkflowNode("c", setOf("a", "b")) { "C" }
            )
        )
        assertTrue(result.failed.isEmpty())
        assertEquals("C", result.outputs["c"])
    }

    @Test
    fun gracefulDegradationSelectsLocalFallback() {
        assertEquals(
            "LOCAL_DEGRADED",
            DegradationController().choose(primaryHealthy = false, localAvailable = true)
        )
    }

    @Test
    fun structuredOutputRejectsMissingFields() {
        val missing = StructuredOutputGate().requireFields(
            mapOf("id" to "x"),
            setOf("id", "result")
        )
        assertEquals(listOf("result"), missing)
    }
}
