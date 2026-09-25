package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentBuilderScreen(onBack: () -> Unit, viewModel: AgentBuilderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf<AgentDefinition?>(null) }
    LaunchedEffect(state.selectedId) { draft = state.selected?.copy() }

    BrandBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Column { Text("AGENT BUILDER"); Text("MOJEALTEREGO · AGENT LAB", style = MaterialTheme.typography.labelSmall, color = BrandPalette.GoldBright) } },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Wstecz", tint = BrandPalette.GoldBright) } },
                    actions = {
                        IconButton(onClick = { draft = viewModel.newAgent() }) { Icon(Icons.Default.Add, "Nowy agent", tint = BrandPalette.GoldBright) }
                        IconButton(onClick = viewModel::reset) { Icon(Icons.Default.Refresh, "Przywróć domyślne", tint = BrandPalette.Titanium) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { BrandSectionLabel("AGENT REGISTRY · ${state.agents.size} AGENTS") }
                    items(state.agents, key = { it.id }) { agent ->
                        LuxuryCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AgentIdentity(agent.id, agent.name)
                                Text(agent.description, color = BrandPalette.Titanium)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.select(agent.id) }, modifier = Modifier.weight(1f)) {
                                        Text(if (agent.id == state.selectedId) "EDYTUJEMY" else "EDYTUJ")
                                    }
                                    IconButton(onClick = { viewModel.delete(agent.id) }) {
                                        Icon(Icons.Default.Delete, "Usuń", tint = BrandPalette.Burgundy)
                                    }
                                }
                            }
                        }
                    }
                    if (state.validationErrors.isNotEmpty()) {
                        item {
                            LuxuryCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    Text("VALIDATION", color = MaterialTheme.colorScheme.error)
                                    state.validationErrors.forEach { Text("• $it", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
                draft?.let { current ->
                    LuxuryCard(Modifier.fillMaxWidth().padding(12.dp)) {
                        AgentEditor(
                            agent = current,
                            onChange = { draft = it },
                            onSave = { viewModel.save(current); draft = current },
                            onDelete = { viewModel.delete(current.id); draft = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentEditor(agent: AgentDefinition, onChange: (AgentDefinition) -> Unit, onSave: () -> Unit, onDelete: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            AgentPortrait(
                agent.id,
                size = when (agent.id) {
                    "coordinator" -> 96.dp
                    "researcher" -> 88.dp
                    "architect" -> 96.dp
                    "coder" -> 100.dp
                    else -> 64.dp
                }
            )
            Column(Modifier.padding(start = 14.dp)) {
                when (agent.id) {
                    "coordinator" -> BrandSectionLabel("AGENT 01 · MASTER ORCHESTRATOR")
                    "researcher" -> BrandSectionLabel("AGENT 02 · DEEP RESEARCH AGENT")
                    "architect" -> BrandSectionLabel("AGENT 03 · SYSTEM ARCHITECT")
                    "coder" -> BrandSectionLabel("AGENT 04 · AI DEVELOPMENT AGENT")
                    else -> BrandSectionLabel("AGENT PROFILE")
                }
                Text(agent.name, style = MaterialTheme.typography.headlineSmall, color = BrandPalette.Ivory)
                Text(agent.id.uppercase(), style = MaterialTheme.typography.labelSmall, color = BrandPalette.GoldBright)
            }
        }
        GoldRule()
        TextField(value = agent.name, onValueChange = { onChange(agent.copy(name = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Nazwa") })
        TextField(value = agent.description, onValueChange = { onChange(agent.copy(description = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Opis") })
        TextField(value = agent.systemPrompt, onValueChange = { onChange(agent.copy(systemPrompt = it)) }, modifier = Modifier.fillMaxWidth(), minLines = 5, label = { Text("System prompt") })
        TextField(value = agent.skills.joinToString(", "), onValueChange = { onChange(agent.copy(skills = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Skills — po przecinku") })
        TextField(value = agent.tools.joinToString(", "), onValueChange = { onChange(agent.copy(tools = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Tools — po przecinku") })
        Text("KATALOG NARZĘDZI", style = MaterialTheme.typography.titleSmall, color = BrandPalette.GoldBright)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(com.mojealterego.newgpt.domain.agent.AgentToolCatalog.builtIns) { tool ->
                AssistChip(
                    onClick = {
                        val next = if (tool.id in agent.tools) agent.tools - tool.id else agent.tools + tool.id
                        onChange(agent.copy(tools = next))
                    },
                    label = { Text(tool.name) }
                )
            }
        }
        Text("HANDOFFS · " + if (agent.handoffs.isEmpty()) "brak" else agent.handoffs.joinToString(" → "), color = BrandPalette.Titanium)
        TextField(value = agent.handoffs.joinToString(", "), onValueChange = { onChange(agent.copy(handoffs = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Handoffs — ID agentów") })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Agent aktywny", color = BrandPalette.Ivory)
            Switch(checked = agent.enabled, onCheckedChange = { onChange(agent.copy(enabled = it)) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("ZAPISZ AGENTA") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń", tint = BrandPalette.Burgundy) }
        }
    }
}
private fun csv(value: String): List<String> = value.split(",").map(String::trim).filter(String::isNotEmpty)
