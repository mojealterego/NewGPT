package com.mojealterego.newgpt.presentation.cognitive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.BitemporalMemoryStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CognitiveModuleUi(
    val name: String,
    val description: String,
    val status: String
)

data class CognitiveUiState(
    val memoryCount: Int = 0,
    val modules: List<CognitiveModuleUi> = emptyList()
)

@HiltViewModel
class CognitiveViewModel @Inject constructor(
    private val memory: BitemporalMemoryStore
) : ViewModel() {
    private val _state = MutableStateFlow(
        CognitiveUiState(
            modules = listOf(
                CognitiveModuleUi("CoALA Memory", "Working, episodic, semantic i procedural memory.", "CORE"),
                CognitiveModuleUi("GoT + Decision Cycle", "Graf decyzji zamiast wyłącznie liniowego handoffu.", "CORE"),
                CognitiveModuleUi("Hybrid RAG 2.0", "Dokumenty + pamięć + routing temporalny.", "CORE"),
                CognitiveModuleUi("MCP Capability Broker", "Jedna granica dla narzędzi, zgód i polityk.", "CORE"),
                CognitiveModuleUi("Adversarial Gate", "Kontrola ryzyka przed uprzywilejowanym wykonaniem.", "CORE"),
                CognitiveModuleUi("Reflexion / Self-Correction", "Weryfikacja i korekta wyników.", "CORE"),
                CognitiveModuleUi("Digital Genotype", "Wersjonowany opis agenta pod Evolution Lab.", "EVOLUTION"),
                CognitiveModuleUi("Mutation Engine", "Propose → sandbox → evaluate → accept/reject.", "EVOLUTION"),
                CognitiveModuleUi("JEPA / HDC / SNN", "Izolowane moduły badawcze dla przyszłych eksperymentów.", "RESEARCH")
            )
        )
    )
    val state: StateFlow<CognitiveUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(memoryCount = memory.all().size)
        }
    }
}
