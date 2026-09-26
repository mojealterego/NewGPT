package com.mojealterego.newgpt.presentation.system

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojealterego.newgpt.presentation.theme.GoldCard
import com.mojealterego.newgpt.presentation.theme.PremiumScaffold

private data class SystemDomain(val id: String, val name: String, val status: String, val description: String)

private val domains = listOf(
SystemDomain("01","Cognitive OS","IMPLEMENTED","Decision cycle, capability/policy gating, runtime budgets and cognitive control."),
SystemDomain("02","Agentic Mesh","IMPLEMENTED","Local agent registry, DAG scheduling and handoff pipeline."),
SystemDomain("03","Memory OS","IMPLEMENTED","Bitemporal memory, checkpoints and memory graph primitives."),
SystemDomain("04","Knowledge / RAG","PARTIAL","Hybrid retrieval orchestration is present; production indexing remains bounded."),
SystemDomain("05","Reasoning Engine","PARTIAL","Graph-of-Thought and supervisor primitives exist; advanced reasoning is bounded."),
SystemDomain("06","Model Router","IMPLEMENTED","Provider-aware routing and local/cloud strategy selection."),
SystemDomain("07","Local AI / GGUF","IMPLEMENTED","GGUF import and llama.cpp JNI execution boundary."),
SystemDomain("08","Cloud AI","IMPLEMENTED","OpenAI, Anthropic, Gemini and OpenAI-compatible strategy layer."),
SystemDomain("09","Tool Fabric","PARTIAL","Declarative tool catalog and guarded execution boundary."),
SystemDomain("10","MCP Fabric","PLANNED","MCP adapter boundary reserved; full gateway still requires implementation."),
SystemDomain("11","Android Edge Runtime","IMPLEMENTED","Native Android/Compose runtime with local inference boundary."),
SystemDomain("12","Device Capabilities","PARTIAL","Safe capability contracts exist; device executors are not universally wired."),
SystemDomain("13","Security / Policy","IMPLEMENTED","Policy engine, tool-call guard and encrypted provider settings."),
SystemDomain("14","HITL / Approval","PARTIAL","Approval boundary is modeled but not every privileged path is interactive."),
SystemDomain("15","Supervisor / Verifier","IMPLEMENTED","Supervisor primitives and verification contracts."),
SystemDomain("16","Evidence Engine","IMPLEMENTED","Evidence ledger and execution audit primitives."),
SystemDomain("17","Execution / Sandbox","PARTIAL","Execution gateway exists; isolated sandbox runtime remains bounded."),
SystemDomain("18","Observability","PARTIAL","Execution audit foundation; full OpenTelemetry surface is not complete."),
SystemDomain("19","Evaluation","PARTIAL","Unit-test and evaluation primitives exist; benchmark lab remains to be expanded."),
SystemDomain("20","Evolution Lab","IMPLEMENTED","Digital genotype, bounded mutations, evolution and red-team scaffolding."),
SystemDomain("21","Voice / Realtime","PARTIAL","Creative voice integration exists; low-latency realtime pipeline is not complete."),
SystemDomain("22","Digital Human","PLANNED","Avatar/lip-sync integration boundary reserved."),
SystemDomain("23","Creative Studio","IMPLEMENTED","Voice, music, video and Canva integration surface."),
SystemDomain("24","Cinematic AI","PARTIAL","Creative orchestration surface exists; full director/editor pipeline is not complete."),
SystemDomain("25","Image / Photo","PLANNED","Dedicated visual generation/continuity runtime remains to be integrated."),
SystemDomain("26","Audio / Music","IMPLEMENTED","Music and speech generation service adapters."),
SystemDomain("27","Video","IMPLEMENTED","Video-generation adapter surface in Creative Studio."),
SystemDomain("28","Web / Research","IMPLEMENTED","Web access service and research-oriented agent contract."),
SystemDomain("29","OSINT","PARTIAL","Research capabilities are available; dedicated OSINT workflow remains bounded."),
SystemDomain("30","Code Studio","PARTIAL","Coder agent and App Builder contracts; full in-app compiler loop is not complete."),
SystemDomain("31","Agent Studio","IMPLEMENTED","Agent Builder with local persistent definitions and handoffs."),
SystemDomain("32","MCP Studio","PLANNED","MCP creation/testing UI reserved."),
SystemDomain("33","Workflow Studio","PARTIAL","DAG scheduler and handoff workflow primitives."),
SystemDomain("34","AI App Factory","PARTIAL","Production specification generator and AppFactory engine foundation."),
SystemDomain("35","Game Factory","PLANNED","Dedicated game-generation pipeline not yet integrated."),
SystemDomain("36","Project Workspaces","PLANNED","Persistent project-level workspace model remains to be implemented."),
SystemDomain("37","Artifact System","PARTIAL","Generated files and results are supported in individual flows; unified artifact registry remains."),
SystemDomain("38","Plugin / App Marketplace","PLANNED","Marketplace contract not yet implemented."),
SystemDomain("39","Connector System","PARTIAL","External service adapters exist; unified connector registry remains."),
SystemDomain("40","Control Center","IMPLEMENTED","Cognitive Control Center and system status surface."),
SystemDomain("41","Mobile Experience","IMPLEMENTED","Native Android Compose experience and premium NewGPT design system."),
SystemDomain("42","Web Experience","PLANNED","No separate web client is shipped in this Android artifact."),
SystemDomain("43","Release Engine","PARTIAL","CI builds release APK/AAB and debug APK; production Play signing remains external."),
SystemDomain("44","Research Lab","EXPERIMENTAL","Experimental verification, JEPA/HDC/SNN and advanced evolution concepts remain isolated.")
)

@Composable
fun SystemOverviewScreen(onBack: () -> Unit) {
    PremiumScaffold(
        selected = "Narzędzia",
        title = "NEWGPT SYSTEM",
        subtitle = "COGNITIVE OS · 44 DOMAINS",
        onBack = onBack,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = {},
        onSettingsNav = {}
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GoldCard(title = "FINAL INTEGRATION CONTROL DECK", icon = Icons.Default.SettingsSuggest) {
                    Text("Jedna mapa systemu: warstwy działające, częściowe, planowane i eksperymentalne.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("44 DOMAINS") }, leadingIcon = { Icon(Icons.Default.AccountTree, null) })
                        AssistChip(onClick = {}, label = { Text("EDGE + CLOUD") }, leadingIcon = { Icon(Icons.Default.AutoAwesome, null) })
                    }
                }
            }
            items(domains, key = { it.id }) { domain ->
                GoldCard(
                    title = "\${domain.id} · \${domain.name}",
                    icon = when {
                        domain.name.contains("Memory") -> Icons.Default.Memory
                        domain.name.contains("Security") -> Icons.Default.Security
                        else -> Icons.Default.SettingsSuggest
                    }
                ) {
                    Text(domain.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AssistChip(
                        onClick = {},
                        label = { Text(domain.status) },
                        colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
