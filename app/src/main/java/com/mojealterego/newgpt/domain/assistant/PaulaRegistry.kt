package com.mojealterego.newgpt.domain.assistant

import com.mojealterego.newgpt.domain.agent.AgentToolCatalog
import com.mojealterego.newgpt.domain.integration.ConnectedAppRegistry

object PaulaRegistry {
    val default = PaulaAssistant(
        toolIds = AgentToolCatalog.builtIns.map { it.id }.toSet(),
        connectedAppIds = ConnectedAppRegistry.catalog.map { it.id }.toSet(),
        videoAssetIds = listOf("paula-default")
    )
}
