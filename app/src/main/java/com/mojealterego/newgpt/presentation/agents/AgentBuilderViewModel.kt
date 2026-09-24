package com.mojealterego.newgpt.presentation.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.domain.agent.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AgentBuilderUiState(
    val agents: List<AgentDefinition> = emptyList(),
    val selectedId: String? = null
) {
    val selected: AgentDefinition?
        get() = agents.firstOrNull { it.id == selectedId }
}

@HiltViewModel
class AgentBuilderViewModel @Inject constructor(
    private val repository: AgentRepository
) : ViewModel() {
    private val manualSelected = MutableStateFlow<String?>(null)

    val state: StateFlow<AgentBuilderUiState> = combine(
        repository.agents, manualSelected
    ) { list, selected ->
        AgentBuilderUiState(list, selected ?: list.firstOrNull()?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgentBuilderUiState())

    fun select(id: String) { manualSelected.value = id }

    fun newAgent(): AgentDefinition = AgentDefinition(
        id = "agent-" + UUID.randomUUID().toString().take(8),
        name = "Nowy agent",
        description = "Opis agenta",
        systemPrompt = "Jesteś wyspecjalizowanym agentem NewGPT.",
        skills = listOf("general")
    )

    fun save(agent: AgentDefinition) {
        viewModelScope.launch {
            repository.upsert(agent)
            manualSelected.value = agent.id
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            manualSelected.value = null
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.resetToDefaults()
            manualSelected.value = null
        }
    }
}
