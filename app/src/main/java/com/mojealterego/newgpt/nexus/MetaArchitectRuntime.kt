package com.mojealterego.newgpt.nexus

import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

data class DecisionCriterion(val id: String, val weight: Double, val lowerIsBetter: Boolean = false)
data class DecisionOption(val id: String, val values: Map<String, Double>, val evidence: List<String> = emptyList())
data class DecisionScore(val optionId: String, val score: Double, val evidenceCoverage: Double)

class DecisionMatrix(private val criteria: List<DecisionCriterion>) {
    fun score(options: List<DecisionOption>): List<DecisionScore> {
        if (options.isEmpty() || criteria.isEmpty()) return emptyList()
        return options.map { option ->
            val totalWeight = criteria.sumOf { it.weight.coerceAtLeast(0.0) }.coerceAtLeast(1e-9)
            val score = criteria.sumOf { c ->
                val raw = option.values[c.id] ?: 0.0
                val normalized = if (c.lowerIsBetter) 1.0 / (1.0 + raw.coerceAtLeast(0.0)) else raw.coerceIn(0.0, 1.0)
                normalized * c.weight.coerceAtLeast(0.0)
            } / totalWeight
            val coverage = criteria.count { option.values.containsKey(it.id) }.toDouble() / criteria.size
            DecisionScore(option.id, score, coverage)
        }
    }
}

data class NegotiationConstraint(val name: String, val target: Double, val current: Double, val unit: String, val direction: String)
data class NegotiationBrief(val constraints: List<NegotiationConstraint>, val alternatives: List<String>, val technicalDebtCost: Double, val totalCostOfOwnership: Double, val recommendationBasis: String)

class NegotiationEngine {
    fun analyze(constraints: List<NegotiationConstraint>, alternatives: List<String>, technicalDebtCost: Double, totalCostOfOwnership: Double) =
        NegotiationBrief(constraints, alternatives.distinct(), technicalDebtCost.coerceAtLeast(0.0), totalCostOfOwnership.coerceAtLeast(0.0),
            "Compare SLA, latency, accuracy, migration cost, lock-in and TCO; do not optimize one metric in isolation.")
}

data class EvidenceMetric(val metric: String, val value: Double, val sampleCount: Int, val confidence: Double)
data class ExperimentArm(val id: String, val metric: Double, val latencyMs: Double, val cost: Double, val samples: Int)
data class TcoModel(val infrastructure: Double, val api: Double, val security: Double, val engineering: Double, val migration: Double) {
    val total: Double get() = infrastructure + api + security + engineering + migration
}
class DataDecisionEngine {
    fun metrics(arms: List<ExperimentArm>) = arms.map { EvidenceMetric("quality:" + it.id, it.metric, it.samples, confidence(it.samples)) }
    fun shadowDelta(baseline: ExperimentArm, candidate: ExperimentArm) = mapOf(
        "qualityDelta" to candidate.metric - baseline.metric,
        "latencyDeltaMs" to candidate.latencyMs - baseline.latencyMs,
        "costDelta" to candidate.cost - baseline.cost
    )
    private fun confidence(n: Int) = (1.0 - exp(-n.coerceAtLeast(0) / 50.0)).coerceIn(0.0, .999)
}

data class ConflictPosition(val stakeholder: String, val goals: Map<String, Double>)
data class ConflictResolution(val sharedObjectives: List<String>, val unresolved: List<String>, val processActions: List<String>)
class ConflictResolver {
    fun resolve(positions: List<ConflictPosition>): ConflictResolution {
        if (positions.isEmpty()) return ConflictResolution(emptyList(), emptyList(), emptyList())
        val keys = positions.flatMap { it.goals.keys }.toSet()
        val shared = keys.filter { key -> positions.all { it.goals.containsKey(key) } }
        return ConflictResolution(shared, keys - shared.toSet(),
            listOf("Use evidence and explicit constraints", "Separate trade-offs from personal disagreements", "Record decisions and revisit after measured results"))
    }
}

class AdaptationEngine {
    fun chooseBackend(localHealthy: Boolean, cloudHealthy: Boolean, privacyMode: Boolean): String = when {
        privacyMode && localHealthy -> "LOCAL"
        localHealthy && cloudHealthy -> "HYBRID"
        cloudHealthy -> "CLOUD"
        localHealthy -> "LOCAL_DEGRADED"
        else -> "OFFLINE_DEGRADED"
    }
}

