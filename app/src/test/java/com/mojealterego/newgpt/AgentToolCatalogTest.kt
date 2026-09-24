package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.agent.AgentToolCatalog
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentToolCatalogTest {
    @Test fun builtInsHaveUniqueIds() {
        val ids = AgentToolCatalog.builtIns.map { it.id }
        assertTrue(ids.size == ids.toSet().size)
    }

    @Test fun privilegedCapabilitiesRequireApproval() {
        assertTrue(
            AgentToolCatalog.builtIns
                .filter { it.capability != com.mojealterego.newgpt.domain.agent.AgentTool.Capability.READ_ONLY }
                .all { it.requiresApproval }
        )
    }

    @Test fun knownToolCanBeResolved() {
        assertNotNull(AgentToolCatalog.find("web"))
    }
}
