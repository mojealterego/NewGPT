package com.mojealterego.newgpt.presentation.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.domain.agent.AgentGraphValidator
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
    val selectedId: String? = null,
    val validationErrors: List<String> = emptyList()
) {
    val selected: AgentDefinition?
        get() = agents.firstOrNull { it.id == selectedId }
}

@HiltViewModel
class AgentBuilderViewModel @Inject constructor(
    private val repository: AgentRepository
) : ViewModel() {
    private val manualSelected = MutableStateFlow<String?>(null)
    private val errors = MutableStateFlow<List<String>>(emptyList())

    val state: StateFlow<AgentBuilderUiState> = combine(
        repository.agents, manualSelected, errors
    ) { list, selected, validationErrors ->
        AgentBuilderUiState(
            agents = list,
            selectedId = selected ?: list.firstOrNull()?.id,
            validationErrors = validationErrors
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgentBuilderUiState())

    fun select(id: String) {
        errors.value = emptyList()
        manualSelected.value = id
    }

    fun newAgent(): AgentDefinition = AgentDefinition(
        id = "agent-" + UUID.randomUUID().toString().take(8),
        name = "Nowy agent",
        description = "Opis agenta",
        systemPrompt = "Jesteś wyspecjalizowanym agentem NewGPT.",
        skills = listOf("general")
    )

    fun save(agent: AgentDefinition) {
        viewModelScope.launch {
            val current = state.value.agents.filterNot { it.id == agent.id } + agent
            val validation = AgentGraphValidator.validate(agent, current)
            val graph = AgentGraphValidator.validateGraph(current)
            if (!validation.valid || !graph.valid) {
                errors.value = (validation.errors + graph.errors).distinct()
                return@launch
            }
            repository.upsert(agent)
            errors.value = emptyList()
            manualSelected.value = agent.id
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            errors.value = emptyList()
            manualSelected.value = null
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.resetToDefaults()
            errors.value = emptyList()
            manualSelected.value = null
        }
    }
}
