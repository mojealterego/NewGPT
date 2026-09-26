package com.mojealterego.newgpt.domain.cognitive

import javax.inject.Inject
import javax.inject.Singleton

data class VerificationReport(
    val accepted: Boolean,
    val score: Float,
    val reasons: List<String>
)

@Singleton
class Supervisor @Inject constructor(
    private val evidence: EvidenceLedger
) {
    fun verify(
        goal: String,
        output: String,
        requiredEvidence: Int = 0,
        riskThreshold: Float = 0.75f
    ): VerificationReport {
        val clean = output.trim()
        val reasons = mutableListOf<String>()
        if (clean.isBlank()) reasons += "empty_output"
        if (clean.length > 200_000) reasons += "output_too_large"
        val evidenceCount = evidence.snapshot().takeLast(500).count {
            it.type == EvidenceType.DOCUMENT ||
            it.type == EvidenceType.TOOL_RESULT ||
            it.type == EvidenceType.VERIFICATION
        }
        if (evidenceCount < requiredEvidence) reasons += "insufficient_evidence"
        val risk = if (goal.isBlank()) 1f else 0f
        if (risk > riskThreshold) reasons += "risk_threshold"
        val score = (
            (if (clean.isNotBlank()) 0.4f else 0f) +
            (if (clean.length in 1..200_000) 0.2f else 0f) +
            (if (evidenceCount >= requiredEvidence) 0.3f else 0f) +
            (if (risk <= riskThreshold) 0.1f else 0f)
        ).coerceIn(0f, 1f)
        return VerificationReport(reasons.isEmpty(), score, reasons)
    }
}
