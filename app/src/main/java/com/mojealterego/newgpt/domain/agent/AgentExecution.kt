package com.mojealterego.newgpt.domain.agent

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AgentExecutionNode(
    val id: String = UUID.randomUUID().toString(),
    val agentId: String,
    val dependsOn: List<String> = emptyList(),
    val input: String,
    val maxRetries: Int = 1
)

@Serializable
enum class AgentNodeStatus { PENDING, RUNNING, SUCCEEDED, FAILED, BLOCKED, CANCELLED }

@Serializable
data class AgentNodeResult(
    val nodeId: String,
    val agentId: String,
    val status: AgentNodeStatus,
    val output: String = "",
    val error: String? = null,
    val attempts: Int = 0,
    val startedAt: Long? = null,
    val finishedAt: Long? = null
)

@Serializable
data class AgentExecutionPlan(
    val id: String = UUID.randomUUID().toString(),
    val nodes: List<AgentExecutionNode>,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExecutionReport(
    val plan: AgentExecutionPlan,
    val results: List<AgentNodeResult>,
    val success: Boolean,
    val durationMs: Long
)
