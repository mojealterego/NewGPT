package com.mojealterego.newgpt.data.repository

import com.mojealterego.newgpt.data.local.MessageDao
import com.mojealterego.newgpt.data.local.MessageEntity
import com.mojealterego.newgpt.data.local.gguf.GgufStrategy
import com.mojealterego.newgpt.data.remote.AnthropicStrategy
import com.mojealterego.newgpt.data.remote.GeminiStrategy
import com.mojealterego.newgpt.data.remote.OpenAiCompatibleStrategy
import com.mojealterego.newgpt.data.remote.OpenAiStrategy
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.model.ProviderType
import com.mojealterego.newgpt.domain.repository.ChatRepository
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val dao: MessageDao,
    private val openAi: OpenAiStrategy,
    private val anthropic: AnthropicStrategy,
    private val gemini: GeminiStrategy,
    private val compatible: OpenAiCompatibleStrategy,
    private val gguf: GgufStrategy
) : ChatRepository {

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        dao.observe(conversationId).map { list ->
            list.map { Message(it.id, it.conversationId, it.content, it.isUser, it.timestamp, it.isPending) }
        }

    override suspend fun sendMessage(conversationId: String, prompt: String, config: ProviderConfig) =
        withContext(Dispatchers.IO) {
            val previousMessages = dao.observe(conversationId).first().map { Message(it.id, it.conversationId, it.content, it.isUser, it.timestamp, it.isPending) }
            val userId = UUID.randomUUID().toString()
            dao.insert(MessageEntity(userId, conversationId, prompt, true, System.currentTimeMillis(), false))

            val aiId = UUID.randomUUID().toString()
            dao.insert(MessageEntity(aiId, conversationId, "", false, System.currentTimeMillis() + 1, true))

            val strategy: AiInferenceStrategy = when (config.activeProvider) {
                ProviderType.OPENAI -> openAi
                ProviderType.ANTHROPIC -> anthropic
                ProviderType.GEMINI -> gemini
                ProviderType.OPENAI_COMPATIBLE -> compatible
                ProviderType.LOCAL_GGUF -> gguf
            }

            var response = ""
            try {
                strategy.generateStream(previousMessages + Message(userId, conversationId, prompt, true, System.currentTimeMillis()), config).collect { token ->
                    response += token
                    dao.update(aiId, response, true)
                }
                dao.update(aiId, response, false)
            } catch (error: Throwable) {
                dao.update(aiId, "Błąd inferencji: " + (error.message ?: "nieznany błąd"), false)
            }
        }

    override suspend fun clearHistory(conversationId: String) = dao.clear(conversationId)
}
