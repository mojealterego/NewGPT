package com.mojealterego.newgpt.runtime

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

data class Budget(val maxMs: Long, val maxTokens: Int = 8_000, val maxParallel: Int = 4)
data class Score(val value: Double, val confidence: Double, val rationale: String) {
    init { require(value in 0.0..1.0); require(confidence in 0.0..1.0) }
}
data class DecisionOption(val id: String, val criteria: Map<String, Double>)
data class DecisionResult(val selected: String?, val scores: Map<String, Double>, val tradeoffs: List<String>)

class DecisionMatrix {
    fun evaluate(options: List<DecisionOption>, weights: Map<String, Double>): DecisionResult {
        require(options.isNotEmpty())
        val totals = options.associate { option ->
            option.id to option.criteria.entries.sumOf { (k, v) -> v.coerceIn(0.0, 1.0) * max(0.0, weights[k] ?: 0.0) }
        }
        return DecisionResult(totals.maxByOrNull { it.value }?.key, totals, emptyList())
    }
}

data class NegotiationPosition(val objective: String, val reservationPoint: Double, val target: Double, val batna: String, val measurableEvidence: List<String>)
class NegotiationEngine {
    fun prepare(position: NegotiationPosition): List<String> = buildList {
        add("Target: ${position.target}; reservation point: ${position.reservationPoint}.")
        add("BATNA: ${position.batna}.")
        addAll(position.measurableEvidence.map { "Evidence: ${it}" })
        add("Separate reversible experiments from irreversible commitments.")
        add("Quantify TCO, latency, reliability and migration cost before concession.")
    }
}

data class ConflictCase(val stakeholders: Set<String>, val constraints: Map<String, String>, val objective: String)
class ConflictResolver {
    fun createMatrix(case: ConflictCase): Map<String, List<String>> = mapOf(
        "shared_objective" to listOf(case.objective),
        "constraints" to case.constraints.entries.map { "${it.key}: ${it.value}" },
        "decision_process" to listOf("document assumptions", "measure", "run bounded experiment", "review evidence"),
        "postmortem" to listOf("focus on system/process", "record guardrail failure", "assign corrective action")
    )
}

data class DelegationContract(val capability: String, val inputSchema: String, val outputSchema: String, val latencyBudgetMs: Long, val maxRetries: Int, val auditRequired: Boolean)
class ContractValidator {
    fun validate(input: Map<String, Any?>, required: Set<String>): List<String> = required.filter { input[it] == null || input[it].toString().isBlank() }
}

class ExponentialBackoff(private val baseMs: Long = 250, private val maxMs: Long = 8_000) {
    fun delayMs(attempt: Int, random: Random = Random.Default): Long {
        val raw = min(maxMs, baseMs * (1L shl min(attempt.coerceAtLeast(0), 5)))
        return (raw / 2) + random.nextLong((raw / 2).coerceAtLeast(1))
    }
}

data class FailureRecord(val fingerprint: String, val taskClass: String, val symptom: String, val correctiveAction: String, val timestampMs: Long)
class FailureMemory(private val maxRecords: Int = 2_000) {
    private val records = ArrayDeque<FailureRecord>()
    @Synchronized fun remember(record: FailureRecord) {
        records.removeAll { it.fingerprint == record.fingerprint }; records.addFirst(record)
        while (records.size > maxRecords) records.removeLast()
    }
    @Synchronized fun relevant(taskClass: String, limit: Int = 8): List<FailureRecord> = records.filter { it.taskClass == taskClass }.take(limit)
}

data class ContextItem(val id: String, val text: String, val priority: Double, val trust: Double)
data class CompiledContext(val items: List<ContextItem>, val estimatedTokens: Int, val dropped: Int)
class ContextOptimizer {
    fun compile(items: List<ContextItem>, tokenBudget: Int): CompiledContext {
        val ordered = items.distinctBy { it.id }.sortedByDescending { it.priority * it.trust }
        var used = 0
        val kept = ordered.takeWhile {
            val cost = max(1, it.text.length / 4)
            if (used + cost <= tokenBudget) { used += cost; true } else false
        }
        return CompiledContext(kept, used, ordered.size - kept.size)
    }
}

