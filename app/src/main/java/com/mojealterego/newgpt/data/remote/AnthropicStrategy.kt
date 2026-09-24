package com.mojealterego.newgpt.data.remote

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import io.ktor.client.HttpClient
import io.ktor.client.request.contentType
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AnthropicStrategy @Inject constructor(private val client: HttpClient) : AiInferenceStrategy {
    private val json = Json { ignoreUnknownKeys = true }

    override fun generateStream(
        messages: List<Message>,
        config: ProviderConfig,
        systemPrompt: String?
    ): Flow<String> = flow {
        require(config.anthropicKey.isNotBlank()) { "Brak klucza Anthropic." }
        val request = AnthropicRequest(
            model = config.anthropicModel,
            system = systemPrompt?.takeIf { it.isNotBlank() },
            messages = messages.map { ChatMessageDto(if (it.isUser) "user" else "assistant", it.content) }
        )
        client.preparePost("https://api.anthropic.com/v1/messages") {
            contentType(ContentType.Application.Json)
            header("x-api-key", config.anthropicKey)
            header("anthropic-version", "2023-06-01")
            setBody(request)
        }.execute { response ->
            check(response.status.value in 200..299) { "HTTP ${response.status.value}" }
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: continue
                if (!line.startsWith("data: ")) continue
                runCatching {
                    val event = json.decodeFromString<AnthropicEvent>(line.removePrefix("data: "))
                    if (event.type == "content_block_delta") event.delta?.text?.let { emit(it) }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
