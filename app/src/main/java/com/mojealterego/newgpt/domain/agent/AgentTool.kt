package com.mojealterego.newgpt.domain.agent

import kotlinx.serialization.Serializable

@Serializable
data class AgentTool(
    val id: String,
    val name: String,
    val description: String,
    val capability: Capability,
    val requiresApproval: Boolean = true
) {
    @Serializable
    enum class Capability {
        READ_ONLY,
        NETWORK,
        FILE_SYSTEM,
        ANDROID_UI,
        NOTIFICATIONS,
        CODE_EXECUTION
    }
}

object AgentToolCatalog {
    val builtIns = listOf(
        AgentTool("web", "Web", "Odczyt informacji z sieci.", AgentTool.Capability.NETWORK),
        AgentTool("documents", "Documents", "Analiza dostarczonych dokumentów.", AgentTool.Capability.READ_ONLY),
        AgentTool("repository", "Repository", "Praca z kodem i plikami repozytorium.", AgentTool.Capability.FILE_SYSTEM),
        AgentTool("compiler", "Compiler", "Weryfikacja kodu przez narzędzia budowania.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("image-generation", "Image Generation", "Generowanie i edycja obrazów.", AgentTool.Capability.NETWORK),
        AgentTool("accessibility", "Android UI", "Planowanie działań na interfejsie Androida.", AgentTool.Capability.ANDROID_UI),
        AgentTool("notifications", "Notifications", "Praca z powiadomieniami urządzenia.", AgentTool.Capability.NOTIFICATIONS)
    )

    fun find(id: String): AgentTool? = builtIns.firstOrNull { it.id == id }
}
