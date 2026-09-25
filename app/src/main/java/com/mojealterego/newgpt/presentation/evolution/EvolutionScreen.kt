package com.mojealterego.newgpt.presentation.evolution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.EvolutionLabStore
import com.mojealterego.newgpt.data.local.EvolutionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvolutionViewModel @Inject constructor(private val store: EvolutionLabStore) : ViewModel() {
    var state by mutableStateOf(EvolutionState())
        private set
    init { refresh() }
    fun refresh() { viewModelScope.launch { state = store.state() } }
    fun updateFlags(dgm: Boolean, rsi: Boolean, approval: Boolean, sandbox: Boolean) {
        viewModelScope.launch { store.updateFlags(dgm, rsi, approval, sandbox); state = store.state() }
    }
    fun propose(title: String, rationale: String, change: String) {
        viewModelScope.launch { store.propose(title, rationale, change); state = store.state() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolutionScreen(onBack: () -> Unit, viewModel: EvolutionViewModel = hiltViewModel()) {\n    BrandBackground {
    val state = viewModel.state
    var title by remember { mutableStateOf("") }
    var rationale by remember { mutableStateOf("") }
    var change by remember { mutableStateOf("") }
    Scaffold(\n            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("DGM · RSI EVOLUTION LAB") },
                navigationIcon = { Button(onClick = onBack) { Text("‹") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Propose → Evaluate → Keep / Reject", style = MaterialTheme.typography.headlineSmall)
            Text(
                "DGM/RSI działają tutaj jako kontrolowany harness: system może tworzyć propozycje usprawnień i je oceniać, ale nie zmienia samodzielnie kodu aplikacji ani nie publikuje zmian.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ToggleRow("DGM proposal search", state.dgmEnabled) { value ->
                viewModel.updateFlags(value, state.rsiEnabled, state.humanApprovalRequired, state.sandboxOnly)
            }
            ToggleRow("RSI recursive evaluation", state.rsiEnabled) { value ->
                viewModel.updateFlags(state.dgmEnabled, value, state.humanApprovalRequired, state.sandboxOnly)
            }
            ToggleRow("Wymagaj akceptacji człowieka", state.humanApprovalRequired) { value ->
                viewModel.updateFlags(state.dgmEnabled, state.rsiEnabled, value, state.sandboxOnly)
            }
            ToggleRow("Tylko sandbox / brak auto-deploy", state.sandboxOnly) { value ->
                viewModel.updateFlags(state.dgmEnabled, state.rsiEnabled, state.humanApprovalRequired, value)
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Propozycja") })
                    OutlinedTextField(rationale, { rationale = it }, Modifier.fillMaxWidth(), minLines = 2, label = { Text("Uzasadnienie") })
                    OutlinedTextField(change, { change = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Planowana zmiana") })
                    Button(
                        onClick = {
                            viewModel.propose(title, rationale, change)
                            title = ""; rationale = ""; change = ""
                        },
                        enabled = title.isNotBlank() && change.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("DODAJ DO ARCHIWUM EWOLUCJI") }
                }
            }
            state.proposals.reversed().forEach { proposal ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(proposal.title, style = MaterialTheme.typography.titleMedium)
                        Text(proposal.rationale)
                        Text(proposal.change, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Status: " + proposal.status + " · score: " + (proposal.score?.toString() ?: "—"))
                    }
                }
            }
        }
    }
\n    }\n}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
