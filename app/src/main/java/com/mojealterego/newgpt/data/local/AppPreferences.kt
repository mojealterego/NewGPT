package com.mojealterego.newgpt.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class AppPreferences(
    val language: String = "pl",
    val webAccess: Boolean = true,
    val ragEnabled: Boolean = true,
    val ragTopK: Int = 5,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxTokens: Int = 512,
    val contextSize: Int = 4096,
    val threads: Int = 4,
    val gpuLayers: Int = 0,
    val repeatPenalty: Float = 1.05f,
    val elevenLabsKey: String = "",
    val xaiKey: String = ""
)

@Singleton
class AppPreferencesStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("newgpt_ui_preferences", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())
    val preferences: StateFlow<AppPreferences> = state.asStateFlow()

    private fun load() = AppPreferences(
        language = prefs.getString("language", "pl") ?: "pl",
        webAccess = prefs.getBoolean("web_access", true),
        ragEnabled = prefs.getBoolean("rag_enabled", true),
        ragTopK = prefs.getInt("rag_top_k", 5),
        temperature = prefs.getFloat("temperature", 0.7f),
        topP = prefs.getFloat("top_p", 0.9f),
        maxTokens = prefs.getInt("max_tokens", 512),
        contextSize = prefs.getInt("context_size", 4096),
        threads = prefs.getInt("threads", 4),
        gpuLayers = prefs.getInt("gpu_layers", 0),
        repeatPenalty = prefs.getFloat("repeat_penalty", 1.05f),
        elevenLabsKey = prefs.getString("eleven_key", "") ?: "",
        xaiKey = prefs.getString("xai_key", "") ?: ""
    )

    fun update(value: AppPreferences) {
        prefs.edit()
            .putString("language", value.language)
            .putBoolean("web_access", value.webAccess)
            .putBoolean("rag_enabled", value.ragEnabled)
            .putInt("rag_top_k", value.ragTopK.coerceIn(1, 12))
            .putFloat("temperature", value.temperature.coerceIn(0f, 2f))
            .putFloat("top_p", value.topP.coerceIn(0.05f, 1f))
            .putInt("max_tokens", value.maxTokens.coerceIn(64, 8192))
            .putInt("context_size", value.contextSize.coerceIn(1024, 32768))
            .putInt("threads", value.threads.coerceIn(1, 32))
            .putInt("gpu_layers", value.gpuLayers.coerceIn(0, 128))
            .putFloat("repeat_penalty", value.repeatPenalty.coerceIn(0.8f, 2f))
            .putString("eleven_key", value.elevenLabsKey)
            .putString("xai_key", value.xaiKey)
            .apply()
        state.value = value
    }

    companion object {
        fun loadLanguage(context: Context): String =
            context.getSharedPreferences("newgpt_ui_preferences", Context.MODE_PRIVATE)
                .getString("language", "pl") ?: "pl"
    }
}