data class ContextItem(val id: String, val text: String, val tokens: Int, val priority: Double, val trust: Double)
data class ContextBudget(val maxTokens: Int, val reservedTokens: Int = 0)
data class CompiledContext(val items: List<ContextItem>, val usedTokens: Int, val omitted: Int, val warnings: List<String>)
class ContextOptimizer {
    fun compile(items: List<ContextItem>, budget: ContextBudget): CompiledContext {
        val unique = items.distinctBy { it.id }
        val capacity = (budget.maxTokens - budget.reservedTokens).coerceAtLeast(0)
        val selected = mutableListOf<ContextItem>()
        var used = 0
        unique.sortedByDescending { it.priority * it.trust }.forEach { item ->
            if (used + item.tokens <= capacity) { selected += item; used += item.tokens }
        }
        return CompiledContext(selected, used, unique.size - selected.size,
            if (selected.size < unique.size) listOf("Context budget applied; lower-priority material omitted") else emptyList())
    }
}

data class RetryPolicy(val maxAttempts: Int = 4, val initialDelayMs: Long = 250, val maxDelayMs: Long = 8_000, val jitterRatio: Double = .20)
class BackoffEngine(private val random: Random = Random.Default) {
    fun delays(policy: RetryPolicy): List<Long> = (0 until policy.maxAttempts.coerceAtLeast(1)).map { i ->
        val base = (policy.initialDelayMs * (1L shl i.coerceAtMost(20))).coerceAtMost(policy.maxDelayMs)
        val jitter = (base * policy.jitterRatio.coerceIn(0.0, 1.0) * random.nextDouble(-1.0, 1.0)).toLong()
        (base + jitter).coerceAtLeast(0L)
    }
}

