package com.mojealterego.newgpt.domain.assistant

import com.mojealterego.newgpt.domain.integration.ConnectedAppRegistry

data class AssistantVoiceProfile(
    val language: String = "pl-PL",
    val description: String = "",
    val realtimeVoice: String = "marin",
    val speakingStyle: String = "",
    val personality: String = ""
)

data class PaulaAssistant(
    val id: String = "paula",
    val name: String = "Paula",
    val title: String = "Wirtualna Asystentka NewGPT",
    val voice: AssistantVoiceProfile = AssistantVoiceProfile(),
    val toolIds: Set<String> = emptySet(),
    val connectedAppIds: Set<String> = emptySet(),
    val videoAssetIds: List<String> = emptyList(),
    val systemInstructions: String = """
        Jesteś Paulą — wirtualną asystentką NewGPT.
        Koordynujesz rozmowę, pamięć, narzędzia, agentów i połączone aplikacje.
        Dla działań zapisywanych, finansowych, komunikacyjnych lub wrażliwych
        wymagających zgody przedstaw krótki plan i poproś o potwierdzenie.
        Nie deklaruj wykonania działania bez potwierdzonego wyniku narzędzia.
    """.trimIndent()
) {
    val hasAllConnectedApps: Boolean
        get() = connectedAppIds.containsAll(ConnectedAppRegistry.catalog.map { it.id }.toSet())
}
