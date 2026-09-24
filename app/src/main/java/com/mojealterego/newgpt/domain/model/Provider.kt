package com.mojealterego.newgpt.domain.model

enum class ProviderType { OPENAI, ANTHROPIC, GEMINI, OPENAI_COMPATIBLE, LOCAL_GGUF }

data class ProviderConfig(
    val activeProvider: ProviderType = ProviderType.OPENAI,
    val openAiKey: String = "",
    val openAiModel: String = "gpt-4.1-mini",
    val anthropicKey: String = "",
    val anthropicModel: String = "claude-sonnet-4-20250514",
    val geminiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val compatibleBaseUrl: String = "",
    val compatibleKey: String = "",
    val compatibleModel: String = "",
    val compatiblePresetId: String = "",
    val localModelPath: String = ""
)

data class Message(
    val id: String,
    val conversationId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isPending: Boolean = false
)
