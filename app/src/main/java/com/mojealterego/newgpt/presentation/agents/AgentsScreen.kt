package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onBuilder: () -> Unit,
    onSettings: () -> Unit,
    viewModel: AgentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

    BrandBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("AGENT COMMAND", style = MaterialTheme.typography.titleLarge)
                            Text("MOJEALTEREGO · MULTI-AGENT SYSTEM", style = MaterialTheme.typography.labelSmall, color = BrandPalette.GoldBright)
                        }
                    },
                    actions = {
                        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Ustawienia", tint = BrandPalette.Titanium) }
                        IconButton(onClick = onBuilder) { Icon(Icons.Default.Settings, "Agent Builder", tint = BrandPalette.GoldBright) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        enabled = state.inputEnabled && state.selectedAgent != null,
                        placeholder = { Text("Zleć zadanie wybranemu agentowi…") },
                        shape = RoundedCornerShape(20.dp)
                    )
                    IconButton(onClick = { viewModel.send(text); text = "" }, enabled = state.inputEnabled && text.isNotBlank()) {
                        Icon(Icons.Default.Send, "Wyślij", tint = BrandPalette.GoldBright)
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LuxuryCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            BrandSectionLabel("AGENT REGISTRY")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                AgentIdentity(state.selectedAgent?.id ?: "coordinator", state.selectedAgent?.name ?: "Coordinator")
                                Box {
                                    IconButton(onClick = { menuOpen = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Wybierz agenta", tint = BrandPalette.GoldBright)
                                    }
                                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                        state.agents.filter { it.enabled }.forEach { agent ->
                                            DropdownMenuItem(
                                                text = { Text(agent.name) },
                                                onClick = { viewModel.selectAgent(agent.id); menuOpen = false }
                                            )
                                        }
                                    }
                                }
                            }
                            GoldRule()
                            state.selectedAgent?.let { agent ->
                                if (agent.id in listOf("coordinator", "researcher", "architect")) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AgentPortrait(
                                            agent.id,
                                            size = when (agent.id) {
                                                "coordinator" -> 104.dp
                                                "researcher" -> 112.dp
                                                else -> 116.dp
                                            }
                                        )
                                        Column(
                                            Modifier.padding(start = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            BrandSectionLabel(
                                                when (agent.id) {
                                                    "coordinator" -> "AGENT 01"
                                                    "researcher" -> "AGENT 02"
                                                    else -> "AGENT 03"
                                                }
                                            )
                                            Text(
                                                when (agent.id) {
                                                    "coordinator" -> "MASTER ORCHESTRATOR"
                                                    "researcher" -> "DEEP RESEARCH AGENT"
                                                    else -> "SYSTEM ARCHITECT"
                                                },
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = BrandPalette.GoldBright
                                            )
                                            Text(
                                                when (agent.id) {
                                                    "coordinator" -> "Central command · planning · delegation · verification"
                                                    "researcher" -> "Information · analysis · sources · facts · insights · verification"
                                                    else -> "System design · architecture · data flow · infrastructure · scalability · integration · security"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = BrandPalette.Titanium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    GoldRule()
                                }
                                Text(agent.description, color = BrandPalette.Ivory)
                                Text("SKILLS · ${agent.skills.joinToString(" · ")}", style = MaterialTheme.typography.labelSmall, color = BrandPalette.Titanium)
                                if (agent.tools.isNotEmpty()) {
                                    Text("TOOLS · ${agent.tools.joinToString(" · ")}", style = MaterialTheme.typography.labelSmall, color = BrandPalette.Titanium)
                                }
                                if (agent.handoffs.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { viewModel.runHandoffPipeline(text) },
                                        enabled = state.inputEnabled && text.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("PIPELINE  ${agent.handoffs.joinToString("  →  ")}")
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.messages.isEmpty()) {
                    item {
                        LuxuryCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AgentIdentity(state.selectedAgent?.id ?: "coordinator", state.selectedAgent?.name ?: "Coordinator")
                                Text("Agent gotowy", style = MaterialTheme.typography.headlineSmall, color = BrandPalette.GoldBright)
                                Text("Każdy agent ma własną specjalizację, zestaw narzędzi i możliwe handoffy.", color = BrandPalette.Titanium)
                                Button(onClick = onBuilder) { Text("OTWÓRZ AGENT BUILDER") }
                            }
                        }
                    }
                } else {
                    items(state.messages, key = { it.id }) { AgentMessage(it, state.selectedAgent?.name.orEmpty()) }
                }
            }
        }
    }
}

@Composable
private fun AgentMessage(message: Message, agentName: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start) {
        LuxuryCard(Modifier.fillMaxWidth(if (message.isUser) 0.84f else 0.94f)) {
            Column(Modifier.padding(14.dp)) {
                Text(if (message.isUser) "TY" else agentName.uppercase(), style = MaterialTheme.typography.labelMedium, color = BrandPalette.GoldBright)
                Text(message.content.ifBlank { "Generowanie…" }, style = MaterialTheme.typography.bodyLarge, color = BrandPalette.Ivory, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}
