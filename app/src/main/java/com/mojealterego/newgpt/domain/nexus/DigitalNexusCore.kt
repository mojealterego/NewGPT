package com.mojealterego.newgpt.domain.nexus

import android.content.Context
import com.mojealterego.newgpt.data.local.GoTStore
import com.mojealterego.newgpt.data.local.LocalRagStore
import com.mojealterego.newgpt.data.local.MemoryGraphStore
import com.mojealterego.newgpt.data.local.TitansMemoryStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class NexusStatus(
    val bitemporal: Boolean,
    val coala: Boolean,
    val gMemory: Boolean,
    val hdc: Boolean,
    val got: Boolean,
    val reflexion: Boolean,
    val decision: Boolean,
    val evolutionSandbox: Boolean,
    val adversarialGating: Boolean,
    val mcpGateway: Boolean,
    val jepaAdapter: Boolean,
    val snnAdapter: Boolean,
    val formalVerificationAdapter: Boolean
)

@Singleton
class DigitalNexusCore @Inject constructor(
    @ApplicationContext context: Context,
    val got: GoTStore,
    val titans: TitansMemoryStore,
    val memoryGraph: MemoryGraphStore,
    val rag: LocalRagStore
) {
    private val root = File(context.filesDir, "nexus")

    val bitemporal = BitemporalMemoryStore(context)
    val coala = CoalaMemoryStore(File(root, "coala.json"))
    val gMemory = GMemoryStore(File(root, "gmemory.json"))
    val hdc = HdcMemory()
    val decision = DecisionCycle()
    val reflexion = ReflexionEngine()
    val evolution = EvolutionEngine()
    val adversarial = AdversarialGate()

    val gateway = McpGateway(
        GatewayPolicy(
            allowlist = setOf(
                "web-search",
                "web-fetch",
                "documents",
                "rag",
                "memory",
                "repository",
                "git",
                "calculator",
                "vision",
                "ocr",
                "image-generation",
                "video-generation",
                "voice-generation",
                "music-generation",
                "app-builder"
            )
        )
    )

    val jepa = DeterministicPredictor()
    val snn = SnnEventGate()
    val formalVerification = UnconfiguredFormalVerificationAdapter()

    /** Unified orchestration facade shared by UI, agents and Android integrations. */
    val runtime = NexusRuntimeFacade()
    val modelCatalog = ModelCatalog()
    val workspaces = WorkspaceRegistry()
    val recoveryGuard = RecoveryGuard()

    init {
        registerDefaultTools()
    }

    fun status() = NexusStatus(
        bitemporal = true,
        coala = true,
        gMemory = true,
        hdc = true,
        got = true,
        reflexion = true,
        decision = true,
        evolutionSandbox = true,
        adversarialGating = true,
        mcpGateway = true,
        jepaAdapter = true,
        snnAdapter = true,
        formalVerificationAdapter = true
    )

    private fun registerDefaultTools() {
        listOf(
            NexusTool("web-search", "Search current web information", requiresApproval = false),
            NexusTool("web-fetch", "Fetch a web resource", requiresApproval = false),
            NexusTool("documents", "Read and transform user documents"),
            NexusTool("rag", "Retrieve local knowledge", requiresApproval = false),
            NexusTool("memory", "Read and write application memory"),
            NexusTool("repository", "Inspect repository metadata", requiresApproval = false),
            NexusTool("git", "Governed source-control operation", sideEffect = true),
            NexusTool("calculator", "Deterministic calculation", requiresApproval = false),
            NexusTool("vision", "Analyze an image", requiresApproval = false),
            NexusTool("ocr", "Extract text from an image", requiresApproval = false),
            NexusTool("image-generation", "Generate an image", sideEffect = true),
            NexusTool("video-generation", "Generate video", sideEffect = true),
            NexusTool("voice-generation", "Generate speech", sideEffect = true),
            NexusTool("music-generation", "Generate music", sideEffect = true),
            NexusTool("app-builder", "Generate an application specification", sideEffect = true)
        ).forEach(gateway::register)
    }
}