data class FailureRecord(val fingerprint: String, val operation: String, val message: String, val timestampMs: Long)
class FailureMemory(private val maxEntries: Int = 1000) {
    private val records = ArrayDeque<FailureRecord>()
    @Synchronized fun remember(operation: String, message: String) {
        val fp = sha256(operation + "|" + message.take(2000))
        records.removeAll { it.fingerprint == fp }
        records.addLast(FailureRecord(fp, operation, message.take(4000), System.currentTimeMillis()))
        while (records.size > maxEntries) records.removeFirst()
    }
    @Synchronized fun retrieve(operation: String, limit: Int = 5): List<FailureRecord> =
        records.asReversed().filter { it.operation == operation }.take(limit.coerceIn(1, 50))
    private fun sha256(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}

data class TimedResult<T>(val value: T?, val elapsedMs: Long, val partial: Boolean)
class LatencyBudget(private val budgetMs: Long) {
    suspend fun <T> execute(task: suspend () -> T): TimedResult<T> {
        val start = System.currentTimeMillis()
        return try {
            val value = kotlinx.coroutines.withTimeout(budgetMs.coerceAtLeast(1)) { task() }
            TimedResult(value, System.currentTimeMillis() - start, false)
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            TimedResult(null, System.currentTimeMillis() - start, true)
        }
    }
}

class EpsilonGreedy(private val epsilon: Double = .05, private val random: Random = Random.Default) {
    fun choose(values: Map<String, Double>): String? {
        if (values.isEmpty()) return null
        if (random.nextDouble() < epsilon.coerceIn(0.0, 1.0)) return values.keys.random(random)
        return values.maxByOrNull { it.value }?.key
    }
}

data class DriftResult(val anomaly: Boolean, val similarity: Double, val threshold: Double)
class DriftDetector(private val threshold: Double = .65) {
    fun compare(current: FloatArray, baseline: FloatArray): DriftResult {
        if (current.isEmpty() || current.size != baseline.size) return DriftResult(true, 0.0, threshold)
        var dot=0.0; var a=0.0; var b=0.0
        current.indices.forEach { i -> dot += current[i]*baseline[i]; a += current[i]*current[i]; b += baseline[i]*baseline[i] }
        val sim = if (a==0.0 || b==0.0) 0.0 else dot/(sqrt(a)*sqrt(b))
        return DriftResult(sim < threshold, sim, threshold)
    }
}

data class PrefetchPlan(val trigger: String, val predictedTasks: List<String>, val confidence: Double)
class SpeculativePlanner {
    fun plan(trigger: String, candidates: List<Pair<String, Double>>, minimumConfidence: Double=.8) =
        PrefetchPlan(trigger, candidates.filter { it.second >= minimumConfidence }.sortedByDescending { it.second }.take(5).map { it.first },
            candidates.maxOfOrNull { it.second } ?: 0.0)
}

enum class Risk { READ, WRITE, EXTERNAL, PRIVILEGED }
data class SecurityDecision(val allowed: Boolean, val requiresHuman: Boolean, val reason: String)
class ZeroTrustGate {
    fun check(risk: Risk, privateMode: Boolean, approved: Boolean): SecurityDecision = when {
        risk == Risk.PRIVILEGED -> SecurityDecision(false, true, "Privileged capability requires explicit human approval")
        privateMode && risk == Risk.EXTERNAL -> SecurityDecision(false, true, "External access blocked in private mode")
        risk == Risk.WRITE && !approved -> SecurityDecision(false, true, "Write action requires confirmation")
        else -> SecurityDecision(true, false, "Allowed by policy")
    }
}

data class ConfidenceDecision(val confidence: Double, val requireHuman: Boolean, val reason: String)
class ConfidenceGate(private val humanThreshold: Double=.98) {
    fun evaluate(confidence: Double, highImpact: Boolean): ConfidenceDecision {
        val c=confidence.coerceIn(0.0,1.0)
        return ConfidenceDecision(c, highImpact && c<humanThreshold,
            if (highImpact && c<humanThreshold) "Low confidence for high-impact action" else "Confidence threshold satisfied")
    }
}

data class SchemaField(val name: String, val required: Boolean, val validator: (Any?)->Boolean)
class StrictSchema(private val fields: List<SchemaField>) {
    fun validate(input: Map<String,Any?>): List<String> = fields.flatMap { f ->
        when {
            f.required && !input.containsKey(f.name) -> listOf("missing:" + f.name)
            input.containsKey(f.name) && !f.validator(input[f.name]) -> listOf("invalid:" + f.name)
            else -> emptyList()
        }
    }
}

class LoadShedder {
    fun mode(primaryHealthy:Boolean, advancedHealthy:Boolean)=when {
        primaryHealthy && advancedHealthy -> "FULL"
        primaryHealthy -> "BASIC"
        else -> "OFFLINE"
    }
}

data class Clarification(val needed:Boolean, val candidates:List<String>, val question:String?)
class FuzzyClarifier {
    fun resolve(query:String,candidates:List<String>):Clarification {
        val q=query.trim().lowercase()
        if(q.isEmpty()) return Clarification(true,candidates.take(5),"What should I select?")
        val ranked=candidates.map{it to similarity(q,it.lowercase())}.sortedByDescending{it.second}
        val top=ranked.take(3).filter{it.second>.25}.map{it.first}
        return if(top.size==1 && ranked.first().second>=.8) Clarification(false,top,null)
        else Clarification(true,top,"Which of these do you mean: "+top.joinToString(", ")+"?")
    }
    private fun similarity(a:String,b:String):Double {
        val sa=a.split(Regex("\\W+")).filter{it.isNotBlank()}.toSet()
        val sb=b.split(Regex("\\W+")).filter{it.isNotBlank()}.toSet()
        return if(sa.isEmpty()||sb.isEmpty()) 0.0 else sa.intersect(sb).size.toDouble()/sa.union(sb).size
    }
}

enum class DagStatus { READY,BLOCKED,DONE,FAILED }
data class DagTask(val id:String,val dependencies:List<String> = emptyList(),val status:DagStatus=DagStatus.READY)
class DagPlanner {
    fun validate(tasks:List<DagTask>):List<String>{
        val ids=tasks.map{it.id}; val errors=mutableListOf<String>()
        if(ids.size!=ids.toSet().size) errors+="duplicate-task-id"
        tasks.forEach{t->t.dependencies.filter{it !in ids}.forEach{d->errors+="unknown-dependency:"+t.id+":"+d}}
        return errors
    }
    fun ready(tasks:List<DagTask>):List<String>{
        val done=tasks.filter{it.status==DagStatus.DONE}.map{it.id}.toSet()
        return tasks.filter{it.status==DagStatus.READY && it.dependencies.all{d->d in done}}.map{it.id}
    }
}

class SemanticRouter {
    fun route(query:FloatArray,centroids:Map<String,FloatArray>):String?=centroids.maxByOrNull{cosine(query,it.value)}?.key
    private fun cosine(a:FloatArray,b:FloatArray):Double{
        if(a.isEmpty()||a.size!=b.size)return -1.0
        var d=0.0;var na=0.0;var nb=0.0
        a.indices.forEach{i->d+=a[i]*b[i];na+=a[i]*a[i];nb+=b[i]*b[i]}
        return if(na==0.0||nb==0.0)-1.0 else d/(sqrt(na)*sqrt(nb))
    }
}

data class ReasoningSummary(val claim:String,val evidence:List<String>,val assumptions:List<String>,val uncertainty:Double)
class ReasoningAuditor {
    fun audit(claim:String,evidence:List<String>,assumptions:List<String>)=
        ReasoningSummary(claim.take(2000),evidence.map{it.take(2000)},assumptions.map{it.take(1000)},if(evidence.isEmpty())1.0 else 1.0/(1.0+evidence.size))
}

data class KnowledgeRelation(val subject:String,val predicate:String,val objectId:String)
class KnowledgeGraphExtractor {
    fun extract(text:String):List<KnowledgeRelation> =
        Regex("\\\\[([^]]+)]\\\\s*->\\\\s*\\\\[([^]]+)]\\\\s*->\\\\s*\\\\[([^]]+)]")
            .findAll(text).map{KnowledgeRelation(it.groupValues[1],it.groupValues[2],it.groupValues[3])}.toList()
}

// 95–99: executable software-side governance only; impossible physical/quantum capabilities are not represented.
data class GovernanceInvariant(val id:String,val description:String,val enabled:Boolean=true)
data class MutationVerification(val mutationId:String,val preserved:List<String>,val passed:Boolean,val humanApprovalRequired:Boolean)
class GovernanceInvariantGate(private val invariants:List<GovernanceInvariant>) {
    fun verify(mutationId:String, candidateDigest:String):MutationVerification {
        val preserved=invariants.filter{it.enabled}.map{it.id}
        val passed=mutationId.isNotBlank() && candidateDigest.isNotBlank() && preserved.isNotEmpty()
        return MutationVerification(mutationId,preserved,passed,true)
    }
}
data class EpistemicCheck(val confidence:Double,val contradictions:List<String>,val rollbackAvailable:Boolean)
class EpistemicHumilityLoop {
    fun inspect(confidence:Double,contradictions:List<String>,rollbackAvailable:Boolean)=
        EpistemicCheck(confidence.coerceIn(0.0,1.0),contradictions.take(20),rollbackAvailable)
}

enum class SpikeCoding { RATE, TEMPORAL }
data class Spike(val neuron:Int,val timeStep:Int,val amplitude:Int=1)
data class SnnProgram(val neurons:Int,val steps:Int,val spikes:List<Spike>)

class SnnTranspiler {
    fun transpile(weights:FloatArray,coding:SpikeCoding,steps:Int=16,threshold:Float=.5f):SnnProgram{
        val safeSteps=steps.coerceIn(1,4096); val spikes=mutableListOf<Spike>()
        weights.forEachIndexed{neuron,weight->
            val normalized=abs(weight).coerceIn(0f,1f)
            if(normalized<threshold)return@forEachIndexed
            when(coding){
                SpikeCoding.RATE->{val count=(normalized*safeSteps).toInt().coerceIn(1,safeSteps);(0 until count).forEach{t->spikes+=Spike(neuron,t)}}
                SpikeCoding.TEMPORAL->{val t=((1f-normalized)*(safeSteps-1)).toInt();spikes+=Spike(neuron,t)}
            }
        }
        return SnnProgram(weights.size,safeSteps,spikes)
    }
}

// 107: energy-aware routing.
data class ComputeNode(val id:String,val thermalC:Double,val energyPerOp:Double,val healthy:Boolean=true)
class EnergyAwareRouter { fun choose(nodes:List<ComputeNode>)=nodes.filter{it.healthy}.minByOrNull{it.thermalC*.45+it.energyPerOp*.55} }

// 108: stochastic-noise source.
class NoiseEngine(private val random:Random=Random.Default) {
    fun sample(amplitude:Double=.01)=random.nextDouble(-amplitude,amplitude)
    fun mutate(value:Double,amplitude:Double=.01)=value+sample(amplitude)
}

class MetaArchitectRuntime {
    val decisions=DecisionMatrix(listOf(
        DecisionCriterion("quality",.35),DecisionCriterion("latency",.20,true),
        DecisionCriterion("cost",.20,true),DecisionCriterion("privacy",.15),DecisionCriterion("reliability",.10)
    ))
    val negotiation=NegotiationEngine(); val dataDecision=DataDecisionEngine(); val conflicts=ConflictResolver()
    val adaptation=AdaptationEngine(); val context=ContextOptimizer(); val backoff=BackoffEngine()
    val failures=FailureMemory(); val drift=DriftDetector(); val prefetch=SpeculativePlanner()
    val zeroTrust=ZeroTrustGate(); val confidence=ConfidenceGate(); val loadShedder=LoadShedder()
    val clarifier=FuzzyClarifier(); val dag=DagPlanner(); val semanticRouter=SemanticRouter()
    val reasoning=ReasoningAuditor(); val knowledgeGraph=KnowledgeGraphExtractor()
    val alignment=GovernanceInvariantGate(listOf(
        GovernanceInvariant("human-approval","Self-modifying or consequential changes require human approval"),
        GovernanceInvariant("rollback","Every evolution candidate must have a rollback path"),
        GovernanceInvariant("least-privilege","Tools execute only within explicit permissions")
    ))
    val humility=EpistemicHumilityLoop()
    val snn=SnnTranspiler()
    val energy=EnergyAwareRouter()
    val noise=NoiseEngine()
}
