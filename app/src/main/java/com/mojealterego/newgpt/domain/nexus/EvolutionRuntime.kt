package com.mojealterego.newgpt.domain.nexus

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.random.Random

@Serializable
data class DigitalGenotype(
    val id: String = "genotype:" + UUID.randomUUID(),
    val genes: Map<String, String> = emptyMap(),
    val generation: Int = 0
)

@Serializable
data class Mutation(
    val id: String = "mutation:" + UUID.randomUUID(),
    val targetGene: String,
    val before: String,
    val after: String,
    val rationale: String
)

@Serializable
data class EvolutionCandidate(
    val genotype: DigitalGenotype,
    val mutations: List<Mutation>,
    val score: Float = 0f,
    val status: String = "PROPOSED"
)

@Serializable
data class EvolutionEvaluation(
    val candidateId: String,
    val score: Float,
    val passed: Boolean,
    val tests: List<String>,
    val redTeamFindings: List<String>
)

/**
 * AlphaEvolve/DGM-inspired application layer:
 * genotype -> bounded mutation -> evaluator -> archive.
 * No autonomous production self-modification or deployment is performed.
 */
class EvolutionEngine(private val random: Random = Random(42)) {
    fun mutate(
        parent: DigitalGenotype,
        targetGene: String,
        alternatives: List<String>,
        rationale: String
    ): EvolutionCandidate {
        val before = parent.genes[targetGene].orEmpty()
        val choices = alternatives.filter { it != before }
        val after = choices.randomOrNull(random) ?: before
        val child = parent.copy(
            id = "genotype:" + UUID.randomUUID(),
            genes = parent.genes + (targetGene to after),
            generation = parent.generation + 1
        )
        return EvolutionCandidate(
            genotype = child,
            mutations = listOf(
                Mutation(
                    targetGene = targetGene,
                    before = before,
                    after = after,
                    rationale = rationale
                )
            )
        )
    }

    fun evaluate(
        candidate: EvolutionCandidate,
        tests: List<Pair<String, () -> Boolean>>,
        redTeam: List<Pair<String, () -> Boolean>>
    ): EvolutionEvaluation {
        val testResults = tests.map { pair ->
            val passed = runCatching { pair.second() }.getOrDefault(false)
            pair.first + "=" + passed
        }
        val findings = redTeam.mapNotNull { pair ->
            val triggered = runCatching { pair.second() }.getOrDefault(true)
            if (triggered) pair.first + "=FAIL" else null
        }
        val passedTests = testResults.count { it.endsWith("=true") }
        val score = (
            passedTests.toFloat() / tests.size.coerceAtLeast(1) * 0.8f +
                if (findings.isEmpty()) 0.2f else 0f
            ).coerceIn(0f, 1f)
        return EvolutionEvaluation(
            candidate.genotype.id,
            score,
            testResults.all { it.endsWith("=true") } && findings.isEmpty(),
            testResults,
            findings
        )
    }

    fun approve(evaluation: EvolutionEvaluation, humanApproved: Boolean): Boolean =
        humanApproved && evaluation.passed && evaluation.score >= 0.8f
}
