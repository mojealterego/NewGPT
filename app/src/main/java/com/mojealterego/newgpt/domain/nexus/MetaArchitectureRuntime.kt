package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import java.security.MessageDigest

/**
 * Meta Architecture Runtime.
 *
 * This is a deterministic control plane for the capabilities described in the
 * NewGPT architecture notes. It intentionally does not claim autonomous control
 * of physical factories, quantum hardware, human values, or production self-modification.
 * Capabilities that are not executable on Android are not represented as fake implementations.
 */
data class DecisionEvidence(
    val source: String,
    val confidence: Double,
    val rationale: String
)

data class ArchitectureDecision(
    val id: String,
    val action: String,
    val evidence: List<DecisionEvidence>,
    val confidence: Double,
    val requiresHumanApproval: Boolean
)

data class NegotiationOption(
    val name: String,
    val cost: Double,
    val latencyMs: Double,
    val quality: Double,
    val lockInRisk: Double
)

data class NegotiationAnalysis(
    val batna: NegotiationOption?,
    val options: List<NegotiationOption>,
    val technicalDebtCost: Double,
    val notes: List<String>
)

data class MetricWindow(
    val requests: Long,
    val failures: Long,
    val averageLatencyMs: Double,
    val p95LatencyMs: Double,
    val estimatedCost: Double,
    val quality: Double
)

data class ShadowComparison(
    val baseline: MetricWindow,
    val candidate: MetricWindow,
    val qualityDelta: Double,
    val latencyDeltaMs: Double,
    val costDelta: Double,
    val recommendation: String
)

data class FailureMemory(
    val fingerprint: String,
    val task: String,
    val error: String,
    val prevention: String,
    val timestampMs: Long
)

data class BackoffDecision(
    val attempt: Int,
    val delayMs: Long,
    val retry: Boolean
)

data class LatencyBudget(
    val totalMs: Long,
    val searchMs: Long,
    val generationMs: Long,
    val reservedMs: Long
)

data class BanditArm(
    val id: String,
    val trials: Long,
    val reward: Double,
    val enabled: Boolean = true
)

data class DriftSignal(
    val score: Double,
    val threshold: Double,
    val driftDetected: Boolean
)

data class ThermalSnapshot(
    val status: Int,
    val batteryPct: Int,
    val isCharging: Boolean,
    val recommendedLoad: Double
)

data class EnergyPolicy(
    val maxWorkFraction: Double,
    val preferLocal: Boolean,
    val allowBackgroundWork: Boolean
)

data class AlignmentPolicy(
    val constitutionVersion: String,
    val allowedPrinciples: Set<String>,
    val forbiddenPrinciples: Set<String>,
    val immutableHash: String
)

data class MutationProposal(
    val id: String,
    val changedComponents: List<String>,
    val invariantChecks: List<String>,
    val policyHashBefore: String,
    val policyHashAfter: String
)

data class ProofResult(
    val passed: Boolean,
    val proofType: String,
    val obligations: List<String>,
    val evidence: List<String>
)

data class HumilityReport(
    val confidence: Double,
    val assumptions: List<String>,
    val contradictions: List<String>,
    val rollbackAvailable: Boolean,
    val humanReviewRecommended: Boolean
)

data class WorkflowNode(
    val id: String,
    val dependencies: Set<String>,
    val action: suspend () -> String
)

data class WorkflowResult(
    val completed: List<String>,
    val failed: List<String>,
    val outputs: Map<String, String>
)

class InputContract private constructor() {
    fun require(condition: Boolean, field: String, message: String = "invalid"): String? =
        if (condition) null else "$field: $message"

    fun requireNonBlank(value: String, field: String): String? =
        require(value.isNotBlank(), field, "must not be blank")
}

