package com.mojealterego.newgpt.data.remote

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject

class OpenAiStrategy @Inject constructor(private val client: HttpClient) : AiInferenceStrategy {
    private val json = Json { ignoreUnknownKeys = true }

    override fun generateStream(
        messages: List<Message>,
        config: ProviderConfig,
        systemPrompt: String?
    ): Flow<String> = flow {
        require(config.openAiKey.isNotBlank()) { "Brak klucza OpenAI." }
        val history = buildList {
            if (!systemPrompt.isNullOrBlank()) add(ChatMessageDto("system", systemPrompt))
            addAll(messages.map { ChatMessageDto(if (it.isUser) "user" else "assistant", it.content) })
        }
        val request = OpenAiRequest(config.openAiModel, history)
        client.preparePost("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.ContentType, "application/json")
            header("Authorization", "Bearer " + config.openAiKey)
            setBody(request)
        }.execute { response ->
            check(response.status.value in 200..299) { "HTTP ${response.status.value}" }
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: continue
                if (!line.startsWith("data: ") || line == "data: [DONE]") continue
                runCatching {
                    json.decodeFromString<OpenAiChunk>(line.removePrefix("data: "))
                        .choices.firstOrNull()?.delta?.content?.let { emit(it) }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
