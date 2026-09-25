package com.mojealterego.newgpt.domain.nexus

data class NexusRuntimeSnapshot(
    val mode: RouteMode,
    val endpoint: ModelEndpoint?,
    val context: CompiledContext,
    val capabilities: List<CapabilityManifest>,
    val jobs: JobState,
    val metrics: List<AiMetric>,
    val releaseReady: Boolean
)

class NexusRuntimeFacade(
    private val router: ModelRouter3 = ModelRouter3(),
    private val compiler: ContextCompiler = ContextCompiler(),
    private val lifecycle: MemoryLifecycleEngine = MemoryLifecycleEngine(),
    private val capabilities: CapabilityRegistry = CapabilityRegistry(),
    private val workflows: WorkflowEngine = WorkflowEngine(),
    private val permissions: ToolPermissionEngine = ToolPermissionEngine(),
    private val jobs: LocalJobEngine = LocalJobEngine(),
    private val multimodal: MultimodalCapability = MultimodalCapability(),
    private val privacy: PrivacyPolicyEngine = PrivacyPolicyEngine(),
    private val observability: ObservabilityCore = ObservabilityCore(),
    private val release: ReleaseHealthGate = ReleaseHealthGate()
) {
    init {
        registerDefaults()
    }

    private fun registerDefaults() {
        listOf(
            CapabilityManifest("chat", "core", "Conversational AI", false),
            CapabilityManifest("agents", "orchestration", "Run and chain governed agents"),
            CapabilityManifest("memory", "knowledge", "Working, episodic, semantic and procedural memory"),
            CapabilityManifest("rag", "knowledge", "Hybrid local retrieval with provenance"),
            CapabilityManifest("web", "tools", "Current web research and fetching", true, 2),
            CapabilityManifest("multimodal", "media", "Text, image, PDF, audio and video inputs"),
            CapabilityManifest("generation", "media", "Image, video, voice and music generation", true, 2),
            CapabilityManifest("workflows", "automation", "Deterministic multi-step workflows", true, 2),
            CapabilityManifest("app-functions", "android", "Expose safe NewGPT functions to Android intelligence", true, 2),
            CapabilityManifest("evolution-lab", "research", "Sandboxed, human-gated evolution experiments", true, 3)
        ).forEach(capabilities::register)
    }

    suspend fun registerEndpoint(endpoint: ModelEndpoint) = router.register(endpoint)

    suspend fun chooseEndpoint(mode: RouteMode): ModelEndpoint? = router.choose(mode)

    fun compileContext(
        userText: String,
        retrieved: List<ContextFragment>,
        maxTokens: Int
    ): CompiledContext = compiler.compile(
        listOf(ContextFragment("user", userText, TrustLevel.USER_CONFIRMED, "chat"))
            + retrieved,
        maxTokens
    )

    fun classifyMemory(memory: LifecycleMemory): MemoryAction = lifecycle.classify(memory)

    fun toolDecision(toolId: String, risk: ToolRisk, privateMode: Boolean): ToolDecision =
        permissions.decide(
            ToolGrant(toolId = toolId, risk = risk, enabled = true, requiresConfirmation = risk != ToolRisk.READ),
            privateMode
        )

    fun canSend(dataClasses: Set<String>, destination: String, privateMode: Boolean): Boolean =
        privacy.allow(dataClasses, destination, privateMode)

    fun validateWorkflow(workflow: WorkflowDefinition): List<String> =
        workflows.validate(workflow)

    fun accepts(kind: MediaKind): Boolean = multimodal.accepts(kind)

    fun enqueueJob() = jobs.enqueue()
    fun startJob() = jobs.start()
    fun completeJob() = jobs.complete()

    fun recordMetric(metric: AiMetric) = observability.record(metric)

    fun releaseReady(gates: List<ReleaseGate>): Boolean = release.ready(gates)

    suspend fun snapshot(mode: RouteMode, maxTokens: Int = 4096): NexusRuntimeSnapshot =
        NexusRuntimeSnapshot(
            mode = mode,
            endpoint = router.choose(mode),
            context = compileContext("", emptyList(), maxTokens),
            capabilities = capabilities.all(),
            jobs = jobs.state(),
            metrics = observability.snapshot(),
            releaseReady = release.ready(
                listOf(
                    ReleaseGate("runtime", true, "runtime initialized"),
                    ReleaseGate("security", true, "policy engine available")
                )
            )
        )
}
