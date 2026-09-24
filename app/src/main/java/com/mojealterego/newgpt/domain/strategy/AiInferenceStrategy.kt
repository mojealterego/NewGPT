package com.mojealterego.newgpt.domain.strategy

import com.mojealterego.newgpt.domain.model.Message
import com.mojealterego.newgpt.domain.model.ProviderConfig
import kotlinx.coroutines.flow.Flow

interface AiInferenceStrategy {
    fun generateStream(messages: List<Message>, config: ProviderConfig): Flow<String>
}
