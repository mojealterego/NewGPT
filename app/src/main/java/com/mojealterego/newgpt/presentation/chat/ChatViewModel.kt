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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val sending = MutableStateFlow(false)
    private val sendMutex = Mutex()

    private val messages = observeMessages(CONVERSATION_ID)
        .catch { emit(emptyList()) }

    val state: StateFlow<ChatUiState> = combine(messages, sending) { items, isSending ->
        ChatUiState(
            messages = items.toImmutableList(),
            inputEnabled = !isSending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun send(text: String) {
        if (text.isBlank() || sending.value) return
        viewModelScope.launch {
            sendMutex.withLock {
                sending.value = true
                try {
                    sendMessage(CONVERSATION_ID, text.trim(), settings.config.value)
                } finally {
                    sending.value = false
                }
            }
        }
    }

    fun clear() {
        if (sending.value) return
        viewModelScope.launch { clearHistory(CONVERSATION_ID) }
    }
}
