package com.mojealterego.newgpt.data.local.gguf

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GgufStrategy @Inject constructor(private val engine: GgufNativeEngine) : AiInferenceStrategy {
    override fun generateStream(
        messages: List<Message>,
        config: ProviderConfig,
        systemPrompt: String?
    ): Flow<String> {
        require(config.localModelPath.isNotBlank()) { "Nie wybrano modelu GGUF." }

        return flow {
            engine.loadModel(config.localModelPath)

            val chatMessages = buildList {
                if (!systemPrompt.isNullOrBlank()) {
                    add("system" to systemPrompt.trim())
                }
                messages.forEach {
                    add((if (it.isUser) "user" else "assistant") to it.content)
                }
            }

            val prompt = engine.formatChat(chatMessages) ?: buildFallbackPrompt(chatMessages)
            engine.generate(prompt).collect { emit(it) }
        }
    }

    private fun buildFallbackPrompt(messages: List<Pair<String, String>>): String =
        buildString {
            messages.forEach { (role, content) ->
                append(role.replaceFirstChar { it.uppercase() })
                    .append(": ")
                    .append(content)
                    .append("\n")
            }
            append("Assistant:")
        }
}
