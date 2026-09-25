package com.mojealterego.newgpt.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.R
import com.mojealterego.newgpt.domain.model.Message
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
    onCognitiveCore: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeAnd(action: () -> Unit) {
        scope.launch { drawerState.close(); action() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_newgpt),
                        contentDescription = "Logo MojeAlterego",
                        modifier = Modifier.size(88.dp),
                        tint = Color.Unspecified
                    )
                    Text("NEWGPT", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("MOJEALTEREGO AI CONTROL CENTER", style = MaterialTheme.typography.labelSmall)
                    HorizontalDivider()
                }
                NavigationDrawerItem(label = { Text("Rozmowa") }, selected = true, onClick = { scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text("Agenci") }, selected = false, onClick = { closeAnd(onAgents) })
                NavigationDrawerItem(label = { Text("Creative Studio") }, selected = false, onClick = { closeAnd(onStudio) })
                NavigationDrawerItem(label = { Text("Holographic Memory") }, selected = false, onClick = { closeAnd(onMemory) })
                NavigationDrawerItem(label = { Text("DGM · RSI Evolution Lab") }, selected = false, onClick = { closeAnd(onEvolution) })
                NavigationDrawerItem(label = { Text("AI App Builder") }, selected = false, onClick = { closeAnd(onBuilder) })
                NavigationDrawerItem(label = { Text("Cognitive Core") }, selected = false, onClick = { closeAnd(onCognitiveCore) })
                NavigationDrawerItem(label = { Text("Ustawienia") }, selected = false, onClick = { closeAnd(onSettings) })
                Column(
                    Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    HorizontalDivider()
                    Text("Wszystkie prawa zastrzeżone Mojealterego", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_newgpt),
                                contentDescription = "Mojealterego",
                                modifier = Modifier.size(42.dp),
                                tint = Color.Unspecified
                            )
                            Column(Modifier.padding(start = 10.dp)) {
                                Text("NewGPT", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                Text("MOJEALTEREGO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::clear, enabled = state.inputEnabled) { Icon(Icons.Default.Delete, "Wyczyść") }
                        IconButton(onClick = onStudio) { Icon(Icons.Default.AutoAwesome, "Creative Studio") }
                    }
                )
            },
            bottomBar = {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        enabled = state.inputEnabled,
                        placeholder = { Text("Napisz wiadomość…") },
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (text.isNotBlank()) {
                                viewModel.send(text)
                                text = ""
                            }
                        })
                    )
                    IconButton(
                        onClick = { viewModel.send(text); text = "" },
                        enabled = state.inputEnabled && text.isNotBlank()
                    ) { Icon(Icons.Default.Send, "Wyślij") }
                }
            }
        ) { padding ->
            if (state.messages.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 28.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_newgpt),
                            contentDescription = "Logo Mojealterego",
                            modifier = Modifier.size(132.dp),
                            tint = Color.Unspecified
                        )
                        Text("NewGPT", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text("MOJEALTEREGO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Box(Modifier.height(18.dp))
                        Text("Nowa rozmowa", style = MaterialTheme.typography.headlineSmall)
                        Text("Dostawcy AI · RAG · pamięć · internet · GGUF · Creative Studio", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LaunchedEffect(state.messages.size) {
                    if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.messages, key = { it.id }) { MessageBubble(it) }
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
        Text(
            text = message.content.ifBlank { "Generowanie…" },
            modifier = Modifier
                .alpha(if (message.isPending) 0.65f else 1f)
                .fillMaxWidth(if (message.isUser) 0.82f else 0.92f)
                .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
