package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.agent.AgentDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentBuilderScreen(onBack: () -> Unit, viewModel: AgentBuilderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf<AgentDefinition?>(null) }

    LaunchedEffect(state.selectedId) { draft = state.selected?.copy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Builder") },
                navigationIcon = { IconButton(onClick = onBack) { Text("‹") } },
                actions = {
                    IconButton(onClick = { draft = viewModel.newAgent() }) { Icon(Icons.Default.Add, "Nowy agent") }
                    IconButton(onClick = viewModel::reset) { Icon(Icons.Default.Refresh, "Przywróć domyślne") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(state.agents, key = { it.id }) { agent ->
                    ListItem(
                        headlineContent = { Text(agent.name) },
                        supportingContent = { Text(agent.description) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.delete(agent.id) }) {
                                Icon(Icons.Default.Delete, "Usuń")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.select(agent.id) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) { Text(if (agent.id == state.selectedId) "Edytowany" else "Edytuj") }
                }
            }
            if (state.validationErrors.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Nie można zapisać agenta:", color = MaterialTheme.colorScheme.error)
                    state.validationErrors.forEach { error ->
                        Text("• $error", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            draft?.let { current ->
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

@Composable
private fun AgentEditor(
    agent: AgentDefinition,
    onChange: (AgentDefinition) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(value = agent.name, onValueChange = { onChange(agent.copy(name = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Nazwa") })
        TextField(value = agent.description, onValueChange = { onChange(agent.copy(description = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Opis") })
        TextField(value = agent.systemPrompt, onValueChange = { onChange(agent.copy(systemPrompt = it)) }, modifier = Modifier.fillMaxWidth(), minLines = 5, label = { Text("System prompt") })
        TextField(value = agent.skills.joinToString(", "), onValueChange = { onChange(agent.copy(skills = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Skills — po przecinku") })
        TextField(value = agent.tools.joinToString(", "), onValueChange = { onChange(agent.copy(tools = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Tools — po przecinku") })
        Text("Katalog narzędzi", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
        Text(
            "Handoffs: " + if (agent.handoffs.isEmpty()) "brak" else agent.handoffs.joinToString(" → "),
            style = MaterialTheme.typography.bodySmall
        )
        TextField(value = agent.handoffs.joinToString(", "), onValueChange = { onChange(agent.copy(handoffs = csv(it))) }, modifier = Modifier.fillMaxWidth(), label = { Text("Handoffs — ID agentów") })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Agent aktywny")
            Switch(checked = agent.enabled, onCheckedChange = { onChange(agent.copy(enabled = it)) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Zapisz agenta") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń") }
        }
    }
}

private fun csv(value: String): List<String> = value.split(",").map(String::trim).filter(String::isNotEmpty)
