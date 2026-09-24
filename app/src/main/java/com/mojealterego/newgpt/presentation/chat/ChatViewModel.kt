package com.mojealterego.newgpt.presentation.chat

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.usecase.ClearHistoryUseCase
import com.mojealterego.newgpt.domain.usecase.ObserveMessagesUseCase
import com.mojealterego.newgpt.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ChatUiState(
    val messages: ImmutableList<Message> = persistentListOf(),
    val inputEnabled: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val settings: SecureSettings
) : ViewModel() {
    companion object { const val CONVERSATION_ID = "default" }

    val state: StateFlow<ChatUiState> = observeMessages(CONVERSATION_ID)
        .map { ChatUiState(messages = it.toImmutableList()) }
        .catch { emit(ChatUiState(error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun send(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            sendMessage(
                CONVERSATION_ID,
                text.trim(),
                settings.config.value
            )
        }
    }

    fun clear() = viewModelScope.launch { clearHistory(CONVERSATION_ID) }
}
