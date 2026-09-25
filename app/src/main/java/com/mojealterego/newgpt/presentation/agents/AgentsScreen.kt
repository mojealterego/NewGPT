package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun AgentsScreen(
    onBuilder: () -> Unit,
    onSettings: () -> Unit,
    onCognitive: () -> Unit = {},
    viewModel: AgentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size, state.selectedAgentId) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    PremiumScaffold(
        selected = "Agenci",
        title = state.selectedAgent?.name ?: "AGENTS",
        subtitle = "NEWGPT · AGENT CONTROL CENTER",
        onSettings = onSettings,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = onCognitive,
        onSettingsNav = onSettings
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlineGoldButton("WYBIERZ AGENTA", { menuOpen = true }, Modifier.fillMaxWidth(), Icons.Default.SmartToy)
                    DropdownMenu(menuOpen, { menuOpen = false }) {
                        state.agents.filter { it.enabled }.forEach { agent ->
                            DropdownMenuItem(
                                text = { Text(agent.name) },
                                onClick = { viewModel.selectAgent(agent.id); menuOpen = false }
                            )
                        }
                    }
                }
                OutlineGoldButton("BUILDER", onBuilder, icon = Icons.Default.Settings)
            }
            state.selectedAgent?.let { agent ->
                GoldCard(title = "AGENT ${agent.name.uppercase()}", icon = Icons.Default.Psychology, modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text(agent.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("skills: " + agent.skills.joinToString(", "), color = MaterialTheme.colorScheme.primary)
                    if (agent.handoffs.isNotEmpty()) {
                        Text("PIPELINE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(agent.handoffs.joinToString("  →  "), color = MaterialTheme.colorScheme.onSurface)
                        GoldButton(
                            "URUCHOM PIPELINE",
                            { viewModel.runHandoffPipeline(text) },
                            enabled = text.isNotBlank(),
                            icon = Icons.Default.AccountTree
                        )
                    }
                }
            }
            if (state.messages.isEmpty()) {
                Column(
                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(70.dp))
                    Text("Agent gotowy", style = MaterialTheme.typography.headlineSmall)
                    Text("Wybierz agenta i zleć mu zadanie.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    GoldButton("OTWÓRZ AGENT BUILDER", onBuilder, Modifier.padding(top = 14.dp), Icons.Default.Settings)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.messages, key = { it.id }) { AgentMessage(it, state.selectedAgent?.name.orEmpty()) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    Modifier.weight(1f),
                    enabled = state.inputEnabled && state.selectedAgent != null,
                    placeholder = { Text("Zleć zadanie agentowi…") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xCC101012),
                        unfocusedContainerColor = Color(0xCC101012)
                    )
                )
                IconButton(
                    onClick = { viewModel.send(text); text = "" },
                    enabled = state.inputEnabled && text.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, "Wyślij", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun AgentMessage(message: Message, agentName: String) {
    GoldCard {
        Text(if (message.isUser) "TY" else agentName.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        Text(message.content.ifBlank { "Generowanie…" }, style = MaterialTheme.typography.bodyLarge)
    }
}