data class LatencyResult<T>(val value: T?, val elapsedMs: Long, val partial: Boolean)
class LatencyBudget(private val budget: Budget) {
    fun <T> accept(elapsedMs: Long, value: T): LatencyResult<T> = LatencyResult(value, elapsedMs, elapsedMs > budget.maxMs)
}

data class BanditArm(val id: String, val trials: Int, val reward: Double)
class EpsilonGreedyRouter(private val epsilon: Double = 0.05) {
    fun choose(arms: List<BanditArm>, random: Random = Random.Default): String {
        require(arms.isNotEmpty())
        if (random.nextDouble() < epsilon) return arms.random(random).id
        return arms.maxByOrNull { if (it.trials == 0) Double.POSITIVE_INFINITY else it.reward / it.trials }!!.id
    }
}

data class DriftSignal(val distance: Double, val threshold: Double, val detected: Boolean)
class ConceptDriftDetector(private val threshold: Double = 0.75) {
    fun cosineDistance(a: List<Double>, b: List<Double>): Double {
        require(a.size == b.size && a.isNotEmpty())
        val dot = a.zip(b).sumOf { it.first * it.second }
        val na = sqrt(a.sumOf { it * it }); val nb = sqrt(b.sumOf { it * it })
        if (na == 0.0 || nb == 0.0) return 1.0
        return 1.0 - (dot / (na * nb)).coerceIn(-1.0, 1.0)
    }
    fun detect(distance: Double) = DriftSignal(distance, threshold, distance >= threshold)
}

data class PrefetchCandidate(val id: String, val probability: Double)
class PredictivePrefetch(private val threshold: Double = 0.80) {
    fun select(candidates: List<PrefetchCandidate>): List<PrefetchCandidate> = candidates.filter { it.probability >= threshold }.sortedByDescending { it.probability }
}

enum class Risk { READ, WRITE, EXTERNAL, PRIVILEGED }
data class ToolRequest(val id: String, val risk: Risk, val payloadHash: String)
data class ToolDecision(val allowed: Boolean, val confirmationRequired: Boolean, val reason: String)
class ZeroTrustPolicy {
    fun evaluate(request: ToolRequest, privateMode: Boolean): ToolDecision = when {
        request.risk == Risk.PRIVILEGED -> ToolDecision(false, true, "Privileged capability requires explicit operator flow.")
        privateMode && request.risk == Risk.EXTERNAL -> ToolDecision(false, false, "External access blocked in private mode.")
        request.risk == Risk.WRITE || request.risk == Risk.EXTERNAL -> ToolDecision(false, true, "Side-effecting capability requires confirmation.")
        else -> ToolDecision(true, false, "Read-only capability allowed.")
    }
}

data class Goal(val id: String, val metrics: Map<String, Double>, val constraints: Map<String, Double>)
class GoalOptimizer {
    fun score(goal: Goal, observed: Map<String, Double>): Double =
        goal.metrics.entries.sumOf { (k, w) -> (observed[k] ?: 0.0).coerceIn(0.0, 1.0) * w }
}

data class StructuredField(val name: String, val required: Boolean)
class StructuredOutputValidator {
    fun validate(fields: List<StructuredField>, output: Map<String, Any?>): List<String> = fields.filter { it.required && output[it.name] == null }.map { "Missing field: ${it.name}" }
}

class GracefulDegradation {
    fun choose(primaryHealthy: Boolean, fallbackHealthy: Boolean): String = when {
        primaryHealthy -> "PRIMARY"
        fallbackHealthy -> "FALLBACK"
        else -> "MINIMAL"
    }
}

