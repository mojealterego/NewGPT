package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.model.ProviderType
import org.junit.Assert.assertEquals
import org.junit.Test

class ProviderConfigTest {
    @Test
    fun providerMatrixContainsCloudCompatibleAndLocalBackends() {
        assertEquals(
            listOf(
                ProviderType.OPENAI,
                ProviderType.ANTHROPIC,
                ProviderType.GEMINI,
                ProviderType.OPENAI_COMPATIBLE,
                ProviderType.LOCAL_GGUF
            ),
            ProviderType.entries.toList()
        )
    }
}