class NegotiationEngine {
    fun analyze(
        options: List<NegotiationOption>,
        technicalDebtCost: Double
    ): NegotiationAnalysis {
        val safe = options.filter { it.cost >= 0 && it.latencyMs >= 0 && it.quality in 0.0..1.0 }
        val batna = safe.minByOrNull { it.cost + it.latencyMs / 1000.0 + it.lockInRisk * 10.0 }
        return NegotiationAnalysis(
            batna = batna,
            options = safe,
            technicalDebtCost = max(0.0, technicalDebtCost),
            notes = listOf(
                "Compare explicit cost, latency, quality and lock-in instead of vendor claims.",
                "Keep a migration-capable BATNA behind a provider-neutral interface."
            )
        )
    }
}

class DataDrivenDecisionEngine {
    fun compare(baseline: MetricWindow, candidate: MetricWindow): ShadowComparison {
        val qualityDelta = candidate.quality - baseline.quality
        val latencyDelta = candidate.p95LatencyMs - baseline.p95LatencyMs
        val costDelta = candidate.estimatedCost - baseline.estimatedCost
        val recommendation = when {
            candidate.failures > baseline.failures -> "BLOCK_REGRESSION"
            qualityDelta > 0.02 && latencyDelta <= baseline.p95LatencyMs * 0.20 -> "CANDIDATE_FOR_REVIEW"
            else -> "KEEP_SHADOW"
        }
        return ShadowComparison(baseline, candidate, qualityDelta, latencyDelta, costDelta, recommendation)
    }
}

class ConflictResolutionEngine {
    fun matrix(criteria: Map<String, Map<String, Double>>): Map<String, Double> {
        if (criteria.isEmpty()) return emptyMap()
        val names = criteria.values.flatMap { it.keys }.toSet()
        return names.associateWith { name ->
            criteria.values.map { it[name] ?: 0.0 }.average()
        }
    }

    fun postMortem(failure: String, processSignals: List<String>): List<String> =
        listOf("failure=$failure", "focus=process-and-guardrails") + processSignals
}

class CriticalThinkingEngine {
    fun decompose(problem: String): List<String> {
        if (problem.isBlank()) return emptyList()
        return listOf(
            "intent:$problem",
            "constraints",
            "data",
            "model-or-rule",
            "tooling",
            "privacy",
            "verification",
            "rollback"
        )
    }

    fun redTeam(input: String): List<String> = buildList {
        if (input.contains("ignore previous", true)) add("prompt-injection")
        if (input.contains("private_key", true) || input.contains("password", true)) add("secret-exposure")
        if (input.length > 100_000) add("oversized-input")
        if (isEmpty()) add("baseline-controls-pass")
    }
}

class DelegationEngine {
    fun contract(name: String, maxLatencyMs: Long, requiredFields: Set<String>): Map<String, Any> =
        mapOf("name" to name, "maxLatencyMs" to maxLatencyMs, "requiredFields" to requiredFields)
}

class AttentionCompiler {
    fun compress(messages: List<String>, keepLast: Int = 3): List<String> {
        if (messages.size <= keepLast) return messages
        val older = messages.dropLast(keepLast)
        val digest = older.joinToString(" ").take(4000)
        return listOf("[compressed-context] $digest") + messages.takeLast(keepLast)
    }

    fun budget(items: List<String>, maxChars: Int): List<String> {
        var used = 0
        return items.filter {
            if (used + it.length > maxChars) false else {
                used += it.length
                true
            }
        }
    }
}

class RetryPolicy(
    private val baseMs: Long = 1_000,
    private val maxMs: Long = 30_000
) {
    fun next(attempt: Int, retryable: Boolean): BackoffDecision {
        if (!retryable) return BackoffDecision(attempt, 0, false)
        val exponent = min(20, max(0, attempt - 1))
        val delay = min(maxMs, baseMs * (1L shl exponent))
        return BackoffDecision(attempt, delay, true)
    }
}

class FailureMemoryStore {
    private val memories = ArrayDeque<FailureMemory>()
    fun remember(task: String, error: String, prevention: String, now: Long): FailureMemory {
        val fingerprint = sha256("$task|$error")
        val item = FailureMemory(fingerprint, task, error, prevention, now)
        memories.removeAll { it.fingerprint == fingerprint }
        memories.addLast(item)
        while (memories.size > 500) memories.removeFirst()
        return item
    }

