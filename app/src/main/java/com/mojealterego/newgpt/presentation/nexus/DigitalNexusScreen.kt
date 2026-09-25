package com.mojealterego.newgpt.presentation.nexus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.domain.nexus.DigitalNexusCore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class NexusUiState(
    val modules: List<Pair<String, Boolean>> = emptyList(),
    val gMemory: Map<String, Int> = emptyMap(),
    val tools: Int = 0,
    val audits: Int = 0,
    val capabilities: Int = 0,
    val models: Int = 0,
    val p95LatencyMs: Long = 0,
    val requests: Long = 0,
    val failures: Long = 0
)

@HiltViewModel
class DigitalNexusViewModel @Inject constructor(
    private val core: DigitalNexusCore
) : ViewModel() {
    var state by mutableStateOf(NexusUiState())
        private set

    fun refresh() {
        viewModelScope.launch {
            val status = core.status()
            state = NexusUiState(
                modules = listOf(
                    "Bitemporal + PITR" to status.bitemporal,
                    "CoALA / episodic / procedural" to status.coala,
                    "G-Memory" to status.gMemory,
                    "HDC / holographic VSA" to status.hdc,
                    "GoT / R2 / decision / reflexion" to status.got,
                    "DGM / AlphaEvolve sandbox" to status.evolutionSandbox,
                    "Adversarial gating / red team" to status.adversarialGating,
                    "MCP Gateway" to status.mcpGateway,
                    "JEPA adapter" to status.jepaAdapter,
                    "SNN event gate" to status.snnAdapter,
                    "Formal verification adapter" to status.formalVerificationAdapter
                ),
                gMemory = core.gMemory.counts(),
                tools = core.gateway.listTools().size,
                audits = core.gateway.auditLog().size,
                capabilities = core.capabilityRegistry.all().size,
                models = core.modelCatalog.all().size,
                p95LatencyMs = core.observability.p95Latency(),
                requests = core.runtimeCounters.snapshot().first,
                failures = core.runtimeCounters.snapshot().second
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalNexusScreen(
    onBack: () -> Unit,
    viewModel: DigitalNexusViewModel = hiltViewModel()
) {
    val state = viewModel.state
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                title = {
                    Column {
                        Text("DIGITAL NEXUS CORE")
                        Text(
                            "MEMORY · REASONING · AGENTS · EVOLUTION",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nexus Runtime", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                        Text("Jedna warstwa orkiestrująca pamięć, rozumowanie, narzędzia i bezpieczną ewolucję.")
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("MCP tools: " + state.tools)
                            Text("audits: " + state.audits)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("capabilities: " + state.capabilities)
                            Text("models: " + state.models)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("p95: " + state.p95LatencyMs + " ms")
                            Text("requests: " + state.requests)
                            Text("failures: " + state.failures)
                        }
                    }
                }
            }

            item { SectionCard(Icons.Default.Memory, "MEMORY CORE", "CoALA · bitemporal · G-Memory · HDC") }
            items(state.modules.filter { it.first.contains("Bitemporal") || it.first.contains("CoALA") || it.first.contains("G-Memory") || it.first.contains("HDC") }) {
                ModuleRow(it.first, it.second)
            }

            item { SectionCard(Icons.Default.AccountTree, "REASONING CORE", "GoT · R2 · decision cycle · reflexion · adaptive search") }
            items(state.modules.filter { it.first.contains("GoT") }) {
                ModuleRow(it.first, it.second)
            }

            item { SectionCard(Icons.Default.AutoAwesome, "EVOLUTION CORE", "Digital Genotype · mutation loop · DGM/AlphaEvolve-inspired sandbox") }
            items(state.modules.filter { it.first.contains("DGM") || it.first.contains("Adversarial") }) {
                ModuleRow(it.first, it.second)
            }

            item { SectionCard(Icons.Default.Security, "INTEGRATION / SAFETY", "MCP policy gateway · JEPA/SNN adapters · formal verification") }
            items(state.modules.filter { it.first.contains("MCP") || it.first.contains("JEPA") || it.first.contains("SNN") || it.first.contains("Formal") }) {
                ModuleRow(it.first, it.second)
            }

            if (state.gMemory.isNotEmpty()) {
                item { Text("G-MEMORY LAYERS", style = androidx.compose.material3.MaterialTheme.typography.titleLarge) }
                items(state.gMemory.entries.toList()) { Text(it.key + ": " + it.value) }
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(detail, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ModuleRow(name: String, enabled: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name)
            Text(if (enabled) "READY" else "OFF")
        }
    }
}
