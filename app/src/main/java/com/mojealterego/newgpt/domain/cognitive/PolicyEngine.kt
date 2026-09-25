package com.mojealterego.newgpt.domain.cognitive

import com.mojealterego.newgpt.domain.agent.AgentTool
import javax.inject.Inject
import javax.inject.Singleton

data class PolicyDecision(
    val allowed: Boolean,
    val requiresApproval: Boolean,
    val reason: String
)

@Singleton
class PolicyEngine @Inject constructor() {
    fun evaluate(
        tool: AgentTool,
        explicitApproval: Boolean = false,
        trustedContext: Boolean = false
    ): PolicyDecision {
        if (tool.capability == AgentTool.Capability.READ_ONLY && !tool.requiresApproval) {
            return PolicyDecision(true, false, "read_only")
        }
        if (tool.capability == AgentTool.Capability.CODE_EXECUTION && !explicitApproval) {
            return PolicyDecision(false, true, "code_execution_requires_approval")
        }
        if (tool.capability == AgentTool.Capability.ANDROID_UI && !explicitApproval) {
            return PolicyDecision(false, true, "device_action_requires_approval")
        }
        if (tool.capability == AgentTool.Capability.NOTIFICATIONS && !explicitApproval) {
            return PolicyDecision(false, true, "notification_action_requires_approval")
        }
        if (tool.capability == AgentTool.Capability.NETWORK && !trustedContext && tool.requiresApproval) {
            return PolicyDecision(false, true, "network_capability_requires_trust_or_approval")
        }
        return PolicyDecision(true, false, "policy_allow")
    }
}
