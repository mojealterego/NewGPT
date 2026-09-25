package com.mojealterego.newgpt.domain.nexus

import com.mojealterego.newgpt.data.local.GoTStore
import com.mojealterego.newgpt.data.local.LocalRagStore
import com.mojealterego.newgpt.data.local.TitansMemoryStore

data class Rag2Result(
    val query: String,
    val chunks: List<String>,
    val scores: List<Float>,
    val usedHybridRetrieval: Boolean
)

/**
 * RAG 2.0 facade over the existing local hybrid RAG store.
 * It adds a deterministic post-retrieval relevance gate without changing
 * the underlying source/provenance representation.
 */
class Rag2Pipeline(
    private val rag: LocalRagStore
) {
    suspend fun retrieve(query: String, topK: Int = 8): Rag2Result {
        val docs = rag.retrieve(query, topK.coerceIn(1, 20))
        val queryTerms = query.lowercase().split(" ").filter { it.length >= 3 }
        val ranked = docs.map { doc ->
            val lexical = queryTerms.count { term ->
                doc.text.contains(term, ignoreCase = true)
            }.toFloat()
            doc to lexical
        }.sortedByDescending { it.second }

        return Rag2Result(
            query = query,
            chunks = ranked.map { it.first.text },
            scores = ranked.map { it.second },
            usedHybridRetrieval = true
        )
    }
}

class GoTEngine(private val store: GoTStore) {
    suspend fun stage(inputId: String, name: String, summary: String, score: Float = 0.5f): String =
        store.appendStage(inputId, name, summary, score)
}

/**
 * R3-Titans controller: a bounded application policy around the existing
 * Titans-inspired memory store. It does not claim the neural Titans model.
 */
class R3TitansController(private val store: TitansMemoryStore) {
    suspend fun consolidate(text: String, importance: Float = 0.6f): Boolean =
        store.consolidateWithDecay(text, importance)
}

data class MutationLoopResult(
    val generations: Int,
    val accepted: Boolean,
    val lastScore: Float,
    val reason: String
)

class MutationLoop(private val engine: EvolutionEngine) {
    fun run(
        parent: DigitalGenotype,
        targetGene: String,
        alternatives: List<String>,
        generations: Int,
        evaluator: (EvolutionCandidate) -> EvolutionEvaluation,
        humanApproval: Boolean
    ): MutationLoopResult {
        var current = parent
        var lastScore = 0f
        var accepted = false

        repeat(generations.coerceIn(1, 8)) {
            val candidate = engine.mutate(
                current,
                targetGene,
                alternatives,
                "bounded mutation loop generation " + (it + 1)
            )
            val evaluation = evaluator(candidate)
            lastScore = evaluation.score
            if (engine.approve(evaluation, humanApproval)) {
                current = candidate.genotype
                accepted = true
            }
        }

        return MutationLoopResult(
            generations = generations.coerceIn(1, 8),
            accepted = accepted,
            lastScore = lastScore,
            reason = if (accepted) "Candidate passed evaluator and human gate." else "No candidate passed both gates."
        )
    }
}

enum class ExperimentalConceptStatus {
    EXTENSION_POINT,
    INTERNAL_ALIAS,
    NOT_PUBLIC_STANDARD
}

/**
 * Names from the requested research vocabulary that do not have a single
 * unambiguous public specification in this app. They are tracked explicitly
 * instead of being presented as completed external standards.
 */
data class ExperimentalConcept(
    val name: String,
    val status: ExperimentalConceptStatus,
    val implementation: String
)

object ExperimentalConceptRegistry {
    val concepts = listOf(
        ExperimentalConcept("SHIMI Index", ExperimentalConceptStatus.EXTENSION_POINT, "index adapter contract"),
        ExperimentalConcept("CEV Engine", ExperimentalConceptStatus.EXTENSION_POINT, "constrained evaluation adapter"),
        ExperimentalConcept("GCP", ExperimentalConceptStatus.EXTENSION_POINT, "provider/backend connector slot"),
        ExperimentalConcept("Gödel", ExperimentalConceptStatus.EXTENSION_POINT, "consistency/formal-reasoning adapter slot"),
        ExperimentalConcept("OESI", ExperimentalConceptStatus.EXTENSION_POINT, "evaluation/safety adapter slot"),
        ExperimentalConcept("SEGPA", ExperimentalConceptStatus.EXTENSION_POINT, "agent policy adapter slot"),
        ExperimentalConcept("Agent Devel", ExperimentalConceptStatus.INTERNAL_ALIAS, "bounded agent-development loop"),
        ExperimentalConcept("Retrospective Correction", ExperimentalConceptStatus.INTERNAL_ALIAS, "bitemporal correction"),
        ExperimentalConcept("Point-in-Time Recovery", ExperimentalConceptStatus.INTERNAL_ALIAS, "bitemporal point-in-time query"),
        ExperimentalConcept("Holographic Memory", ExperimentalConceptStatus.INTERNAL_ALIAS, "HDC/VSA primitives")
    )
}