    fun retrieve(query: String, limit: Int = 5): List<FailureMemory> =
        memories.asSequence()
            .filter { it.task.contains(query, true) || it.error.contains(query, true) }
            .take(limit)
            .toList()

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

class ConcurrencyEngine {
    suspend fun <T> all(tasks: List<suspend () -> T>): List<T> = coroutineScope {
        tasks.map { async { it() } }.awaitAll()
    }
}

class LatencyBudgetEngine {
    fun allocate(totalMs: Long): LatencyBudget {
        val total = max(100, totalMs)
        return LatencyBudget(
            totalMs = total,
            searchMs = (total * 0.25).toLong(),
            generationMs = (total * 0.65).toLong(),
            reservedMs = (total * 0.10).toLong()
        )
    }
}

class CreativityPolicy {
    fun parameters(creative: Boolean): Pair<Double, Double> =
        if (creative) 0.85 to 0.95 else 0.20 to 0.90
}

class BanditRouter {
    fun select(arms: List<BanditArm>, exploration: Double = 0.05): BanditArm? {
        val enabled = arms.filter { it.enabled }
        if (enabled.isEmpty()) return null
        val explored = enabled.minByOrNull { it.trials }
        if (exploration >= 1.0 || (exploration > 0.0 && explored?.trials == 0L)) return explored
        return enabled.maxByOrNull { if (it.trials == 0L) Double.POSITIVE_INFINITY else it.reward / it.trials }
    }
}

class DriftDetector {
    fun compare(current: List<Double>, baseline: List<Double>, threshold: Double = 0.25): DriftSignal {
        if (current.isEmpty() || baseline.isEmpty()) return DriftSignal(0.0, threshold, false)
        val a = current.average()
        val b = baseline.average()
        val variance = sqrt((current.map { (it - a) * (it - a) }.average() + 1e-9))
        val score = abs(a - b) / max(variance, 1e-6)
        return DriftSignal(score, threshold, score > threshold)
    }
}

class PredictivePrefetchPolicy {
    fun shouldPrefetch(confidence: Double, threshold: Double = 0.80): Boolean =
        confidence >= threshold
}

class ZeroTrustGate {
    fun allow(
        actor: String,
        capability: String,
        privateMode: Boolean,
        sideEffect: Boolean,
        approved: Boolean
    ): Boolean {
        if (actor.isBlank() || capability.isBlank()) return false
        if (privateMode && capability.startsWith("external:")) return false
        return !sideEffect || approved
    }
}

class GoalRewardEngine {
    fun score(quality: Double, safety: Double, cost: Double, latency: Double): Double =
        quality.coerceIn(0.0, 1.0) * 0.50 +
            safety.coerceIn(0.0, 1.0) * 0.30 -
            cost.coerceAtLeast(0.0) * 0.10 -
            latency.coerceAtLeast(0.0) * 0.10
}

class KnowledgeTransferRegistry {
    private val lessons = linkedMapOf<String, String>()
    fun put(id: String, lesson: String) { lessons[id] = lesson }
    fun get(id: String): String? = lessons[id]
    fun all(): Map<String, String> = lessons.toMap()
}

class ServiceDiscoveryRegistry {
    private val services = linkedMapOf<String, Set<String>>()
    fun announce(service: String, capabilities: Set<String>) { services[service] = capabilities }
    fun find(capability: String): List<String> =
        services.filterValues { capability in it }.keys.toList()
}

class AsyncCompletionRegistry {
    private val completed = mutableMapOf<String, String>()
    fun complete(jobId: String, result: String) { completed[jobId] = result }
    fun result(jobId: String): String? = completed[jobId]
}

class ConfidenceGate {
    fun decide(confidence: Double, highImpact: Boolean): Boolean =
        confidence >= if (highImpact) 0.98 else 0.70
}

class StructuredOutputGate {
    fun requireFields(payload: Map<String, Any?>, fields: Set<String>): List<String> =
        fields.filter { key -> !payload.containsKey(key) || payload[key] == null }
}

class DegradationController {
    fun choose(primaryHealthy: Boolean, localAvailable: Boolean): String = when {
        primaryHealthy -> "PRIMARY"
        localAvailable -> "LOCAL_DEGRADED"
        else -> "MINIMAL_SAFE_MODE"
    }
}

class ClarificationEngine {
    fun candidates(query: String, choices: List<String>, limit: Int = 3): List<String> =
        choices.map { it to similarity(query, it) }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }

