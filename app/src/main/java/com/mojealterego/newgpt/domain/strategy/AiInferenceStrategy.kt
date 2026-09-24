package com.mojealterego.newgpt.domain.strategy

import com.mojealterego.newgpt.domain.model.ProviderConfig
import kotlinx.coroutines.flow.Flow

interface AiInferenceStrategy {
    fun generateStream(prompt: String, config: ProviderConfig): Flow<String>
}
