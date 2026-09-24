package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.agent.defaultAgents
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentDefinitionTest {
    @Test fun defaultAgentRegistryContainsCoreRoles() {
        val ids = defaultAgents().map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("coordinator","researcher","architect","coder","writer","wda-photo","mobile-operator")))
    }

    @Test fun coordinatorHasHandoffs() {
        val coordinator = defaultAgents().first { it.id == "coordinator" }
        assertTrue(coordinator.handoffs.isNotEmpty())
    }
}
