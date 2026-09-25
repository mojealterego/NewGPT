package com.mojealterego.newgpt.domain.cognitive

import kotlinx.serialization.Serializable

@Serializable
data class BitemporalInterval(
    val validFrom: Long,
    val validTo: Long? = null,
    val recordedFrom: Long,
    val recordedTo: Long? = null
)

@Serializable
data class CognitiveMemoryItem(
    val id: String,
    val kind: MemoryKind,
    val content: String,
    val source: String,
    val confidence: Float = 0.5f,
    val interval: BitemporalInterval,
    val concepts: List<String> = emptyList(),
    val provenance: List<String> = emptyList(),
    val importance: Float = 0.5f
)

@Serializable
enum class MemoryKind {
    WORKING,
    EPISODIC,
    SEMANTIC,
    PROCEDURAL,
    PROSPECTIVE
}

@Serializable
data class EvidenceItem(
    val id: String,
    val type: EvidenceType,
    val statement: String,
    val source: String? = null,
    val confidence: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class EvidenceType {
    USER_INPUT,
    MEMORY,
    DOCUMENT,
    TOOL_RESULT,
    MODEL_INFERENCE,
    VERIFICATION,
    ASSUMPTION
}

@Serializable
data class DecisionNode(
    val id: String,
    val label: String,
    val kind: DecisionKind,
    val parentIds: List<String> = emptyList(),
    val confidence: Float = 0.5f,
    val cost: Float = 0f,
    val risk: Float = 0f
)

@Serializable
enum class DecisionKind {
    GOAL,
    HYPOTHESIS,
    PLAN,
    ACTION,
    OBSERVATION,
    VERIFICATION,
    REFLECTION,
    RESULT
}

@Serializable
data class MutationCandidate(
    val id: String,
    val parentId: String,
    val genotype: Map<String, String>,
    val hypothesis: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: MutationStatus = MutationStatus.PROPOSED
)

@Serializable
enum class MutationStatus {
    PROPOSED,
    SANDBOXED,
    EVALUATED,
    ACCEPTED,
    REJECTED
)
