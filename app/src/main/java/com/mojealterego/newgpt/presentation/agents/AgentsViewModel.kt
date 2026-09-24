package com.mojealterego.newgpt.presentation.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.domain.agent.AgentRepository
import com.mojealterego.newgpt.domain.agent.AgentRuntime
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentsUiState(
    val agents: List<AgentDefinition> = emptyList(),
    val selectedAgentId: String = "coordinator",
    val messages: List<Message> = emptyList(),
    val inputEnabled: Boolean = true
) {
    val selectedAgent: AgentDefinition?
        get() = agents.firstOrNull { it.id == selectedAgentId }
}

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val agentRuntime: AgentRuntime,
    private val chatRepository: ChatRepository,
    private val settings: SecureSettings
) : ViewModel() {
    private val selected = MutableStateFlow("coordinator")
    private val sending = MutableStateFlow(false)

    private val messages = selected.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else chatRepository.observeMessages("agent:$id")
    }

    val state: StateFlow<AgentsUiState> = combine(
        agentRepository.agents, selected, messages, sending
    ) { list, id, items, busy ->
        val actualId = if (list.any { it.id == id }) id else list.firstOrNull()?.id.orEmpty()
        AgentsUiState(list, actualId, items, !busy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgentsUiState())

    fun selectAgent(id: String) { selected.value = id }

    fun send(text: String) {
        if (text.isBlank() || sending.value) return
        val agentId = state.value.selectedAgentId
        if (agentId.isBlank()) return
        viewModelScope.launch {
            sending.value = true
            try {
                agentRuntime.run(agentId, "agent:$agentId", text.trim(), settings.config.value)
            } finally { sending.value = false }
        }
    }

    fun runHandoffPipeline(text: String) {
        if (text.isBlank() || sending.value) return
        val root = state.value.selectedAgent ?: return
        val pipeline = (listOf(root.id) + root.handoffs).distinct()
            .filter { id -> state.value.agents.any { it.id == id && it.enabled } }
        if (pipeline.isEmpty()) return
        viewModelScope.launch {
            sending.value = true
            try {
                agentRuntime.runPipeline(pipeline, "agent:${root.id}", text.trim(), settings.config.value)
            } finally { sending.value = false }
        }
    }

    fun clear() {
        val id = state.value.selectedAgentId
        if (id.isBlank() || sending.value) return
        viewModelScope.launch { chatRepository.clearHistory("agent:$id") }
    }
}
