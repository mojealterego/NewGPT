package com.mojealterego.newgpt.domain.cognitive

import com.mojealterego.newgpt.domain.agent.AgentTool
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ToolExecutionRequest(
    val executionId: String = UUID.randomUUID().toString(),
    val actor: String,
    val tool: AgentTool,
    val arguments: Map<String, String>,
    val explicitApproval: Boolean = false,
    val trustedContext: Boolean = false
)

data class ToolExecutionResult(
    val executionId: String,
    val status: CapabilityStatus,
    val output: String = "",
    val reason: String
)

interface ToolAdapter {
    val toolId: String
    suspend fun execute(arguments: Map<String, String>): String
}

@Singleton
class ToolExecutionGateway @Inject constructor(
    private val broker: CapabilityBroker,
    private val audit: ExecutionAudit
) {
    private val adapters = LinkedHashMap<String, ToolAdapter>()

    @Synchronized
    fun register(adapter: ToolAdapter) {
        adapters[adapter.toolId] = adapter
    }

    suspend fun execute(request: ToolExecutionRequest): ToolExecutionResult {
        val authorization = broker.authorize(
            CapabilityRequest(
                requestId = request.executionId,
                toolId = request.tool.id,
                arguments = request.arguments,
                userApproved = request.explicitApproval,
                trustedContext = request.trustedContext
            )
        )
        audit.record(AuditEvent(
            executionId = request.executionId, actor = request.actor,
            action = "authorize_tool", capability = request.tool.id,
            decision = authorization.status.name, detail = authorization.message
        ))
        if (authorization.status != CapabilityStatus.ALLOWED) {
            return ToolExecutionResult(request.executionId, authorization.status,
                reason = authorization.message)
        }
        val adapter = synchronized(this) { adapters[request.tool.id] }
            ?: return ToolExecutionResult(request.executionId, CapabilityStatus.DENIED,
                reason = "No adapter registered.")
        return runCatching {
            val output = adapter.execute(request.arguments)
            audit.record(AuditEvent(
                executionId = request.executionId, actor = request.actor,
                action = "tool_completed", capability = request.tool.id, decision = "SUCCEEDED"
            ))
            ToolExecutionResult(request.executionId, CapabilityStatus.ALLOWED, output, "executed")
        }.getOrElse { error ->
            audit.record(AuditEvent(
                executionId = request.executionId, actor = request.actor,
                action = "tool_failed", capability = request.tool.id, decision = "FAILED",
                detail = error.message ?: "unknown"
            ))
            ToolExecutionResult(request.executionId, CapabilityStatus.DENIED,
                reason = error.message ?: "Tool execution failed.")
        }
    }
}
