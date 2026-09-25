package com.mojealterego.newgpt.domain.nexus

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class DecisionOption(
    val id: String,
    val description: String,
    val evidence: List<String> = emptyList(),
    val score: Float = 0.5f
)

@Serializable
data class DecisionResult(
    val selectedId: String?,
    val confidence: Float,
    val rationale: String,
    val evaluatedOptions: List<DecisionOption>,
    val budgetUsed: Int
)

class DecisionCycle(private val maxOptions: Int = 8) {
    fun evaluate(options: List<DecisionOption>, budget: Int = 4): DecisionResult {
        val bounded = options.take(maxOptions)
        if (bounded.isEmpty()) return DecisionResult(null, 0f, "No viable options.", emptyList(), 0)
        val ranked = bounded.map { option ->
            val evidenceBonus = min(option.evidence.size, 5) * 0.05f
            option.copy(score = (option.score + evidenceBonus).coerceIn(0f, 1f))
        }.sortedByDescending { it.score }
        val best = ranked.first()
        val second = ranked.getOrNull(1)?.score ?: 0f
        val confidence = (0.5f + (best.score - second) * 0.8f).coerceIn(0f, 1f)
        return DecisionResult(
            best.id,
            confidence,
            "Bounded decision using score, evidence and margin.",
            ranked,
            min(budget.coerceAtLeast(0), bounded.size)
        )
    }
}

class ReflexionEngine(private val maxRounds: Int = 3) {
    fun run(
        initial: String,
        evaluator: (String) -> Pair<Float, String>,
        corrector: (String, String) -> String
    ): Pair<String, List<String>> {
        var current = initial
        val notes = mutableListOf<String>()
        repeat(maxRounds.coerceAtLeast(1)) {
            val (score, feedback) = evaluator(current)
            notes += "round=" + (it + 1) + "; score=" + score.coerceIn(0f, 1f) + "; " + feedback
            if (score >= 0.85f) return current to notes
            current = corrector(current, feedback).take(16000)
        }
        return current to notes
    }
}

data class CognitiveModulation(
    val urgency: Float = 0.5f,
    val novelty: Float = 0.5f,
    val risk: Float = 0.2f,
    val depthBudget: Int = 4
) {
    fun adjustedBudget(): Int {
        val base = depthBudget.coerceIn(1, 16)
        return (base + (urgency * 3f).toInt() + (novelty * 2f).toInt() - (risk * 2f).toInt()).coerceIn(1, 16)
    }
}

data class ReasoningRoute(val providerId: String, val subtask: String, val budget: Int)

class R2Router {
    fun route(task: String, providers: List<String>, budget: Int = 4): List<ReasoningRoute> {
        val pool = providers.distinct().take(8)
        if (pool.isEmpty()) return emptyList()
        val parts = task.split('.', '!', '?').filter { it.isNotBlank() }.take(8)
        return parts.mapIndexed { index, part ->
            ReasoningRoute(pool[index % pool.size], part.trim(), max(1, budget / max(1, parts.size)))
        }
    }
}

class AdaptiveSearchController {
    fun nextAction(branchingFactor: Int, depth: Int, rewardVariance: Float): String =
        when {
            branchingFactor < 2 -> "WIDEN"
            rewardVariance > 0.25f -> "EXPLORE"
            depth < 4 -> "DEEPEN"
            else -> "PRUNE"
        }
}
