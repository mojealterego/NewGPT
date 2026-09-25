package com.mojealterego.newgpt.domain.cognitive

import javax.inject.Inject
import javax.inject.Singleton

enum class ModelTier {
    LOCAL_FAST,
    LOCAL_DEEP,
    CLOUD_FAST,
    CLOUD_DEEP,
    SPECIALIST
}

data class InferenceRequest(
    val promptTokens: Int,
    val taskComplexity: Float,
    val latencySensitive: Boolean,
    val offline: Boolean,
    val requiresTools: Boolean,
    val requiresLongContext: Boolean
)

data class InferencePlan(
    val tier: ModelTier,
    val maxContextTokens: Int,
    val speculativeDecoding: Boolean,
    val verification: Boolean
)

@Singleton
class ModelRouter @Inject constructor() {
    fun route(request: InferenceRequest): InferencePlan {
        val complexity = request.taskComplexity.coerceIn(0f, 1f)
        val tier = when {
            request.offline && complexity < 0.55f -> ModelTier.LOCAL_FAST
            request.offline -> ModelTier.LOCAL_DEEP
            request.requiresTools && complexity > 0.7f -> ModelTier.CLOUD_DEEP
            request.latencySensitive -> ModelTier.CLOUD_FAST
            complexity > 0.8f -> ModelTier.CLOUD_DEEP
            else -> ModelTier.CLOUD_FAST
        }
        return InferencePlan(
            tier = tier,
            maxContextTokens = when {
                request.requiresLongContext -> 32768
                complexity > 0.75f -> 16384
                else -> 8192
            }.coerceAtLeast(request.promptTokens + 512),
            speculativeDecoding = tier == ModelTier.LOCAL_FAST || tier == ModelTier.CLOUD_FAST,
            verification = request.requiresTools || complexity > 0.65f
        )
    }
}
