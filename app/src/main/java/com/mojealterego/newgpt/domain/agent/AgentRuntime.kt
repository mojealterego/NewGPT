package com.mojealterego.newgpt.domain.agent

import com.mojealterego.newgpt.data.local.BitemporalMemoryStore
import com.mojealterego.newgpt.domain.cognitive.CheckpointStore
import com.mojealterego.newgpt.domain.cognitive.CognitiveRuntime
import com.mojealterego.newgpt.domain.cognitive.DecisionKind
import com.mojealterego.newgpt.domain.cognitive.ExecutionCheckpoint
import com.mojealterego.newgpt.domain.cognitive.MemoryKind
import com.mojealterego.newgpt.domain.cognitive.Supervisor
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class AgentRunResult(val agentId: String, val response: String)

class AgentRuntime @Inject constructor(
    private val agents: AgentRepository,
    private val chat: ChatRepository,
    private val memory: BitemporalMemoryStore,
    private val cognition: CognitiveRuntime,
    private val scheduler: AgentDagScheduler,
    private val supervisor: Supervisor,
    private val checkpoints: CheckpointStore
) {
    suspend fun run(
        agentId: String,
        conversationId: String,
        prompt: String,
        config: ProviderConfig
    ): AgentRunResult {
        val agent = agents.get(agentId) ?: error("Nie znaleziono agenta: " + agentId)
        check(agent.enabled) { "Agent jest wyłączony: " + agent.name }

        val cycle = cognition.decisionCycle(prompt)
        memory.remember(
            content = "GOAL: " + prompt,
            kind = MemoryKind.WORKING,
            source = "agent:" + agentId,
            confidence = 1f,
            importance = 0.8f
        )

        val relevant = memory.query(prompt, limit = 6)
        val grounding = relevant.joinToString("\n") {
            "[" + it.kind + "] " + it.content
        }.take(12000)

        val enrichedPrompt = if (grounding.isBlank()) {
            prompt
        } else {
            prompt + "\n\nCognitive context:\n" + grounding
        }

        val response = chat.sendAgentMessage(
            conversationId,
            enrichedPrompt,
            config,
            agent.systemPrompt
        )

        memory.remember(
            content = response,
            kind = MemoryKind.EPISODIC,
            source = "agent:" + agentId,
            confidence = 0.65f,
            importance = 0.6f
        )

        cognition.add(
            cycle,
            "Agent " + agentId + " completed generation",
            DecisionKind.RESULT,
            confidence = 0.65f
        )

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
        val selected = LinkedHashSet<String>()

        fun expand(id: String, depth: Int) {
            require(depth <= 16) { "Pipeline przekracza maksymalną głębokość 16." }
            if (!selected.add(id)) return
            val agent = byId[id] ?: error("Nie znaleziono agenta w pipeline: " + id)
            check(agent.enabled) { "Agent jest wyłączony: " + agent.name }
            agent.handoffs.forEach { expand(it, depth + 1) }
        }

        agentIds.forEach { expand(it, 0) }

        val nodes = selected.map { id ->
            val agent = byId.getValue(id)
            AgentExecutionNode(
                id = id,
                agentId = id,
                dependsOn = agent.handoffs.filter { it in selected },
                input = prompt,
                maxRetries = 1
            )
        }
        val plan = AgentExecutionPlan(nodes = nodes)
        checkpoints.save(ExecutionCheckpoint(plan.id, "planned", "nodes=" + nodes.size))

        val report = scheduler.execute(plan, maxParallel = 4) { node, previous ->
            val dependencyOutput = node.dependsOn.mapNotNull { previous[it]?.output }
                .joinToString("\n\n--- DEPENDENCY RESULT ---\n")
            val instruction = if (dependencyOutput.isBlank()) {
                node.input
            } else {
                node.input + "\n\nVerified upstream results:\n" + dependencyOutput
            }
            val result = run(node.agentId, conversationId, instruction, config)
            val verification = supervisor.verify(instruction, result.response)
            checkpoints.save(
                ExecutionCheckpoint(
                    plan.id,
                    "agent:" + node.agentId,
                    "accepted=" + verification.accepted + ";score=" + verification.score
                )
            )
            if (!verification.accepted) {
                AgentNodeResult(
                    node.id, node.agentId, AgentNodeStatus.FAILED,
                    error = "Supervisor rejected output: " + verification.reasons.joinToString(",")
                )
            } else {
                AgentNodeResult(node.id, node.agentId, AgentNodeStatus.SUCCEEDED, result.response)
            }
        }

        checkpoints.save(
            ExecutionCheckpoint(
                plan.id,
                "completed",
                "success=" + report.success + ";durationMs=" + report.durationMs
            )
        )
        return report.results.mapNotNull { node ->
            if (node.status == AgentNodeStatus.SUCCEEDED) {
                AgentRunResult(node.agentId, node.output)
            } else {
                null
            }
        }
    }
}
