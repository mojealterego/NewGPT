package com.mojealterego.newgpt.domain.nexus

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Serializable
data class NexusTool(
    val id: String,
    val description: String,
    val sideEffect: Boolean = false,
    val requiresApproval: Boolean = true
)

@Serializable
data class ToolAudit(
    val toolId: String,
    val allowed: Boolean,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GatewayPolicy(
    val allowlist: Set<String> = emptySet(),
    val requireApprovalForSideEffects: Boolean = true,
    val maxCallsPerWindow: Int = 30
)

/**
 * MCP gateway policy layer. It is transport-neutral; a real MCP transport
 * remains an integration boundary rather than being faked inside Android.
 */
class McpGateway(private val policy: GatewayPolicy) {
    private val tools = ConcurrentHashMap<String, NexusTool>()
    private val audits = ArrayDeque<ToolAudit>()
    private val calls = AtomicLong(0)

    fun register(tool: NexusTool) {
        tools[tool.id] = tool
    }

    fun listTools(): List<NexusTool> =
        tools.values.sortedBy { it.id }

    @Synchronized
    fun authorize(toolId: String, humanApproved: Boolean = false): ToolAudit {
        val tool = tools[toolId]
        val reason = when {
            tool == null -> "UNKNOWN_TOOL"
            policy.allowlist.isNotEmpty() && toolId !in policy.allowlist -> "NOT_ALLOWLISTED"
            policy.requireApprovalForSideEffects &&
                tool.sideEffect &&
                tool.requiresApproval &&
                !humanApproved -> "APPROVAL_REQUIRED"
            calls.get() >= policy.maxCallsPerWindow.coerceAtLeast(1) -> "RATE_LIMIT"
            else -> "ALLOWED"
        }

        val allowed = reason == "ALLOWED"
        if (allowed) calls.incrementAndGet()

        val audit = ToolAudit(toolId, allowed, reason)
        audits.addLast(audit)
        while (audits.size > 1000) audits.removeFirst()
        return audit
    }

    @Synchronized
    fun auditLog(): List<ToolAudit> = audits.toList()

    fun resetRateWindow() {
        calls.set(0)
    }
}
