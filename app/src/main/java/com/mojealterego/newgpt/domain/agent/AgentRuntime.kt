package com.mojealterego.newgpt.domain.agent

import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class AgentRunResult(val agentId: String, val response: String)

class AgentRuntime @Inject constructor(
    private val agents: AgentRepository,
    private val chat: ChatRepository
) {
    suspend fun run(
        agentId: String,
        conversationId: String,
        prompt: String,
        config: ProviderConfig
    ): AgentRunResult {
        val agent = agents.get(agentId) ?: error("Nie znaleziono agenta: $agentId")
        check(agent.enabled) { "Agent jest wyłączony: ${agent.name}" }
        val response = chat.sendAgentMessage(conversationId, prompt, config, agent.systemPrompt)
        return AgentRunResult(agentId, response)
    }

    suspend fun runPipeline(
        agentIds: List<String>,
        conversationId: String,
        prompt: String,
        config: ProviderConfig
    ): List<AgentRunResult> {
        require(agentIds.isNotEmpty()) { "Pipeline wymaga co najmniej jednego agenta." }

        val registry = agents.agents.first()
        val graph = AgentGraphValidator.validateGraph(registry)
        check(graph.valid) { graph.errors.joinToString(" ") }

        val byId = registry.associateBy { it.id }
        val ordered = LinkedHashSet<String>()

        fun expand(id: String, depth: Int) {
            require(depth <= 16) { "Pipeline przekracza maksymalną głębokość 16." }
            if (!ordered.add(id)) return
            val agent = byId[id] ?: error("Nie znaleziono agenta w pipeline: $id")
            check(agent.enabled) { "Agent jest wyłączony: ${agent.name}" }
            agent.handoffs.forEach { expand(it, depth + 1) }
        }

        agentIds.forEach { expand(it, 0) }

        var current = prompt
        val results = mutableListOf<AgentRunResult>()
        for ((index, id) in ordered.withIndex()) {
            val instruction = if (index == 0) {
                current
            } else {
                """Kontynuujesz pracę po poprzednim agencie.
Twoje zadanie: $current

Poprzedni rezultat:
${results.last().response}

Zweryfikuj rezultat, popraw błędy i zwróć ulepszoną wersję."""
            }
            val result = run(id, conversationId, instruction, config)
            results += result
            current = result.response
        }
        return results
    }
}
