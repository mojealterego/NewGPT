package com.mojealterego.newgpt.presentation.cognitive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.presentation.theme.*

@Composable
fun CognitiveScreen(onBack: () -> Unit, viewModel: CognitiveViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PremiumScaffold(
        selected = "Narzędzia",
        title = "COGNITIVE OS",
        subtitle = "MEMORY · REASONING · SECURITY · EVOLUTION",
        onBack = onBack,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = {},
        onSettingsNav = {}
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GoldCard(title = "NEWGPT COGNITIVE CONTROL CENTER", icon = Icons.Default.Psychology) {
                    Text("Pamięć, reasoning, bezpieczeństwo i ewolucja działają jako osobne warstwy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.memoryCount} zapisów bitemporal memory", color = MaterialTheme.colorScheme.primary)
                }
            }
            items(state.modules) { module ->
                GoldCard(title = module.name, icon = Icons.Default.Memory) {
                    Text(module.description)
                    AssistChip(onClick = {}, label = { Text(module.status) })
                }
            }
        }
    }
}
