package com.mojealterego.newgpt.presentation.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.R
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onAgents: () -> Unit,
    onSettings: () -> Unit,
    onStudio: () -> Unit,
    onMemory: () -> Unit,
    onEvolution: () -> Unit,
    onBuilder: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun go(action: () -> Unit) = scope.launch { drawerState.close(); action() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color(0x99000000),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF080809),
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(painterResource(R.drawable.ic_newgpt), "NewGPT", Modifier.size(82.dp), tint = Color.Unspecified)
                    Text("NewGPT", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("MOJEALTEREGO AI CONTROL CENTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(color = Color(0x664F3B12))
                }
                listOf(
                    "Rozmowa" to { },
                    "Agenci" to { go(onAgents) },
                    "Creative Studio" to { go(onStudio) },
                    "Holographic Memory" to { go(onMemory) },
                    "DGM · RSI Evolution Lab" to { go(onEvolution) },
                    "AI App Builder" to { go(onBuilder) },
                    "Ustawienia" to { go(onSettings) }
                ).forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(item.first) },
                        selected = index == 0,
                        onClick = item.second,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF4A3810),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) {
        NewGptBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    NewGptHeader(
                        onMenu = { scope.launch { drawerState.open() } },
                        onDelete = { viewModel.clear() },
                        onSettings = onSettings
                    )
                },
                bottomBar = {
                    NewGptBottomNav("Panel", {}, onAgents, onMemory, onStudio, onSettings)
                }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    if (state.messages.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Icon(
                                    painterResource(R.drawable.ic_newgpt),
                                    "NewGPT",
                                    Modifier.size(154.dp),
                                    tint = Color.Unspecified
                                )
                                Text("NewGPT", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                                Text("MOJEALTEREGO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, letterSpacing = 3.sp)
                                Spacer(Modifier.height(18.dp))
                                Text("Nowa rozmowa", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "Dostawcy AI · RAG · pamięć · internet · GGUF · Creative Studio",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LaunchedEffect(state.messages.size) {
                            listState.animateScrollToItem(state.messages.lastIndex)
                        }
                        LazyColumn(
                            state = listState,
                            Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.messages, key = { it.id }) { MessageBubble(it) }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            Modifier.weight(1f),
                            enabled = state.inputEnabled,
                            placeholder = { Text("Napisz wiadomość…") },
                            shape = RoundedCornerShape(28.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (text.isNotBlank()) { viewModel.send(text); text = "" }
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xCC101012),
                                unfocusedContainerColor = Color(0xCC101012),
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        IconButton(
                            onClick = { viewModel.send(text); text = "" },
                            enabled = state.inputEnabled && text.isNotBlank(),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary
                            ) { Icon(Icons.Default.Send, "Wyślij", tint = Color(0xFF050506), Modifier.padding(14.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (message.isUser) .82f else .92f).alpha(if (message.isPending) .65f else 1f),
            shape = RoundedCornerShape(18.dp),
            color = if (message.isUser) Color(0xFF3F3212) else Color(0xB50D0D10),
            border = BorderStroke(1.dp, if (message.isUser) Color(0xAAE1B84A) else Color(0x554D432F))
        ) {
            Text(
                message.content.ifBlank { "Generowanie…" },
                Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
