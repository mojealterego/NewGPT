package com.mojealterego.newgpt.presentation.agents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.presentation.theme.*


@Composable
private fun agentRole(id: String): String = when (id) {
    "coordinator" -> "GŁÓWNY AGENT"
    "researcher" -> "BADANIA I ANALIZY"
    "architect" -> "ARCHITEKT SYSTEMÓW"
    "coder" -> "PROGRAMOWANIE"
    "writer" -> "TEKSTY I PUBLIKACJE"
    "wda-photo" -> "EDYCJA I FOTO"
    "mobile-operator" -> "ANDROID I ADB"
    "rag-master" -> "BAZA WIEDZY"
    "web-researcher" -> "WYSZUKIWANIE WWW"
    "creative-director" -> "KREACJA I WIZJA"
    "gguf-engineer" -> "MODELE LLM"
    "memory-architect" -> "PAMIĘĆ I KONTEKST"
    "evolution-engineer" -> "ROZWÓJ I AUTOMATYZACJA"
    else -> "AGENT NEWGPT"
}

@Composable
fun AgentsScreen(
    onBack: () -> Unit,
    onBuilder: () -> Unit,
    onSettings: () -> Unit,
    viewModel: AgentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var task by remember { mutableStateOf("") }
    val selected = state.selectedAgent ?: state.agents.firstOrNull()

    BrandBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BrandGlobalHeader(
                    onMenu = onBack,
                    onSettings = onSettings
                )
            },
            bottomBar = {
                TaskComposer(
                    value = task,
                    onValueChange = { task = it },
                    enabled = state.inputEnabled && selected != null,
                    onSend = {
                        if (task.isNotBlank()) {
                            viewModel.send(task)
                            task = ""
                        }
                    }
                )
            }
        ) { padding ->
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AgentRegistry(
                    agents = state.agents.filter { it.enabled },
                    selectedId = selected?.id,
                    onSelect = viewModel::selectAgent,
                    modifier = Modifier.weight(0.47f)
                )
                AgentDetail(
                    agent = selected,
                    messages = state.messages,
                    onBuilder = onBuilder,
                    onRunPipeline = {
                        if (task.isNotBlank()) {
                            viewModel.runHandoffPipeline(task)
                            task = ""
                        }
                    },
                    modifier = Modifier.weight(0.53f)
                )
            }
        }
    }
}

@Composable
private fun AgentRegistry(
    agents: List<AgentDefinition>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(BrandPalette.AnilineBlack.copy(alpha = 0.93f))
            .border(1.5.dp, BrandPalette.Gold.copy(alpha = 0.85f), RoundedCornerShape(20.dp)),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(agents, key = { it.id }) { agent ->
            val selected = agent.id == selectedId
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(
                            listOf(
                                BrandPalette.Gold.copy(alpha = 0.42f),
                                BrandPalette.GoldDeep.copy(alpha = 0.10f)
                            )
                        ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onSelect(agent.id) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AgentPortrait(agent.id, size = 52.dp)
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        agent.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) BrandPalette.Ivory else BrandPalette.GoldBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        agentRole(agent.id),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandPalette.Titanium,
                        maxLines = 1
                    )
                }
                Text("›", color = BrandPalette.GoldBright, style = MaterialTheme.typography.headlineSmall)
            }
            GoldRule(Modifier.padding(horizontal = 8.dp))
        }
    }
}

@Composable
private fun AgentDetail(
    agent: AgentDefinition?,
    messages: List<Message>,
    onBuilder: () -> Unit,
    onRunPipeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (agent == null) return

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                agent.name,
                style = MaterialTheme.typography.headlineSmall,
                color = BrandPalette.GoldBright,
                fontWeight = FontWeight.Bold
            )
            Text(
                agent.description,
                style = MaterialTheme.typography.bodySmall,
                color = BrandPalette.Ivory
            )
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandPalette.Gold.copy(alpha = 0.30f),
                                BrandPalette.AnilineBlack.copy(alpha = 0.95f),
                                BrandPalette.Obsidian
                            )
                        )
                    )
                    .border(1.5.dp, BrandPalette.Gold, RoundedCornerShape(30.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AgentPortrait(agent.id, size = 205.dp)
                    Text(
                        agent.name.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrandPalette.Ivory,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        agentRole(agent.id),
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandPalette.GoldBright
                    )
                }
            }
        }

        item {
            LuxuryCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("AGENT PROFILE", color = BrandPalette.GoldBright, fontWeight = FontWeight.Bold)
                    Text(agent.description, color = BrandPalette.Ivory)
                    if (agent.skills.isNotEmpty()) {
                        Text("SKILLS", color = BrandPalette.GoldBright, style = MaterialTheme.typography.labelMedium)
                        Text(agent.skills.joinToString(" · "), color = BrandPalette.Titanium)
                    }
                    if (agent.tools.isNotEmpty()) {
                        Text("TOOLS", color = BrandPalette.GoldBright, style = MaterialTheme.typography.labelMedium)
                        Text(agent.tools.joinToString(" · "), color = BrandPalette.Titanium)
                    }
                }
            }
        }

        if (agent.handoffs.isNotEmpty()) {
            item {
                LuxuryCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("URUCHOM PIPELINE", color = BrandPalette.GoldBright, fontWeight = FontWeight.Bold)
                        agent.handoffs.forEach { next ->
                            AssistChip(
                                onClick = {},
                                label = { Text(next, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Default.ArrowForward, null, Modifier.size(13.dp)) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onRunPipeline,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPalette.GoldBright,
                    contentColor = BrandPalette.Obsidian
                )
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("AGENT GOTOWY", fontWeight = FontWeight.Black)
                    Text("Wybierz agenta i zleć mu zadanie.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onBuilder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPalette.GoldBright)
            ) {
                Icon(Icons.Default.Settings, null)
                Spacer(Modifier.width(8.dp))
                Text("OTWÓRZ AGENT BUILDER")
            }
        }

        items(messages, key = { it.id }) { message ->
            AgentMessage(message, agent.name)
        }
    }
}

@Composable
private fun TaskComposer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(BrandPalette.AnilineBlack.copy(alpha = 0.98f))
            .border(1.5.dp, BrandPalette.GoldBright, RoundedCornerShape(30.dp))
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Zleć zadanie agentowi…", color = BrandPalette.Titanium) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        IconButton(
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(BrandPalette.GoldBright)
        ) {
            Icon(Icons.Default.Send, "Wyślij", tint = BrandPalette.Obsidian)
        }
    }
}

@Composable
private fun AgentMessage(message: Message, agentName: String) {
    LuxuryCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                if (message.isUser) "TY" else agentName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = BrandPalette.GoldBright
            )
            Text(
                message.content.ifBlank { "Generowanie…" },
                color = BrandPalette.Ivory,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
