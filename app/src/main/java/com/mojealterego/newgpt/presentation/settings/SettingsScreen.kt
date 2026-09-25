package com.mojealterego.newgpt.presentation.settings

import com.mojealterego.newgpt.presentation.theme.BrandGlobalHeader
import com.mojealterego.newgpt.presentation.theme.BrandPageHeader
import com.mojealterego.newgpt.presentation.theme.LuxuryCard
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.ProviderCatalog
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.model.ProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    BrandBackground {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val canvaToken by viewModel.canvaAccessToken.collectAsStateWithLifecycle()
    var draft by remember(config) { mutableStateOf(config) }
    var prefDraft by remember(prefs) { mutableStateOf(prefs) }
    var canvaDraft by remember(canvaToken) { mutableStateOf(canvaToken) }
    var providerExpanded by remember { mutableStateOf(false) }
    var presetExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var hfRepo by remember { mutableStateOf("") }
    var hfFile by remember { mutableStateOf("") }
    var hfRevision by remember { mutableStateOf("main") }
    var hfToken by remember { mutableStateOf("") }
    var ragStatus by remember { mutableStateOf("") }
    var hfStatus by remember { mutableStateOf("") }

    val ggufPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) draft = draft.copy(localModelPath = uri.toString(), activeProvider = ProviderType.LOCAL_GGUF)
    }
    val ragPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importRag(uri) { ragStatus = it }
    }

    Scaffold(
            containerColor = Color.Transparent,
        topBar = { BrandGlobalHeader(onMenu = onBack) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BrandPageHeader("USTAWIENIA", "AI CONTROL CENTER · DOSTAWCY · RAG · MEMORY · GGUF", onBack)
            Text("AI CONTROL CENTER", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text("Obsydian · 24K Gold · Serif", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)

            Section("JĘZYK / LANGUAGE") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { languageExpanded = true }) { Text(languageName(prefDraft.language)) }
                    DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                        listOf("pl","en","de","fr","es","it","uk").forEach { code ->
                            DropdownMenuItem(
                                text = { Text(languageName(code)) },
                                onClick = { prefDraft = prefDraft.copy(language = code); languageExpanded = false }
                            )
                        }
                    }
                }
            }

            Section("DOSTAWCY AI") {
                Button(onClick = { providerExpanded = true }, modifier = Modifier.fillMaxWidth()) { Text(draft.activeProvider.name) }
                DropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                    ProviderType.entries.forEach { provider ->
                        DropdownMenuItem(text = { Text(provider.name) }, onClick = {
                            draft = draft.copy(activeProvider = provider)
                            providerExpanded = false
                        })
                    }
                }
                if (draft.activeProvider == ProviderType.OPENAI_COMPATIBLE) {
                    Button(onClick = { presetExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(ProviderCatalog.find(draft.compatiblePresetId)?.name ?: "Wybierz preset providera")
                    }
                    DropdownMenu(expanded = presetExpanded, onDismissRequest = { presetExpanded = false }) {
                        ProviderCatalog.presets.forEach { preset ->
                            DropdownMenuItem(text = { Text(preset.name + " · " + preset.note) }, onClick = {
                                draft = draft.copy(
                                    activeProvider = ProviderType.OPENAI_COMPATIBLE,
                                    compatiblePresetId = preset.id,
                                    compatibleBaseUrl = preset.baseUrl,
                                    compatibleModel = preset.defaultModel
                                )
                                presetExpanded = false
                            })
                        }
                    }
                }
                ProviderFields(draft) { draft = it }
            }

            Section("RAG · LOKALNA BAZA WIEDZY") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Retrieval-Augmented Generation")
                        Text("Dokumenty są przechowywane lokalnie i dołączane do promptu jako kontekst.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefDraft.ragEnabled, onCheckedChange = { prefDraft = prefDraft.copy(ragEnabled = it) })
                }
                OutlinedTextField(value = prefDraft.ragTopK.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(ragTopK = it.toIntOrNull()?.coerceIn(1,12) ?: prefDraft.ragTopK)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Liczba wyników RAG") }, singleLine = true)
                Button(onClick = { ragPicker.launch(arrayOf("text/*", "application/json")) }, modifier = Modifier.fillMaxWidth()) { Text("DODAJ DOKUMENT DO RAG") }
                Button(onClick = { viewModel.clearRag { ragStatus = it } }, modifier = Modifier.fillMaxWidth()) { Text("WYCZYŚĆ BAZĘ RAG") }
                if (ragStatus.isNotBlank()) Text(ragStatus, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }

            Section("MEMORY · WORKING / PERMANENT / GRAPH") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Pamięć robocza")
                        Text("Zapamiętuje bieżące doświadczenia i buduje graf skojarzeń.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefDraft.workingMemoryEnabled, onCheckedChange = { prefDraft = prefDraft.copy(workingMemoryEnabled = it) })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Pamięć stała")
                        Text("Przechowuje wybrane doświadczenia między sesjami.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = prefDraft.permanentMemoryEnabled, onCheckedChange = { prefDraft = prefDraft.copy(permanentMemoryEnabled = it) })
                }
                OutlinedTextField(
                    value = prefDraft.memoryTopK.toString(),
                    onValueChange = { prefDraft = prefDraft.copy(memoryTopK = it.toIntOrNull()?.coerceIn(1, 12) ?: prefDraft.memoryTopK) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Liczba wspomnień do retrieval") },
                    singleLine = true
                )
                Text("Graf pamięci jest dostępny z Menu → Holographic Memory.", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }

            Section("INTERNET / WEB ACCESS") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pobieraj treść adresów HTTP(S) podanych w wiadomości.")
                    Switch(checked = prefDraft.webAccess, onCheckedChange = { prefDraft = prefDraft.copy(webAccess = it) })
                }
                Text("Bez klucza wyszukiwarki aplikacja wykonuje bezpośredni fetch URL; nie udaje pełnego indeksu wyszukiwania.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Section("GGUF · LOCAL INFERENCE") {
                Text("Model: " + (draft.localModelPath.ifBlank { "nie wybrano" }))
                Button(onClick = { ggufPicker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("WYBIERZ GGUF Z TELEFONU") }
                OutlinedTextField(value = prefDraft.contextSize.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(contextSize = it.toIntOrNull()?.coerceIn(1024,32768) ?: prefDraft.contextSize)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Context size") }, singleLine = true)
                OutlinedTextField(value = prefDraft.maxTokens.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(maxTokens = it.toIntOrNull()?.coerceIn(64,8192) ?: prefDraft.maxTokens)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Max tokens") }, singleLine = true)
                OutlinedTextField(value = prefDraft.temperature.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(temperature = it.toFloatOrNull()?.coerceIn(0f,2f) ?: prefDraft.temperature)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Temperature") }, singleLine = true)
                OutlinedTextField(value = prefDraft.topP.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(topP = it.toFloatOrNull()?.coerceIn(0.05f,1f) ?: prefDraft.topP)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Top-P") }, singleLine = true)
                OutlinedTextField(value = prefDraft.threads.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(threads = it.toIntOrNull()?.coerceIn(1,32) ?: prefDraft.threads)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("CPU threads") }, singleLine = true)
                OutlinedTextField(value = prefDraft.gpuLayers.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(gpuLayers = it.toIntOrNull()?.coerceIn(0,128) ?: prefDraft.gpuLayers)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("GPU layers") }, singleLine = true)
                OutlinedTextField(value = prefDraft.repeatPenalty.toString(), onValueChange = {
                    prefDraft = prefDraft.copy(repeatPenalty = it.toFloatOrNull()?.coerceIn(0.8f,2f) ?: prefDraft.repeatPenalty)
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Repeat penalty") }, singleLine = true)
            }

            Section("HUGGING FACE · GGUF DOWNLOADER") {
                Text("Resolver URL: huggingface.co/{repo}/resolve/{revision}/{file}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(hfRepo, { hfRepo = it }, Modifier.fillMaxWidth(), label = { Text("Repo ID, np. Qwen/...") }, singleLine = true)
                OutlinedTextField(hfFile, { hfFile = it }, Modifier.fillMaxWidth(), label = { Text("Nazwa pliku .gguf") }, singleLine = true)
                OutlinedTextField(hfRevision, { hfRevision = it }, Modifier.fillMaxWidth(), label = { Text("Revision") }, singleLine = true)
                OutlinedTextField(hfToken, { hfToken = it }, Modifier.fillMaxWidth(), label = { Text("HF token (opcjonalny)") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                Button(onClick = { viewModel.downloadHf(hfRepo, hfFile, hfRevision, hfToken) { hfStatus = it } }, modifier = Modifier.fillMaxWidth(), enabled = hfRepo.isNotBlank() && hfFile.isNotBlank()) { Text("POBIERZ GGUF") }
                if (hfStatus.isNotBlank()) Text(hfStatus, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }

            Section("CREATIVE API") {
                OutlinedTextField(value = prefDraft.elevenLabsKey, onValueChange = { prefDraft = prefDraft.copy(elevenLabsKey = it) }, modifier = Modifier.fillMaxWidth(), label = { Text("ElevenLabs API key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(value = prefDraft.xaiKey, onValueChange = { prefDraft = prefDraft.copy(xaiKey = it) }, modifier = Modifier.fillMaxWidth(), label = { Text("xAI API key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(value = canvaDraft, onValueChange = { canvaDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Canva Connect access token") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                Text("Token jest przechowywany w EncryptedSharedPreferences. Produkcyjny OAuth Canva wymaga Authorization Code + PKCE.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(onClick = {
                viewModel.update(draft)
                viewModel.updatePreferences(prefDraft)
                viewModel.updateCanvaAccessToken(canvaDraft)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text("ZAPISZ WSZYSTKO") }
        }
    }

    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    LuxuryCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            HorizontalDivider()
            content()
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
            KeyField("API key", config.compatibleKey) { onChange(config.copy(compatibleKey = it)) }
            TextField("Model", config.compatibleModel) { onChange(config.copy(compatibleModel = it)) }
        }
        ProviderType.LOCAL_GGUF -> Unit
    }
}

@Composable private fun KeyField(label: String, value: String, onValueChange: (String) -> Unit) =
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, visualTransformation = PasswordVisualTransformation())

@Composable private fun TextField(label: String, value: String, onValueChange: (String) -> Unit) =
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true)

private fun languageName(code: String): String = when (code) {
    "pl" -> "Polski"
    "en" -> "English"
    "de" -> "Deutsch"
    "fr" -> "Français"
    "es" -> "Español"
    "it" -> "Italiano"
    "uk" -> "Українська"
    else -> code
}
