package com.mojealterego.newgpt.data.local.gguf

import com.mojealterego.newgpt.data.local.AppPreferencesStore
import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GgufStrategy @Inject constructor(
    private val engine: GgufNativeEngine,
    private val preferences: AppPreferencesStore
) : AiInferenceStrategy {
    override fun generateStream(
        messages: List<Message>,
        config: ProviderConfig,
        systemPrompt: String?
    ): Flow<String> = flow {
        require(config.localModelPath.isNotBlank()) { "Nie wybrano modelu GGUF." }
        val pref = preferences.preferences.value
        engine.loadModel(config.localModelPath, pref.gpuLayers)

        val chatMessages = buildList {
            if (!systemPrompt.isNullOrBlank()) add("system" to systemPrompt.trim())
            messages.forEach { add((if (it.isUser) "user" else "assistant") to it.content) }
        }

        val prompt = engine.formatChat(chatMessages) ?: buildFallbackPrompt(chatMessages)
        engine.generate(
            prompt = prompt,
            contextSize = pref.contextSize,
            maxTokens = pref.maxTokens,
            temperature = pref.temperature,
            topP = pref.topP,
            threads = pref.threads
        ).collect { emit(it) }
    }

    private fun buildFallbackPrompt(messages: List<Pair<String, String>>): String =
        buildString {
            messages.forEach { (role, content) ->
                append(role.replaceFirstChar { it.uppercase() }).append(": ").append(content).append("\n")
            }
            append("Assistant:")
        }
}
