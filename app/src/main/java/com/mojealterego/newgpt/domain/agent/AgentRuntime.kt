package com.mojealterego.newgpt.domain.agent

import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.repository.ChatRepository
import javax.inject.Inject

data class AgentRunResult(val agentId: String, val response: String)

class AgentRuntime @Inject constructor(
    private val agents: AgentRepository,
    private val chat: ChatRepository
) {
    suspend fun run(agentId: String, conversationId: String, prompt: String, config: ProviderConfig): AgentRunResult {
        val agent = agents.get(agentId) ?: error("Nie znaleziono agenta: $agentId")
        check(agent.enabled) { "Agent jest wyłączony: ${agent.name}" }
        val response = chat.sendAgentMessage(conversationId, prompt, config, agent.systemPrompt)
        return AgentRunResult(agentId, response)
    }

    suspend fun runPipeline(agentIds: List<String>, conversationId: String, prompt: String, config: ProviderConfig): List<AgentRunResult> {
        require(agentIds.isNotEmpty()) { "Pipeline wymaga co najmniej jednego agenta." }
        var current = prompt
        val results = mutableListOf<AgentRunResult>()
        for ((index, id) in agentIds.withIndex()) {
            val instruction = if (index == 0) current else """Kontynuujesz pracę po poprzednim agencie.
Twoje zadanie: $current

Poprzedni rezultat:
${results.last().response}

Zweryfikuj rezultat, popraw błędy i zwróć ulepszoną wersję."""
            val result = run(id, conversationId, instruction, config)
            results += result
            current = result.response
        }
        return results
    }
}
