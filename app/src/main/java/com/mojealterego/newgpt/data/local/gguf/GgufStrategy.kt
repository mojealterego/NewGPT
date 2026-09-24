package com.mojealterego.newgpt.data.local.gguf

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GgufStrategy @Inject constructor(private val engine: GgufNativeEngine) : AiInferenceStrategy {
    override fun generateStream(messages: List<Message>, config: ProviderConfig): Flow<String> {
        require(config.localModelPath.isNotBlank()) { "Nie wybrano modelu GGUF." }
        val prompt = messages.joinToString("\n") {
            if (it.isUser) "User: " + it.content else "Assistant: " + it.content
        } + "\nAssistant:"
        return flow {
            engine.loadModel(config.localModelPath)
            engine.generate(prompt).collect { emit(it) }
        }
    }
}