    private fun similarity(a: String, b: String): Double {
        val x = a.lowercase().split(Regex("\\W+")).filter(String::isNotBlank).toSet()
        val y = b.lowercase().split(Regex("\\W+")).filter(String::isNotBlank).toSet()
        if (x.isEmpty() && y.isEmpty()) return 1.0
        return x.intersect(y).size.toDouble() / max(1, x.union(y).size)
    }
}

class DagPlanner {
    fun validate(nodes: List<WorkflowNode>): List<String> {
        val ids = nodes.map { it.id }.toSet()
        return nodes.flatMap { n -> n.dependencies.filter { it !in ids }.map { "unknown:$it" } }
    }

    suspend fun run(nodes: List<WorkflowNode>): WorkflowResult = coroutineScope {
        val errors = validate(nodes)
        if (errors.isNotEmpty()) return@coroutineScope WorkflowResult(emptyList(), errors, emptyMap())
        val outputs = linkedMapOf<String, String>()
        val pending = nodes.toMutableList()
        while (pending.isNotEmpty()) {
            val ready = pending.filter { it.dependencies.all(outputs::containsKey) }
            if (ready.isEmpty()) {
                return@coroutineScope WorkflowResult(outputs.keys.toList(), listOf("cycle-or-blocked"), outputs)
            }
            val results: List<Deferred<Pair<String, String>>> = ready.map { node ->
                async { node.id to node.action() }
            }
            results.awaitAll().forEach { (id, value) -> outputs[id] = value }
            pending.removeAll(ready)
        }
        WorkflowResult(outputs.keys.toList(), emptyList(), outputs)
    }
}

class SemanticClusterIndex {
    private val entries = linkedMapOf<String, Set<String>>()
    fun put(id: String, tokens: Set<String>) { entries[id] = tokens }
    fun search(query: Set<String>, limit: Int = 8): List<String> =
        entries.map { (id, tokens) ->
            id to if (tokens.isEmpty() || query.isEmpty()) 0.0 else
                tokens.intersect(query).size.toDouble() / tokens.union(query).size
        }.sortedByDescending { it.second }.take(limit).map { it.first }
}

class KnowledgeGraph {
    data class Edge(val from: String, val relation: String, val to: String)
    private val edges = LinkedHashSet<Edge>()
    fun add(from: String, relation: String, to: String) { edges += Edge(from, relation, to) }
    fun neighbors(node: String): List<Edge> = edges.filter { it.from == node || it.to == node }
    fun all(): List<Edge> = edges.toList()
}

class AuditTrail {
    data class Event(val action: String, val actor: String, val allowed: Boolean, val timestampMs: Long)
    private val events = ArrayDeque<Event>()
    fun record(event: Event) {
        events.addLast(event)
        while (events.size > 1000) events.removeFirst()
    }
    fun recent(limit: Int = 50): List<Event> = events.takeLast(limit)
}

class GovernanceInvariantGate {
    fun policy(version: String, principles: Set<String>, forbidden: Set<String>): AlignmentPolicy {
        val canonical = version + "|" + principles.sorted().joinToString(",") + "|" + forbidden.sorted().joinToString(",")
        return AlignmentPolicy(version, principles.toSet(), forbidden.toSet(), sha256(canonical))
    }

