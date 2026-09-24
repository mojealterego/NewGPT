package com.mojealterego.newgpt.domain.usecase

import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.repository.ChatRepository
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(private val repository: ChatRepository) {
    operator fun invoke(conversationId: String) = repository.observeMessages(conversationId)
}

class SendMessageUseCase @Inject constructor(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, text: String, config: ProviderConfig) =
        repository.sendMessage(conversationId, text, config)
}

class ClearHistoryUseCase @Inject constructor(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String) = repository.clearHistory(conversationId)
}
