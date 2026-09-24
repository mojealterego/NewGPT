package com.mojealterego.newgpt.data.local.gguf

import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.strategy.AiInferenceStrategy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GgufStrategy @Inject constructor(private val engine: GgufNativeEngine) : AiInferenceStrategy {
    override fun generateStream(prompt: String, config: ProviderConfig): Flow<String> {
        require(config.localModelPath.isNotBlank()) { "Nie wybrano modelu GGUF." }
        return kotlinx.coroutines.flow.flow {
            engine.loadModel(config.localModelPath)
            emitAll(engine.generate(prompt))
        }
    }

    private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(source: Flow<T>) {
        source.collect { emit(it) }
    }
}
