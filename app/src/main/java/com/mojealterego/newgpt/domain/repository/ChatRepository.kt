package com.mojealterego.newgpt.domain.repository

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, prompt: String, config: ProviderConfig)
    suspend fun clearHistory(conversationId: String)
}
