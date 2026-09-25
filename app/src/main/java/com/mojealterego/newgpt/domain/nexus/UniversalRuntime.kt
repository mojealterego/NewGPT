package com.mojealterego.newgpt.domain.nexus

import java.util.concurrent.atomic.AtomicLong

enum class RuntimePrivacy { STANDARD, PRIVATE, OFFLINE }

data class RuntimeRequest(
    val text: String,
    val mode: RouteMode = RouteMode.HYBRID,
    val privacy: RuntimePrivacy = RuntimePrivacy.STANDARD,
    val contextBudget: Int = 12_000
)

data class RuntimePlan(
    val endpoint: ModelEndpoint?,
    val compiledContext: CompiledContext,
    val warnings: List<String>,
    val allowedTools: List<String>,
    val requiresConfirmation: Boolean
)

class NexusRuntimeFacade(
    private val router: ModelRouter3,
    private val contextCompiler: ContextCompiler,
    private val lifecycle: MemoryLifecycleEngine,
    private val capabilities: CapabilityRegistry,
    private val permissions: ToolPermissionEngine,
    private val observability: ObservabilityCore,
    private val privacy: PrivacyPolicyEngine
) {
    suspend fun plan(
        request: RuntimeRequest,
        fragments: List<ContextFragment>,
        requestedTools: List<String>
    ): RuntimePlan {
        val started = System.nanoTime()
        val endpoint = router.choose(request.mode)
        val compiled = contextCompiler.compile(fragments, request.contextBudget)
        val warnings = compiled.warnings.toMutableList()

        if (request.privacy == RuntimePrivacy.PRIVATE) {
            if (!privacy.allow(setOf("text"), if (request.privacy == RuntimePrivacy.PRIVATE) "LOCAL" else "CLOUD", privateMode = true)) warnings += "PRIVATE_DATA_FLOW_BLOCKED"
        }

        val decisions = requestedTools.mapNotNull { id -> capabilities.find(id).firstOrNull()?.let { cap -> id to permissions.decide(ToolGrant(id, if (cap.risk >= 3) ToolRisk.EXTERNAL else if (cap.risk >= 1) ToolRisk.WRITE else ToolRisk.READ, requiresConfirmation = cap.requiresConfirmation), request.privacy != RuntimePrivacy.STANDARD) } }
        val allowed = decisions.filter { it.second.allowed }.map { it.first }
        val confirmation = decisions.any { it.second.requiresConfirmation }

        lifecycle.classify(LifecycleMemory("runtime-request", request.text, System.currentTimeMillis(), System.currentTimeMillis(), 0.5, 1.0, TrustLevel.USER_CONFIRMED))

        observability.record(AiMetric("runtime.plan", (System.nanoTime() - started) / 1_000_000L, true))
        return RuntimePlan(endpoint, compiled, warnings, allowed, confirmation)
    }
}

class ModelCatalog {
    data class Model(
        val id: String,
        val provider: String,
        val family: String,
        val quantization: String? = null,
        val contextSize: Int = 0,
        val local: Boolean = false,
        val multimodal: Boolean = false,
        val sha256: String? = null,
        val license: String? = null
    )

    private val models = linkedMapOf<String, Model>()
    fun upsert(model: Model) { models[model.id] = model }
    fun remove(id: String) { models.remove(id) }
    fun all(): List<Model> = models.values.toList()
    fun find(query: String): List<Model> =
        models.values.filter { it.id.contains(query, true) || it.provider.contains(query, true) || it.family.contains(query, true) }
}

class RuntimeCounters {
    private val requests = AtomicLong()
    private val failures = AtomicLong()
    fun request() = requests.incrementAndGet()
    fun failure() = failures.incrementAndGet()
    fun snapshot(): Pair<Long, Long> = requests.get() to failures.get()
}