data class ClarificationCandidate(val id: String, val similarity: Double)
class ClarificationLoop(private val ambiguityThreshold: Double = 0.15) {
    fun clarify(candidates: List<ClarificationCandidate>): List<ClarificationCandidate> {
        if (candidates.isEmpty()) return emptyList()
        val top = candidates.sortedByDescending { it.similarity }
        val gap = top.first().similarity - (top.getOrNull(1)?.similarity ?: 0.0)
        return if (gap < ambiguityThreshold) top.take(3) else emptyList()
    }
}

data class DagNode<T>(val id: String, val deps: Set<String>, val action: suspend () -> T)
class DagPlanner {
    fun <T> validate(nodes: List<DagNode<T>>) {
        require(nodes.map { it.id }.toSet().size == nodes.size) { "Duplicate node id." }
        val ids = nodes.map { it.id }.toSet()
        require(nodes.all { it.deps.all(ids::contains) }) { "Unknown dependency." }
        fun visit(id: String, path: Set<String>): Boolean {
            if (id in path) return false
            val node = nodes.firstOrNull { it.id == id } ?: return true
            return node.deps.all { visit(it, path + id) }
        }
        require(nodes.all { visit(it.id, emptySet()) }) { "Cycle detected." }
    }
}

data class Cluster(val id: String, val members: List<String>)
class SemanticClusterRouter {
    fun cluster(labels: List<String>, maxClusterSize: Int = 16): List<Cluster> = labels.distinct().chunked(maxClusterSize.coerceAtLeast(1)).mapIndexed { i, group -> Cluster("cluster-§i", group) }
}

data class KnowledgeRelation(val subject: String, val predicate: String, val objectId: String)
class KnowledgeGraph(private val maxEdges: Int = 10_000) {
    private val edges = ArrayDeque<KnowledgeRelation>()
    @Synchronized fun add(edge: KnowledgeRelation) { edges.addFirst(edge); while (edges.size > maxEdges) edges.removeLast() }
    @Synchronized fun find(subject: String) = edges.filter { it.subject == subject }
}

data class PreferenceSignal(val itemId: String, val positive: Boolean, val context: String)
class PreferenceBuffer(private val maxSize: Int = 5_000) {
    private val signals = ArrayDeque<PreferenceSignal>()
    @Synchronized fun add(signal: PreferenceSignal) { signals.addFirst(signal); while (signals.size > maxSize) signals.removeLast() }
    @Synchronized fun snapshot() = signals.toList()
}

data class ConfidenceGate(val confidence: Double, val threshold: Double, val humanRequired: Boolean, val reason: String)
class HumilityGate {
    fun evaluate(confidence: Double, highImpact: Boolean): ConfidenceGate {
        val c = confidence.coerceIn(0.0, 1.0)
        val human = highImpact && c < 0.98
        return ConfidenceGate(c, 0.98, human, if (human) "Uncertainty exceeds high-impact autonomy budget." else "Within configured confidence budget.")
    }
}

data class CognitiveAudit(val claims: List<String>, val evidenceIds: List<String>, val unsupportedClaims: List<String>, val assumptions: List<String>)
class ReasoningAuditor {
    fun audit(claims: List<String>, evidenceIds: Set<String>, claimEvidence: Map<String, String>): CognitiveAudit {
        val unsupported = claims.filter { claimEvidence[it].isNullOrBlank() || claimEvidence[it] !in evidenceIds }
        return CognitiveAudit(claims, evidenceIds.toList(), unsupported, emptyList())
    }
}

data class TechRadarEntry(val name: String, val maturity: String, val evidenceUrl: String?, val revisitAfterMs: Long)
class TechRadar {
    fun rank(entries: List<TechRadarEntry>): List<TechRadarEntry> = entries.sortedBy { it.revisitAfterMs }
}

/** 95. Physical substrate optimization: design-space planning only. */
data class SubstrateCandidate(val material: String, val architecture: String, val estimatedGain: Double, val energyPerOp: Double, val manufacturability: Double)
class PhysicalSubstratePlanner {
    fun rank(candidates: List<SubstrateCandidate>): List<SubstrateCandidate> =
        candidates.sortedByDescending { it.estimatedGain * it.manufacturability / max(0.001, it.energyPerOp) }
}

