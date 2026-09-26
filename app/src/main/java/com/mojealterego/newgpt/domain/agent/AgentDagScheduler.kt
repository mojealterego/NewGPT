package com.mojealterego.newgpt.domain.agent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentDagScheduler @Inject constructor() {
    suspend fun execute(
        plan: AgentExecutionPlan,
        maxParallel: Int = 4,
        executor: suspend (AgentExecutionNode, Map<String, AgentNodeResult>) -> AgentNodeResult
    ): ExecutionReport = coroutineScope {
        require(plan.nodes.isNotEmpty()) { "Execution plan is empty." }
        require(maxParallel in 1..16) { "maxParallel must be 1..16." }
        validate(plan)

        val started = System.currentTimeMillis()
        val results = ConcurrentHashMap<String, AgentNodeResult>()
        val pending = plan.nodes.associateBy { it.id }.toMutableMap()
        val running = mutableSetOf<String>()

        while (pending.isNotEmpty()) {
            val ready = pending.values.filter { node ->
                node.dependsOn.all { dependency ->
                    results[dependency]?.status == AgentNodeStatus.SUCCEEDED
                }
            }
            val blocked = pending.values.filter { node ->
                node.dependsOn.any { dependency ->
                    results[dependency]?.status == AgentNodeStatus.FAILED ||
                    results[dependency]?.status == AgentNodeStatus.BLOCKED ||
                    results[dependency]?.status == AgentNodeStatus.CANCELLED
                }
            }
            blocked.forEach { node ->
                results[node.id] = AgentNodeResult(
                    nodeId = node.id, agentId = node.agentId,
                    status = AgentNodeStatus.BLOCKED,
                    error = "Dependency failed or was blocked."
                )
                pending.remove(node.id)
            }
            if (ready.isEmpty()) {
                if (pending.isNotEmpty()) throw IllegalStateException("DAG stalled: unresolved dependency.")
                break
            }
            val batch = ready.filter { it.id !in running }.take(maxParallel)
            if (batch.isEmpty()) throw IllegalStateException("DAG scheduler made no progress.")
            batch.forEach { running += it.id }
            val batchResults = batch.map { node ->
                async { runWithRetry(node, results.toMap(), executor) }
            }.awaitAll()
            batchResults.forEach {
                results[it.nodeId] = it
                running.remove(it.nodeId)
                pending.remove(it.nodeId)
            }
        }
        val ordered = plan.nodes.mapNotNull { results[it.id] }
        ExecutionReport(plan, ordered, ordered.all { it.status == AgentNodeStatus.SUCCEEDED },
            System.currentTimeMillis() - started)
    }

    private suspend fun runWithRetry(
        node: AgentExecutionNode,
        results: Map<String, AgentNodeResult>,
        executor: suspend (AgentExecutionNode, Map<String, AgentNodeResult>) -> AgentNodeResult
    ): AgentNodeResult {
        var attempts = 0
        var lastError: Throwable? = null
        while (attempts <= node.maxRetries) {
            attempts++
            val start = System.currentTimeMillis()
            try {
                val result = executor(node, results)
                return result.copy(
                    attempts = attempts,
                    startedAt = result.startedAt ?: start,
                    finishedAt = result.finishedAt ?: System.currentTimeMillis()
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                lastError = error
            }
        }
        return AgentNodeResult(
            nodeId = node.id, agentId = node.agentId,
            status = AgentNodeStatus.FAILED,
            error = lastError?.message ?: "Unknown execution error",
            attempts = attempts
        )
    }

    private fun validate(plan: AgentExecutionPlan) {
        val ids = plan.nodes.map { it.id }
        require(ids.distinct().size == ids.size) { "Duplicate node id." }
        val known = ids.toSet()
        plan.nodes.forEach { node ->
            require(node.agentId.isNotBlank()) { "Agent id is blank." }
            require(node.dependsOn.all { it in known }) { "Unknown dependency." }
            require(node.id !in node.dependsOn) { "Self dependency." }
        }
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        val byId = plan.nodes.associateBy { it.id }
        fun dfs(id: String) {
            if (id in visiting) error("Cycle detected.")
            if (!visited.add(id)) return
            visiting.add(id)
            byId.getValue(id).dependsOn.forEach(::dfs)
            visiting.remove(id)
        }
        plan.nodes.forEach { dfs(it.id) }
    }
}
