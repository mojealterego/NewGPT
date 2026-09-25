package com.mojealterego.newgpt.domain.cognitive

import javax.inject.Inject
import javax.inject.Singleton

data class RuntimeBudget(
    val maxTokens: Int,
    val maxToolCalls: Int,
    val maxRetries: Int,
    val maxParallelBranches: Int,
    val deadlineMs: Long
)

@Singleton
class RuntimeBudgetController @Inject constructor() {
    fun budget(complexity: Float, latencySensitive: Boolean): RuntimeBudget {
        val c = complexity.coerceIn(0f, 1f)
        return RuntimeBudget(
            maxTokens = if (latencySensitive) 2048 else (4096 + (c * 12288).toInt()),
            maxToolCalls = 2 + (c * 8).toInt(),
            maxRetries = if (c > 0.7f) 3 else 1,
            maxParallelBranches = if (c > 0.8f) 4 else 2,
            deadlineMs = if (latencySensitive) 15_000 else 90_000
        )
    }
}
