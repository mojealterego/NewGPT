package com.mojealterego.newgpt.domain.cognitive

import com.mojealterego.newgpt.domain.agent.AgentTool
import javax.inject.Inject
import javax.inject.Singleton

data class CapabilityRequest(
    val requestId: String,
    val toolId: String,
    val arguments: Map<String, String> = emptyMap(),
    val userApproved: Boolean = false,
    val trustedContext: Boolean = false
)

data class CapabilityResult(
    val requestId: String,
    val status: CapabilityStatus,
    val message: String
)

enum class CapabilityStatus { ALLOWED, APPROVAL_REQUIRED, DENIED }

@Singleton
class CapabilityBroker @Inject constructor(
    private val guard: ToolCallGuard
) {
    fun authorize(request: CapabilityRequest): CapabilityResult {
        val tool = AgentToolCatalogBridge.find(request.toolId)
            ?: return CapabilityResult(request.requestId, CapabilityStatus.DENIED, "Unknown capability")

        val decision = guard.check(tool, request.userApproved, request.trustedContext)
        return when {
            decision.allowed -> CapabilityResult(request.requestId, CapabilityStatus.ALLOWED, decision.reason)
            decision.requiresApproval -> CapabilityResult(request.requestId, CapabilityStatus.APPROVAL_REQUIRED, decision.reason)
            else -> CapabilityResult(request.requestId, CapabilityStatus.DENIED, decision.reason)
        }
    }
}

private object AgentToolCatalogBridge {
    fun find(id: String): AgentTool? =
        com.mojealterego.newgpt.domain.agent.AgentToolCatalog.find(id)
}
