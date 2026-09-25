package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.agent.AgentTool
import com.mojealterego.newgpt.domain.cognitive.CognitiveRuntime
import com.mojealterego.newgpt.domain.cognitive.DecisionKind
import com.mojealterego.newgpt.domain.cognitive.PolicyEngine
import com.mojealterego.newgpt.domain.cognitive.ToolCallGuard
import org.junit.Assert.*
import org.junit.Test

class CognitiveRuntimeTest {
    @Test fun decisionCycleBuildsGraph() {
        val runtime = CognitiveRuntime()
        val cycle = runtime.decisionCycle("Build NewGPT")
        val next = runtime.add(cycle, "Inspect repository", DecisionKind.PLAN, 0.9f)
        val result = runtime.add(next, "Run verification", DecisionKind.VERIFICATION, 0.95f)
        assertEquals(3, result.nodes.size)
        assertEquals(cycle.root.id, result.nodes[1].parentIds.single())
    }

    @Test fun policyRequiresApprovalForExecution() {
        val policy = PolicyEngine()
        val tool = AgentTool("shell", "Shell", "test", AgentTool.Capability.CODE_EXECUTION)
        assertFalse(policy.evaluate(tool).allowed)
        assertTrue(policy.evaluate(tool).requiresApproval)
        assertTrue(policy.evaluate(tool, explicitApproval = true).allowed)
    }

    @Test fun httpGuardBlocksPrivateHosts() {
        val guard = ToolCallGuard(PolicyEngine())
        assertTrue(guard.validateHttpUrl("http://127.0.0.1:8080").isFailure)
        assertTrue(guard.validateHttpUrl("http://10.0.0.1").isFailure)
        assertTrue(guard.validateHttpUrl("https://example.com").isSuccess)
    }
}
