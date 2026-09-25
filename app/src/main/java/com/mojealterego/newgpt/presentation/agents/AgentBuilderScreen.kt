package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.presentation.theme.*

@Composable
fun AgentBuilderScreen(onBack: () -> Unit, viewModel: AgentBuilderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf<AgentDefinition?>(null) }
    LaunchedEffect(state.selectedId) { draft = state.selected?.copy() }

    PremiumScaffold(
        selected = "Agenci",
        title = "AGENT BUILDER",
        subtitle = "TWÓRZ · EDYTUJ · ZARZĄDZAJ AGENTAMI",
        onBack = onBack,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = {},
        onSettingsNav = {}
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoldButton("NOWY AGENT", { draft = viewModel.newAgent() }, Modifier.weight(1f), Icons.Default.Add)
                OutlineGoldButton("RESET", viewModel::reset, Modifier.weight(1f), Icons.Default.Refresh)
            }
            if (state.validationErrors.isNotEmpty()) {
                GoldCard(title = "BŁĘDY WALIDACJI", icon = Icons.Default.Warning, modifier = Modifier.padding(horizontal = 14.dp)) {
                    state.validationErrors.forEach { Text("• $it", color = MaterialTheme.colorScheme.error) }
                }
            }
            draft?.let { current ->
                AgentEditor(
                    agent = current,
                    onChange = { draft = it },
                    onSave = { viewModel.save(current); draft = current },
                    onDelete = { viewModel.delete(current.id); draft = null }
                )
            } ?: LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.agents, key = { it.id }) { agent ->
                    GoldCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(agent.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                Text(agent.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.delete(agent.id) }) { Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.primary) }
                        }
                        OutlineGoldButton(if (agent.id == state.selectedId) "EDYTOWANY" else "EDYTUJ", { viewModel.select(agent.id) }, icon = Icons.Default.Edit)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentEditor(
    agent: AgentDefinition,
    onChange: (AgentDefinition) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GoldCard(title = agent.name.ifBlank { "NOWY AGENT" }, icon = Icons.Default.Psychology) {
                TextField(agent.name, { onChange(agent.copy(name = it)) }, Modifier.fillMaxWidth(), label = { Text("Nazwa") })
                TextField(agent.description, { onChange(agent.copy(description = it)) }, Modifier.fillMaxWidth(), label = { Text("Opis") })
                TextField(agent.systemPrompt, { onChange(agent.copy(systemPrompt = it)) }, Modifier.fillMaxWidth(), minLines = 6, label = { Text("System prompt") })
                TextField(agent.skills.joinToString(", "), { onChange(agent.copy(skills = csv(it))) }, Modifier.fillMaxWidth(), label = { Text("Skills — po przecinku") })
                TextField(agent.tools.joinToString(", "), { onChange(agent.copy(tools = csv(it))) }, Modifier.fillMaxWidth(), label = { Text("Tools — po przecinku") })
            }
        }
        item {
            GoldCard(title = "NARZĘDZIA", icon = Icons.Default.Build) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(com.mojealterego.newgpt.domain.agent.AgentToolCatalog.builtIns.size) { index ->
                        val tool = com.mojealterego.newgpt.domain.agent.AgentToolCatalog.builtIns[index]
                        AssistChip(
                            onClick = {
                                val next = if (tool.id in agent.tools) agent.tools - tool.id else agent.tools + tool.id
                                onChange(agent.copy(tools = next))
                            },
                            label = { Text(tool.name) }
                        )
                    }
                }
                TextField(agent.handoffs.joinToString(", "), { onChange(agent.copy(handoffs = csv(it))) }, Modifier.fillMaxWidth(), label = { Text("Handoffs — ID agentów") })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Agent aktywny")
                    GoldSwitch(agent.enabled) { onChange(agent.copy(enabled = it)) }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoldButton("ZAPISZ", onSave, Modifier.weight(1f), Icons.Default.Save)
                OutlineGoldButton("USUŃ", onDelete, Modifier.weight(1f), Icons.Default.Delete)
            }
        }
    }
}

private fun csv(value: String): List<String> = value.split(",").map(String::trim).filter(String::isNotEmpty)
