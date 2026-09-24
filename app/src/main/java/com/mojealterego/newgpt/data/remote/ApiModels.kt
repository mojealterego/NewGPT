package com.mojealterego.newgpt.data.remote

import kotlinx.serialization.Serializable

@Serializable data class ChatMessageDto(val role: String, val content: String)

@Serializable data class OpenAiRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = true
)

@Serializable data class OpenAiDelta(val content: String? = null)
@Serializable data class OpenAiChoice(val delta: OpenAiDelta)
@Serializable data class OpenAiChunk(val choices: List<OpenAiChoice> = emptyList())

@Serializable data class AnthropicRequest(
    val model: String,
    val max_tokens: Int = 4096,
    val system: String? = null,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = true
)

@Serializable data class AnthropicDelta(val text: String? = null)
@Serializable data class AnthropicEvent(
    val type: String,
    val delta: AnthropicDelta? = null
)

@Serializable data class GeminiPart(val text: String)
@Serializable data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)
@Serializable data class GeminiSystemInstruction(val parts: List<GeminiPart>)
@Serializable data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null
)
@Serializable data class GeminiCandidate(val content: GeminiContent)
@Serializable data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())
