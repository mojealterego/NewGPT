package com.mojealterego.newgpt.presentation.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.model.ProviderType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var draft by remember(config) { mutableStateOf(config) }
    var expanded by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val destination = File(context.filesDir, "models").apply { mkdirs() }
            val file = File(destination, "local-model.gguf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            draft = draft.copy(localModelPath = file.absolutePath, activeProvider = ProviderType.LOCAL_GGUF)
            viewModel.update(draft)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ustawienia") }, navigationIcon = {
        androidx.compose.material3.IconButton(onClick = onBack) {
            androidx.compose.material3.Text("‹")
        }
    }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = draft.activeProvider.name,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("Dostawca") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ProviderType.entries.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.name) },
                            onClick = { draft = draft.copy(activeProvider = provider); expanded = false }
                        )
                    }
                }
            }

            ProviderFields(draft, onChange = { draft = it })

            if (draft.activeProvider == ProviderType.LOCAL_GGUF) {
                Text("Model lokalny: " + (draft.localModelPath.ifBlank { "nie wybrano" }))
                Button(onClick = { picker.launch(arrayOf("*/*")) }) { Text("Wybierz plik GGUF") }
            }

            Button(onClick = { viewModel.update(draft); onBack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Zapisz konfigurację")
            }
        }
    }
}

@Composable
private fun ProviderFields(config: ProviderConfig, onChange: (ProviderConfig) -> Unit) {
    when (config.activeProvider) {
        ProviderType.OPENAI -> {
            KeyField("OpenAI API key", config.openAiKey) { onChange(config.copy(openAiKey = it)) }
            TextField("Model", config.openAiModel) { onChange(config.copy(openAiModel = it)) }
        }
        ProviderType.ANTHROPIC -> {
            KeyField("Anthropic API key", config.anthropicKey) { onChange(config.copy(anthropicKey = it)) }
            TextField("Model", config.anthropicModel) { onChange(config.copy(anthropicModel = it)) }
        }
        ProviderType.GEMINI -> {
            KeyField("Gemini API key", config.geminiKey) { onChange(config.copy(geminiKey = it)) }
            TextField("Model", config.geminiModel) { onChange(config.copy(geminiModel = it)) }
        }
        ProviderType.OPENAI_COMPATIBLE -> {
            TextField("Base URL", config.compatibleBaseUrl) { onChange(config.copy(compatibleBaseUrl = it)) }
            KeyField("API key (opcjonalny)", config.compatibleKey) { onChange(config.copy(compatibleKey = it)) }
            TextField("Model", config.compatibleModel) { onChange(config.copy(compatibleModel = it)) }
        }
        ProviderType.LOCAL_GGUF -> Unit
    }
}

@Composable private fun KeyField(label: String, value: String, onValueChange: (String) -> Unit) =
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, visualTransformation = PasswordVisualTransformation())

@Composable private fun TextField(label: String, value: String, onValueChange: (String) -> Unit) =
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true)
