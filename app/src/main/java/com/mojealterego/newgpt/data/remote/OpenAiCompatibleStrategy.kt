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

class OpenAiCompatibleStrategy @Inject constructor(private val client: HttpClient) : AiInferenceStrategy {
    private val json = Json { ignoreUnknownKeys = true }

    override fun generateStream(
        messages: List<Message>,
        config: ProviderConfig,
        systemPrompt: String?
    ): Flow<String> = flow {
        require(config.compatibleBaseUrl.isNotBlank()) { "Brak adresu OpenAI-compatible." }
        require(config.compatibleModel.isNotBlank()) { "Brak modelu OpenAI-compatible." }
        val base = config.compatibleBaseUrl.trimEnd('/')
        val history = buildList {
            if (!systemPrompt.isNullOrBlank()) add(ChatMessageDto("system", systemPrompt))
            addAll(messages.map { ChatMessageDto(if (it.isUser) "user" else "assistant", it.content) })
        }
        val request = OpenAiRequest(config.compatibleModel, history)
        client.preparePost(base + "/chat/completions") {
            contentType(ContentType.Application.Json)
            if (config.compatibleKey.isNotBlank()) header("Authorization", "Bearer " + config.compatibleKey)
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
