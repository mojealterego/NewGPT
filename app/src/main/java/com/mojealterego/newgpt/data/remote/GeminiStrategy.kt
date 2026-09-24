package com.mojealterego.newgpt.data.remote

import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.client.request.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiStrategy @Inject constructor(private val client: HttpClient) : AiInferenceStrategy {
    override fun generateStream(prompt: String, config: ProviderConfig): Flow<String> = flow {
        require(config.geminiKey.isNotBlank()) { "Brak klucza Gemini." }
        val response: GeminiResponse = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/" + config.geminiModel + ":generateContent"
        ) {
            parameter("key", config.geminiKey)
            contentType(ContentType.Application.Json)
            setBody(GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)), "user"))))
        }.body()
        response.candidates.firstOrNull()?.content?.parts?.forEach { emit(it.text) }
    }
}
