package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField\nimport androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onBuilder: () -> Unit,
    onSettings: () -> Unit,\n    onCognitive: () -> Unit = {},
    viewModel: AgentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.selectedAgentId) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.selectedAgent?.name ?: "Agenci") },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.ArrowDropDown, "Wybierz agenta") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            state.agents.filter { it.enabled }.forEach { agent ->
                                DropdownMenuItem(
                                    text = { Text(agent.name) },
                                    onClick = { viewModel.selectAgent(agent.id); menuOpen = false }
                                )
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onCognitive) { Text("Cognitive OS") }
                    IconButton(onClick = viewModel::clear) { Icon(Icons.Default.Delete, "Wyczyść") }
                    IconButton(onClick = onBuilder) { Icon(Icons.Default.Settings, "Agent Builder") }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f),
                    enabled = state.inputEnabled && state.selectedAgent != null,
                    placeholder = { Text("Zleć zadanie agentowi…") }
                )
                IconButton(
                    onClick = { viewModel.send(text); text = "" },
                    enabled = state.inputEnabled && text.isNotBlank()
                ) { Icon(Icons.Default.Send, "Wyślij") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.selectedAgent?.let {
                Text(
                    "${it.description}  •  skills: ${it.skills.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                if (it.handoffs.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.runHandoffPipeline(text) },
                        enabled = state.inputEnabled && text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) { Text("Uruchom pipeline: ${it.handoffs.joinToString(" → ")}") }
                }
            }
            if (state.messages.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Agent gotowy", style = MaterialTheme.typography.headlineSmall)
                    Text("Wybierz agenta i zleć mu zadanie.")
                    Button(onClick = onBuilder, modifier = Modifier.padding(top = 12.dp)) { Text("Otwórz Agent Builder") }
                }
            } else {
                LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.messages, key = { it.id }) { AgentMessage(it, state.selectedAgent?.name.orEmpty()) }
                }
            }
        }
    }
}

@Composable
private fun AgentMessage(message: Message, agentName: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(if (message.isUser) "Ty" else agentName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(message.content.ifBlank { "Generowanie…" }, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 3.dp))
    }
}