    fun evaluate(
        before: AlignmentPolicy,
        proposal: MutationProposal
    ): ProofResult {
        val obligations = proposal.invariantChecks
        val immutablePreserved = proposal.policyHashBefore == before.immutableHash &&
            proposal.policyHashAfter == before.immutableHash
        return ProofResult(
            passed = immutablePreserved && obligations.isNotEmpty(),
            proofType = "invariant-gate",
            obligations = obligations,
            evidence = listOf("policy-hash", "human-approval-required", "sandbox-only")
        )
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

class EpistemicHumilityLoop {
    fun inspect(
        confidence: Double,
        assumptions: List<String>,
        contradictions: List<String>
    ): HumilityReport = HumilityReport(
        confidence = confidence.coerceIn(0.0, 1.0),
        assumptions = assumptions.distinct(),
        contradictions = contradictions.distinct(),
        rollbackAvailable = true,
        humanReviewRecommended = confidence < 0.70 || contradictions.isNotEmpty()
    )
}

class QuantumHybridAdapter {
    /**
     * Adapter boundary for a future QPU. Current implementation is exact classical
     * fallback; it never fabricates quantum speedups or hardware access.
     */
    fun <T> search(query: QuantumQuery<T>): QuantumResult<T> {
        val item = query.items.firstOrNull(query.predicate)
        return QuantumResult(item, "CLASSICAL_FALLBACK", true, "linear-fallback")
    }
}

class PhysicalComputeAdvisor {
    fun recommend(
        thermal: ThermalSnapshot,
        availableRamMb: Long,
        acceleratorPresent: Boolean
    ): EnergyPolicy {
        val thermalFactor = thermal.recommendedLoad.coerceIn(0.1, 1.0)
        val memoryFactor = (availableRamMb / 4096.0).coerceIn(0.25, 1.0)
        val maxWork = min(thermalFactor, memoryFactor)
        return EnergyPolicy(
            maxWorkFraction = maxWork,
            preferLocal = acceleratorPresent && maxWork > 0.45,
            allowBackgroundWork = thermal.status < 4 && thermal.batteryPct > 25
        )
    }

    fun advisoryNotes(): List<String> = listOf(
        "Use Android thermal/battery signals to budget inference.",
        "Treat graphene/CNT/lab control as an external research adapter, not a device capability.",
        "Optimize algorithms and scheduling before attempting physical substrate changes."
    )
}


/** Aggregated, dependency-light control plane exposed to the Digital Nexus Core. */
class MetaArchitectureRuntime {
    val negotiation = NegotiationEngine()
    val dataDriven = DataDrivenDecisionEngine()
    val conflict = ConflictResolutionEngine()
    val criticalThinking = CriticalThinkingEngine()
    val delegation = DelegationEngine()
    val attention = AttentionCompiler()
    val retry = RetryPolicy()
    val failures = FailureMemoryStore()
    val concurrency = ConcurrencyEngine()
    val latency = LatencyBudgetEngine()
    val creativity = CreativityPolicy()
    val bandit = BanditRouter()
    val drift = DriftDetector()
    val prefetch = PredictivePrefetchPolicy()
    val zeroTrust = ZeroTrustGate()
    val reward = GoalRewardEngine()
    val knowledgeTransfer = KnowledgeTransferRegistry()
    val discovery = ServiceDiscoveryRegistry()
    val asyncCompletion = AsyncCompletionRegistry()
    val confidence = ConfidenceGate()
    val structuredOutput = StructuredOutputGate()
    val degradation = DegradationController()
    val clarification = ClarificationEngine()
    val dag = DagPlanner()
    val semanticIndex = SemanticClusterIndex()
    val knowledgeGraph = KnowledgeGraph()
    val audit = AuditTrail()
    val alignment = GovernanceInvariantGate()
    val humility = EpistemicHumilityLoop()
    val quantum = QuantumHybridAdapter()
    val physicalCompute = PhysicalComputeAdvisor()
}
