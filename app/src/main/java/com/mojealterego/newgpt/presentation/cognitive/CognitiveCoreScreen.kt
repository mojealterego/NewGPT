package com.mojealterego.newgpt.presentation.cognitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.semantics.contentDescription\nimport androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.GoTGraph
import com.mojealterego.newgpt.data.local.GoTStore
import com.mojealterego.newgpt.data.local.LocalRagStore
import com.mojealterego.newgpt.data.local.TitansMemoryStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class CognitiveUiState(
    val loading: Boolean = true,
    val ragChunks: Int = 0,
    val gotNodes: Int = 0,
    val gotEdges: Int = 0,
    val titanMemories: List<String> = emptyList(),
    val gotRecent: List<String> = emptyList()
)

@HiltViewModel
class CognitiveCoreViewModel @Inject constructor(
    private val rag: LocalRagStore,
    private val got: GoTStore,
    private val titans: TitansMemoryStore
) : ViewModel() {
    var state by mutableStateOf(CognitiveUiState())
        private set

    fun refresh() {
        viewModelScope.launch {
            val graph: GoTGraph = got.recent(80)
            val memories = titans.retrieve("recent conversation cognitive memory", 8)
            state = CognitiveUiState(
                loading = false,
                ragChunks = rag.count(),
                gotNodes = graph.nodes.size,
                gotEdges = graph.edges.size,
                titanMemories = memories.map { it.value.take(220) },
                gotRecent = graph.nodes.takeLast(12).reversed().map { "[${it.type}] ${it.content.take(220)}" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveCoreScreen(
    onBack: () -> Unit,
    onNexusCore: () -> Unit,
    viewModel: CognitiveCoreViewModel = hiltViewModel()
) {
    val state = viewModel.state
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Wstecz")
                    }
                },
                title = {
                    Column {
                        Text("COGNITIVE CORE")
                        Text("RAG · GoT · TITANS · RUST", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cognitive Core 2.1", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                        Text("Stan warstw pamięci i retrieval w czasie rzeczywistym.")
                        LinearProgressIndicator(
                            progress = { if (state.loading) 0.25f else 1f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                Button(onClick = onNexusCore, modifier = Modifier.fillMaxWidth()) {
                    Text("OTWÓRZ DIGITAL NEXUS CORE", modifier = Modifier.semantics { contentDescription = "Otwórz Digital Nexus Core" })
                }
            }
            item { MetricCard(Icons.Default.Memory, "RAG chunks", state.ragChunks.toString(), "Lokalny hybrid retrieval") }
            item { MetricCard(Icons.Default.Timeline, "GoT graph", "${state.gotNodes} nodes · ${state.gotEdges} edges", "Jawne podsumowania i zależności") }
            item { MetricCard(Icons.Default.Speed, "Rust hot path", "READY / BENCHMARK", "Cosine + surprise; wynik benchmarku z CI") }
            if (state.titanMemories.isNotEmpty()) {
                item { Text("TITANS MEMORY", style = androidx.compose.material3.MaterialTheme.typography.titleLarge) }
                items(state.titanMemories) { Text(it, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) }
            }
            if (state.gotRecent.isNotEmpty()) {
                item { Text("RECENT GoT", style = androidx.compose.material3.MaterialTheme.typography.titleLarge) }
                items(state.gotRecent) { Text(it, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) }
            }
        }
    }
}

@Composable
private fun MetricCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(value, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                Text(detail, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
    }
}
