package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.agent.AgentDagScheduler
import com.mojealterego.newgpt.domain.agent.AgentExecutionNode
import com.mojealterego.newgpt.domain.agent.AgentExecutionPlan
import com.mojealterego.newgpt.domain.agent.AgentNodeResult
import com.mojealterego.newgpt.domain.agent.AgentNodeStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentDagSchedulerTest {
    @Test
    fun executesDependenciesBeforeDependents(): Unit = runBlocking {
        val scheduler = AgentDagScheduler()
        val a = AgentExecutionNode(id = "a", agentId = "researcher", input = "start")
        val b = AgentExecutionNode(id = "b", agentId = "writer", dependsOn = listOf("a"), input = "finish")
        val report = scheduler.execute(AgentExecutionPlan(nodes = listOf(a, b))) { node, previous ->
            AgentNodeResult(
                nodeId = node.id,
                agentId = node.agentId,
                status = AgentNodeStatus.SUCCEEDED,
                output = node.input + previous.values.joinToString { it.output }
            )
        }
        assertTrue(report.success)
        assertEquals(listOf("a", "b"), report.results.map { it.nodeId })
    }

    @Test
    fun rejectsCycles(): Unit = runBlocking {
        val scheduler = AgentDagScheduler()
        val a = AgentExecutionNode(id = "a", agentId = "a", dependsOn = listOf("b"), input = "")
        val b = AgentExecutionNode(id = "b", agentId = "b", dependsOn = listOf("a"), input = "")
        var rejected = false
        try {
            scheduler.execute(AgentExecutionPlan(nodes = listOf(a, b))) { node, _ ->
                AgentNodeResult(node.id, node.agentId, AgentNodeStatus.SUCCEEDED)
            }
        } catch (_: IllegalStateException) {
            rejected = true
        }
        assertTrue(rejected)
    }
}
