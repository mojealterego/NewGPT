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
import io.ktor.utils.io.readUTF8Line
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiStrategy @Inject constructor(private val client: HttpClient) : AiInferenceStrategy {
    private val json = Json { ignoreUnknownKeys = true }

    override fun generateStream(messages: List<Message>, config: ProviderConfig): Flow<String> = flow {
        require(config.geminiKey.isNotBlank()) { "Brak klucza Gemini." }
        val request = GeminiRequest(
            messages.map {
                GeminiContent(
                    parts = listOf(GeminiPart(it.content)),
                    role = if (it.isUser) "user" else "model"
                )
            }
        )
        client.preparePost(
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                config.geminiModel + ":streamGenerateContent?alt=sse"
        ) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", config.geminiKey)
            setBody(request)
        }.execute { response ->
            check(response.status.value in 200..299) { "Gemini HTTP ${response.status.value}" }
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: continue
                if (!line.startsWith("data: ")) continue
                runCatching {
                    val chunk = json.decodeFromString<GeminiResponse>(line.removePrefix("data: "))
                    chunk.candidates.firstOrNull()?.content?.parts?.forEach { emit(it.text) }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
