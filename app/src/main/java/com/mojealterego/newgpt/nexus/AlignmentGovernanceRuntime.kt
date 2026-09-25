package com.mojealterego.newgpt.nexus

import java.security.MessageDigest

data class ConstitutionalRule(
    val id: String,
    val statement: String,
    val prohibitedActions: Set<String> = emptySet(),
    val requiredControls: Set<String> = emptySet()
)

data class PolicySnapshot(
    val version: Long,
    val rules: List<ConstitutionalRule>,
    val digest: String
)

data class MutationCandidate(
    val id: String,
    val changedComponents: Set<String>,
    val requestedActions: Set<String>,
    val expectedBenefits: Set<String>
)

data class FormalInvariantResult(
    val accepted: Boolean,
    val violations: List<String>,
    val beforeDigest: String,
    val afterDigest: String
)

class ConstitutionalGovernance(private var snapshot: PolicySnapshot) {

    @Synchronized fun current(): PolicySnapshot = snapshot

    @Synchronized fun verifyMutation(candidate: MutationCandidate): FormalInvariantResult {
        val violations = mutableListOf<String>()
        if (candidate.id.isBlank()) violations += "mutation-id-empty"
        if (candidate.changedComponents.any { it.isBlank() }) violations += "blank-component"
        if (candidate.requestedActions.any { it.isBlank() }) violations += "blank-action"

        snapshot.rules.forEach { rule ->
            val forbidden = candidate.requestedActions.intersect(rule.prohibitedActions)
            if (forbidden.isNotEmpty()) {
                violations += "rule:" + rule.id + ":prohibited:" + forbidden.sorted().joinToString(",")
            }
            rule.requiredControls.forEach { control ->
                if (control !in candidate.expectedBenefits && control !in candidate.changedComponents) {
                    violations += "rule:" + rule.id + ":missing-control:" + control
                }
            }
        }

        val afterDigest = digest(snapshot.version, snapshot.rules)
        return FormalInvariantResult(
            accepted = violations.isEmpty() && afterDigest == snapshot.digest,
            violations = violations,
            beforeDigest = snapshot.digest,
            afterDigest = afterDigest
        )
    }

    @Synchronized fun rotateConstitution(
        newVersion: Long,
        rules: List<ConstitutionalRule>,
        humanApprovalDigest: String
    ): PolicySnapshot {
        require(newVersion > snapshot.version) { "Constitution versions must increase monotonically" }
        require(humanApprovalDigest == digest(newVersion, rules)) {
            "Constitution rotation requires an exact human-approved digest"
        }
        snapshot = PolicySnapshot(newVersion, rules.toList(), humanApprovalDigest)
        return snapshot
    }

    companion object {
        fun create(version: Long, rules: List<ConstitutionalRule>): ConstitutionalGovernance =
            ConstitutionalGovernance(PolicySnapshot(version, rules.toList(), digest(version, rules)))

        private fun digest(version: Long, rules: List<ConstitutionalRule>): String {
            val canonical = buildString {
                append(version).append('|')
                rules.sortedBy { it.id }.forEach { r ->
                    append(r.id).append('|').append(r.statement).append('|')
                    r.prohibitedActions.sorted().forEach { append(it).append(',') }
                    append('|')
                    r.requiredControls.sorted().forEach { append(it).append(',') }
                    append(';')
                }
            }
            return MessageDigest.getInstance("SHA-256")
                .digest(canonical.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
    }
}

data class EpistemicObservation(
    val confidence: Double,
    val evidenceCount: Int,
    val contradictions: List<String>,
    val reversible: Boolean,
    val actionAllowed: Boolean
)

class EpistemicHumilityController(
    private val minimumConfidence: Double = 0.80
) {
    fun inspect(
        confidence: Double,
        evidenceCount: Int,
        contradictions: List<String>,
        reversible: Boolean
    ): EpistemicObservation {
        val c = confidence.coerceIn(0.0, 1.0)
        val allowed = c >= minimumConfidence && contradictions.isEmpty() && reversible
        return EpistemicObservation(c, evidenceCount.coerceAtLeast(0), contradictions.take(32), reversible, allowed)
    }
}

data class PreferenceFeedback(
    val promptHash: String,
    val preferredOutputHash: String,
    val rejectedOutputHash: String,
    val reason: String,
    val timestampMs: Long
)

class PreferenceLearningLedger(private val maxEntries: Int = 10_000) {
    private val entries = ArrayDeque<PreferenceFeedback>()

    @Synchronized fun record(prompt: String, preferred: String, rejected: String, reason: String) {
        entries.addLast(
            PreferenceFeedback(
                sha256(prompt),
                sha256(preferred),
                sha256(rejected),
                reason.take(2000),
                System.currentTimeMillis()
            )
        )
        while (entries.size > maxEntries) entries.removeFirst()
    }

    @Synchronized fun recent(limit: Int = 100): List<PreferenceFeedback> =
        entries.asReversed().take(limit.coerceIn(1, maxEntries))

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
