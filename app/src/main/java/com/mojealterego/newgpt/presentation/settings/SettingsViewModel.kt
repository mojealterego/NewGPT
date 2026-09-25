package com.mojealterego.newgpt.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.AppPreferences
import com.mojealterego.newgpt.data.local.AppPreferencesStore
import com.mojealterego.newgpt.data.local.LocalRagStore
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.data.remote.HuggingFaceModelService
import com.mojealterego.newgpt.domain.model.ProviderConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureSettings: SecureSettings,
    private val appPreferences: AppPreferencesStore,
    private val rag: LocalRagStore,
    private val hf: HuggingFaceModelService
) : ViewModel() {
    val config: StateFlow<ProviderConfig> = secureSettings.config
    val preferences: StateFlow<AppPreferences> = appPreferences.preferences
    val canvaAccessToken: StateFlow<String> = secureSettings.canvaAccessToken

    fun update(config: ProviderConfig) = secureSettings.update(config)
    fun updateCanvaAccessToken(value: String) = secureSettings.updateCanvaAccessToken(value)
    fun updatePreferences(value: AppPreferences) = appPreferences.update(value)

    fun importRag(uri: Uri, onDone: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { rag.importUri(uri) }
                .onSuccess(onDone)
                .onFailure { onDone("Błąd RAG: " + (it.message ?: "nieznany")) }
        }
    }

    fun clearRag(onDone: (String) -> Unit) {
        viewModelScope.launch {
            rag.clear()
            onDone("Indeks RAG wyczyszczony.")
        }
    }

    fun downloadHf(
        repoId: String,
        fileName: String,
        revision: String,
        token: String,
        onDone: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                hf.download(repoId, fileName, revision, token.ifBlank { null }) { _, _ -> }
            }.onSuccess { file ->
                onDone("Pobrano GGUF: " + file.absolutePath)
            }.onFailure {
                onDone("Błąd pobierania GGUF: " + (it.message ?: "nieznany"))
            }
        }
    }
}
