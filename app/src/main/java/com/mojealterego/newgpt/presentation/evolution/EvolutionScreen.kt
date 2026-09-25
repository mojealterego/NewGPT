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
import androidx.compose.ui.Modifier
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
fun EvolutionScreen(onBack: () -> Unit, viewModel: EvolutionViewModel = hiltViewModel()) {
    val state = viewModel.state
    var title by remember { mutableStateOf("") }
    var rationale by remember { mutableStateOf("") }
    var change by remember { mutableStateOf("") }
    PremiumScaffold(
        selected = "Narzędzia",
        title = "DGM · RSI EVOLUTION LAB",
        subtitle = "PROPOSE → EVALUATE → KEEP / REJECT",
        onBack = onBack,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = {},
        onSettingsNav = {}
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(14.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldCard(title = "DGM / RSI Evolution Lab", icon = Icons.Default.Psychology) {
                Text("Kontrolowany harness: system tworzy propozycje usprawnień i je ocenia, ale nie zmienia samodzielnie kodu aplikacji ani nie publikuje zmian.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ToggleRow("DGM proposal search", state.dgmEnabled) { viewModel.updateFlags(it, state.rsiEnabled, state.humanApprovalRequired, state.sandboxOnly) }
            ToggleRow("RSI recursive evaluation", state.rsiEnabled) { viewModel.updateFlags(state.dgmEnabled, it, state.humanApprovalRequired, state.sandboxOnly) }
            ToggleRow("Wymagaj akceptacji człowieka", state.humanApprovalRequired) { viewModel.updateFlags(state.dgmEnabled, state.rsiEnabled, it, state.sandboxOnly) }
            ToggleRow("Tylko sandbox / brak auto-deploy", state.sandboxOnly) { viewModel.updateFlags(state.dgmEnabled, state.rsiEnabled, state.humanApprovalRequired, it) }

            GoldCard(title = "NOWA PROPOZYCJA", icon = Icons.Default.Science) {
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Propozycja") })
                OutlinedTextField(rationale, { rationale = it }, Modifier.fillMaxWidth(), minLines = 2, label = { Text("Uzasadnienie") })
                OutlinedTextField(change, { change = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Planowana zmiana") })
                GoldButton(
                    "GENERUJ / ZAPISZ PROPOZYCJĘ",
                    { viewModel.propose(title, rationale, change); title = ""; rationale = ""; change = "" },
                    enabled = title.isNotBlank() && change.isNotBlank(),
                    icon = Icons.Default.AutoAwesome
                )
            }
            state.proposals.reversed().forEach { proposal ->
                GoldCard(title = proposal.title, icon = Icons.Default.Description) {
                    Text(proposal.rationale)
                    Text(proposal.change, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Status: ${proposal.status} · score: ${proposal.score?.toString() ?: "—"}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        GoldSwitch(checked, onChecked)
    }
}
