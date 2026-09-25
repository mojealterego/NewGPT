package com.mojealterego.newgpt.presentation.memory

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.mojealterego.newgpt.data.local.MemoryGraph
import com.mojealterego.newgpt.data.local.MemoryGraphStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(private val store: MemoryGraphStore) : ViewModel() {
    var graph by mutableStateOf(MemoryGraph())
        private set
    init { refresh() }
    fun refresh() { viewModelScope.launch { graph = store.graph() } }
    fun clearWorking() { viewModelScope.launch { store.clearWorking(); graph = store.graph() } }
    fun clearAll() { viewModelScope.launch { store.clearAll(); graph = MemoryGraph() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(onBack: () -> Unit, viewModel: MemoryViewModel = hiltViewModel()) {\n    BrandBackground {
    val graph = viewModel.graph
    var showPermanent by remember { mutableStateOf(true) }
    Scaffold(\n            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("HOLOGRAPHIC MEMORY") },
                navigationIcon = { Button(onClick = onBack) { Text("‹") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pamięć robocza + pamięć stała + graf skojarzeń", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Widok holograficzny jest projekcją grafu: węzły reprezentują wspomnienia i pojęcia, a krawędzie ich relacje.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pokaż pamięć stałą")
                Switch(checked = showPermanent, onCheckedChange = { showPermanent = it })
            }
            Card(Modifier.fillMaxWidth()) { MemoryGraphCanvas(graph, showPermanent) }
            Text("Węzły: " + graph.nodes.size + " · relacje: " + graph.edges.size)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::refresh, modifier = Modifier.weight(1f)) { Text("ODŚWIEŻ") }
                Button(onClick = viewModel::clearWorking, modifier = Modifier.weight(1f)) { Text("WYCZYŚĆ ROBOCZĄ") }
            }
            Button(onClick = viewModel::clearAll, modifier = Modifier.fillMaxWidth()) { Text("WYCZYŚĆ CAŁĄ PAMIĘĆ") }
        }
    }
\n    }\n}

@Composable
private fun MemoryGraphCanvas(graph: MemoryGraph, showPermanent: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    Canvas(Modifier.fillMaxWidth().height(420.dp).padding(8.dp)) {
        val visible = graph.nodes.filter { showPermanent || it.type != "permanent" }.take(80)
        if (visible.isEmpty()) return@Canvas
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.minDimension * 0.36f).coerceAtLeast(80f)
        val positions = visible.mapIndexed { index, node ->
            val angle = (index.toFloat() / visible.size) * (Math.PI * 2).toFloat()
            node.id to Offset(center.x + kotlin.math.cos(angle) * radius, center.y + kotlin.math.sin(angle) * radius)
        }.toMap()
        graph.edges.forEach { edge ->
            val a = positions[edge.from]
            val b = positions[edge.to]
            if (a != null && b != null) drawLine(primary, a, b, strokeWidth = 2f)
        }
        visible.forEach { node ->
            val p = positions[node.id] ?: return@forEach
            drawCircle(primary, radius = if (node.type == "concept") 10f else 15f, center = p)
            drawCircle(background, radius = 4f, center = p)
            drawCircle(primary, radius = 15f, center = p, style = Stroke(width = 2f))
        }
    }
}
