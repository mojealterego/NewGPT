package com.mojealterego.newgpt.presentation.nexus

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val audits: Int = 0
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
                audits = core.gateway.auditLog().size
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
    val transition = rememberInfiniteTransition(label = "nexus")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) { viewModel.refresh() }

    val ready = state.modules.count { it.second }
    val total = state.modules.size.coerceAtLeast(1)
    val readiness = ready.toFloat() / total

    Scaffold(
        containerColor = Color(0xFF07080C),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Bolt, contentDescription = "Wstecz")
                    }
                },
                title = {
                    Column {
                        Text("DIGITAL NEXUS", fontWeight = FontWeight.Bold)
                        Text(
                            "INTELLIGENCE CONTROL DECK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB7A36A)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0D13), Color(0xFF07080C), Color(0xFF111018))
                )
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141C))
                    ) {
                        Box(
                            Modifier.fillMaxWidth().padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("NEXUS RUNTIME", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Adaptive · Private · Governed", color = Color(0xFF9EA3B2))
                                    }
                                    Box(
                                        Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFB79A45).copy(alpha = pulse))
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                                Text(
                                    "$ready / $total subsystems online",
                                    color = Color(0xFFE9D18A),
                                    fontWeight = FontWeight.SemiBold
                                )
                                LinearProgressIndicator(
                                    progress = { readiness },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
                                    color = Color(0xFFD6B85A),
                                    trackColor = Color(0xFF292B34)
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatPill("TOOLS", state.tools.toString())
                                    StatPill("AUDIT", state.audits.toString())
                                    StatPill("MEMORY", state.gMemory.values.sum().toString())
                                }
                            }
                        }
                    }
                }

                item { CoreSection(Icons.Default.Memory, "MEMORY FABRIC", "Bitemporal · CoALA · G-Memory · HDC") }
                items(state.modules.filter { it.first.contains("Bitemporal") || it.first.contains("CoALA") || it.first.contains("G-Memory") || it.first.contains("HDC") }) {
                    ModuleRow(it.first, it.second)
                }

                item { CoreSection(Icons.Default.AccountTree, "REASONING FABRIC", "GoT · R2 · decision cycle · reflexion · adaptive search") }
                items(state.modules.filter { it.first.contains("GoT") }) {
                    ModuleRow(it.first, it.second)
                }

                item { CoreSection(Icons.Default.AutoAwesome, "EVOLUTION LAB", "DGM / AlphaEvolve-inspired sandbox · adversarial gates · human approval") }
                items(state.modules.filter { it.first.contains("DGM") || it.first.contains("Adversarial") }) {
                    ModuleRow(it.first, it.second)
                }

                item { CoreSection(Icons.Default.Security, "TRUST FABRIC", "MCP policy · JEPA/SNN adapters · formal verification") }
                items(state.modules.filter { it.first.contains("MCP") || it.first.contains("JEPA") || it.first.contains("SNN") || it.first.contains("Formal") }) {
                    ModuleRow(it.first, it.second)
                }

                if (state.gMemory.isNotEmpty()) {
                    item { Text("G-MEMORY LAYERS", color = Color(0xFFE9D18A), fontWeight = FontWeight.Bold) }
                    items(state.gMemory.entries.toList()) {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10121A))
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(it.key, color = Color(0xFFD7D9E0))
                                Text(it.value.toString(), color = Color(0xFFE9D18A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("SYNCHRONIZUJ NEXUS")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CoreSection(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFD6B85A))
        Spacer(Modifier.size(10.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(detail, color = Color(0xFF858B9A), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ModuleRow(name: String, enabled: Boolean) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10121A))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = Color(0xFFD7D9E0), modifier = Modifier.weight(1f))
            Text(
                if (enabled) "ONLINE" else "OFF",
                color = if (enabled) Color(0xFFE9D18A) else Color(0xFF707582),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(if (enabled) 1f else .65f)
            )
        }
    }
}

@Composable
private fun RowScope.StatPill(label: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C25))
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(label, color = Color(0xFF777D8B), style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
