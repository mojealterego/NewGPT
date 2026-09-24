package com.mojealterego.newgpt.presentation.settings

import androidx.lifecycle.ViewModel
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.domain.model.ProviderConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val secureSettings: SecureSettings) : ViewModel() {
    val config: StateFlow<ProviderConfig> = secureSettings.config
    fun update(config: ProviderConfig) = secureSettings.update(config)
}
