package com.mojealterego.newgpt.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.model.ProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSettings @Inject constructor(@ApplicationContext context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "newgpt_secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val state = MutableStateFlow(load())
    private val canvaState = MutableStateFlow(prefs.getString("canva_access_token", "") ?: "")
    private val elevenLabsState = MutableStateFlow(prefs.getString("elevenlabs_api_key", "") ?: "")
    val config: StateFlow<ProviderConfig> = state.asStateFlow()
    val canvaAccessToken: StateFlow<String> = canvaState.asStateFlow()
    val elevenLabsKey: StateFlow<String> = elevenLabsState.asStateFlow()

    private fun load() = ProviderConfig(
        activeProvider = prefs.getString("provider", ProviderType.OPENAI.name)
            ?.let { runCatching { ProviderType.valueOf(it) }.getOrDefault(ProviderType.OPENAI) }
            ?: ProviderType.OPENAI,
        openAiKey = prefs.getString("openai_key", "") ?: "",
        openAiModel = prefs.getString("openai_model", "gpt-4.1-mini") ?: "gpt-4.1-mini",
        anthropicKey = prefs.getString("anthropic_key", "") ?: "",
        anthropicModel = prefs.getString("anthropic_model", "claude-sonnet-4-20250514") ?: "claude-sonnet-4-20250514",
        geminiKey = prefs.getString("gemini_key", "") ?: "",
        geminiModel = prefs.getString("gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash",
        compatibleBaseUrl = prefs.getString("compatible_url", "") ?: "",
        compatibleKey = prefs.getString("compatible_key", "") ?: "",
        compatibleModel = prefs.getString("compatible_model", "") ?: "",
        compatiblePresetId = prefs.getString("compatible_preset", "") ?: "",
        localModelPath = prefs.getString("local_model", "") ?: ""
    )

    fun updateCanvaAccessToken(value: String) {
        prefs.edit().putString("canva_access_token", value).apply()
        canvaState.value = value
    }

    fun updateElevenLabsKey(value: String) {
        prefs.edit().putString("elevenlabs_api_key", value).apply()
        elevenLabsState.value = value
    }

    fun update(value: ProviderConfig) {
        prefs.edit()
            .putString("provider", value.activeProvider.name)
            .putString("openai_key", value.openAiKey)
            .putString("openai_model", value.openAiModel)
            .putString("anthropic_key", value.anthropicKey)
            .putString("anthropic_model", value.anthropicModel)
            .putString("gemini_key", value.geminiKey)
            .putString("gemini_model", value.geminiModel)
            .putString("compatible_url", value.compatibleBaseUrl)
            .putString("compatible_key", value.compatibleKey)
            .putString("compatible_model", value.compatibleModel)
            .putString("compatible_preset", value.compatiblePresetId)
            .putString("local_model", value.localModelPath)
            .apply()
        state.value = value
    }
}