/** 96. Quantum-classical hybrid adapter with deterministic classical fallback. */
interface QuantumBackend { suspend fun optimize(problem: String, parameters: Map<String, Double>): Map<String, Double> }
class ClassicalQuantumFallback : QuantumBackend {
    override suspend fun optimize(problem: String, parameters: Map<String, Double>): Map<String, Double> = parameters.toSortedMap()
}
class QuantumHybridCoordinator(private val backend: QuantumBackend = ClassicalQuantumFallback()) {
    suspend fun optimize(problem: String, parameters: Map<String, Double>) = backend.optimize(problem, parameters)
}

/** 97. Energy-aware compute scheduling. */
data class EnergyBudget(val joules: Double, val thermalHeadroom: Double, val batteryFraction: Double)
data class ComputePlan(val mode: String, val maxParallelism: Int, val maxTokens: Int)
class EnergyAwareScheduler {
    fun plan(budget: EnergyBudget, requestedTokens: Int, requestedParallelism: Int): ComputePlan {
        val constrained = budget.joules < 20.0 || budget.thermalHeadroom < 0.20 || budget.batteryFraction < 0.15
        return if (constrained) ComputePlan("ENERGY_SAVER", 1, min(requestedTokens, 1_500))
        else ComputePlan("PERFORMANCE", requestedParallelism.coerceAtLeast(1), requestedTokens.coerceAtLeast(1))
    }
}

/** 98. Explicit-invariant alignment gate; not a proof that CEV is mathematically complete. */
data class AlignmentPolicy(val policyHash: String, val immutableInvariants: Set<String>, val forbiddenEffects: Set<String>)
data class MutationProposal(val id: String, val changes: Set<String>, val newPolicyHash: String)
data class AlignmentDecision(val allowed: Boolean, val failedInvariants: List<String>, val reason: String)
class ProvableAlignmentGate {
    fun verify(policy: AlignmentPolicy, proposal: MutationProposal): AlignmentDecision {
        val failures = buildList {
            if (proposal.newPolicyHash != policy.policyHash) add("Policy hash changed.")
            if (proposal.changes.intersect(policy.forbiddenEffects).isNotEmpty()) add("Forbidden effect requested.")
            if (proposal.changes.any { it.isBlank() }) add("Malformed mutation.")
        }
        return AlignmentDecision(failures.isEmpty(), failures, if (failures.isEmpty()) "All explicit invariants hold." else "Mutation rejected.")
    }
}

/** 99. Epistemic humility with dissent, assumptions and rollback availability. */
data class EpistemicCheck(val confidence: Double, val dissent: List<String>, val assumptions: List<String>, val rollbackAvailable: Boolean, val humanReviewRequired: Boolean)
class EpistemicHumilityLoop {
    fun check(confidence: Double, dissent: List<String>, assumptions: List<String>, highImpact: Boolean): EpistemicCheck {
        val c = confidence.coerceIn(0.0, 1.0)
        return EpistemicCheck(c, dissent, assumptions, true, highImpact && (c < 0.98 || dissent.isNotEmpty()))
    }
}

data class Event<T>(val type: String, val payload: T, val timestampMs: Long = System.currentTimeMillis())
class BoundedEventBus<T>(private val maxEvents: Int = 2_000) {
    private val events = ArrayDeque<Event<T>>()
    @Synchronized fun publish(event: Event<T>) { events.addFirst(event); while (events.size > maxEvents) events.removeLast() }
    @Synchronized fun recent(type: String? = null): List<Event<T>> = events.filter { type == null || it.type == type }
}

data class LaboratoryAction(val id: String, val description: String, val requiresApproval: Boolean = true)
class LaboratoryGateway {
    fun authorize(action: LaboratoryAction, approved: Boolean): Boolean = !action.requiresApproval || approved
}
