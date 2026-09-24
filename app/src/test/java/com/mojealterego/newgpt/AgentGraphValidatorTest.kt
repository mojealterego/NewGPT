package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.domain.agent.AgentGraphValidator
import com.mojealterego.newgpt.domain.agent.defaultAgents
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentGraphValidatorTest {
    @Test fun acceptsDefaultAgentGraph() {
        val result = AgentGraphValidator.validateGraph(defaultAgents())
        assertTrue(result.errors.joinToString(), result.valid)
    }

    @Test fun rejectsSelfHandoff() {
        val agent = AgentDefinition("a", "A", "A", "prompt", handoffs = listOf("a"))
        assertFalse(AgentGraphValidator.validate(agent, listOf(agent)).valid)
    }

    @Test fun rejectsUnknownHandoff() {
        val agent = AgentDefinition("a", "A", "A", "prompt", handoffs = listOf("missing"))
        assertFalse(AgentGraphValidator.validate(agent, listOf(agent)).valid)
    }

    @Test fun rejectsCycle() {
        val a = AgentDefinition("a", "A", "A", "prompt", handoffs = listOf("b"))
        val b = AgentDefinition("b", "B", "B", "prompt", handoffs = listOf("a"))
        assertFalse(AgentGraphValidator.validateGraph(listOf(a, b)).valid)
    }
}
