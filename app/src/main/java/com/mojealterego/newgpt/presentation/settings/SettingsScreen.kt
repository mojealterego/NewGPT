package com.mojealterego.newgpt.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojealterego.newgpt.domain.model.ProviderCatalog
import com.mojealterego.newgpt.domain.model.ProviderConfig
import com.mojealterego.newgpt.domain.model.ProviderType
import com.mojealterego.newgpt.presentation.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val canvaToken by viewModel.canvaAccessToken.collectAsStateWithLifecycle()
    val elevenLabsKey by viewModel.elevenLabsKey.collectAsStateWithLifecycle()
    var draft by remember(config) { mutableStateOf(config) }
    var prefDraft by remember(prefs) { mutableStateOf(prefs) }
    var canvaDraft by remember(canvaToken) { mutableStateOf(canvaToken) }
    var elevenLabsDraft by remember(elevenLabsKey) { mutableStateOf(elevenLabsKey) }
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

    PremiumScaffold(
        selected = "Ustawienia",
        title = "USTAWIENIA",
        subtitle = "AI CONTROL CENTER · OBSYDIAN · 24K GOLD · SERIF",
        onBack = onBack,
        onPanel = {},
        onAgents = {},
        onMemory = {},
        onTools = {},
        onSettingsNav = {}
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(14.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldCard(title = "JĘZYK / LANGUAGE", icon = Icons.Default.Language) {
                Box {
                    OutlineGoldButton(languageName(prefDraft.language), { languageExpanded = true }, icon = Icons.Default.Language)
                    DropdownMenu(languageExpanded, { languageExpanded = false }) {
                        listOf("pl","en","de","fr","es","it","uk").forEach { code ->
                            DropdownMenuItem(
                                text = { Text(languageName(code)) },
                                onClick = { prefDraft = prefDraft.copy(language = code); languageExpanded = false }
                            )
                        }
                    }
                }
            }

            GoldCard(title = "DOSTAWCY AI", icon = Icons.Default.Memory) {
                Box {
                    OutlineGoldButton(draft.activeProvider.name, { providerExpanded = true }, icon = Icons.Default.Tune)
                    DropdownMenu(providerExpanded, { providerExpanded = false }) {
                        ProviderType.entries.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.name) },
                                onClick = { draft = draft.copy(activeProvider = provider); providerExpanded = false }
                            )
                        }
                    }
                }
                if (draft.activeProvider == ProviderType.OPENAI_COMPATIBLE) {
                    Box {
                        OutlineGoldButton(ProviderCatalog.find(draft.compatiblePresetId)?.name ?: "Wybierz preset providera", { presetExpanded = true })
                        DropdownMenu(presetExpanded, { presetExpanded = false }) {
                            ProviderCatalog.presets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.name + " · " + preset.note) },
                                    onClick = {
                                        draft = draft.copy(
                                            activeProvider = ProviderType.OPENAI_COMPATIBLE,
                                            compatiblePresetId = preset.id,
                                            compatibleBaseUrl = preset.baseUrl,
                                            compatibleModel = preset.defaultModel
                                        )
                                        presetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                ProviderFields(draft) { draft = it }
            }

            GoldCard(title = "RAG · LOKALNA BAZA WIEDZY", icon = Icons.Default.Storage) {
                ToggleLine("Włącz RAG", prefDraft.ragEnabled) { prefDraft = prefDraft.copy(ragEnabled = it) }
                OutlinedTextField(
                    prefDraft.ragTopK.toString(),
                    { prefDraft = prefDraft.copy(ragTopK = it.toIntOrNull()?.coerceIn(1, 12) ?: prefDraft.ragTopK) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Liczba wyników RAG") }
                )
                OutlineGoldButton("DODAJ DOKUMENT DO RAG", { ragPicker.launch(arrayOf("text/*", "application/json")) }, icon = Icons.Default.AttachFile)
                OutlineGoldButton("WYCZYŚĆ BAZĘ RAG", { viewModel.clearRag { ragStatus = it } }, icon = Icons.Default.Delete)
                if (ragStatus.isNotBlank()) Text(ragStatus, color = MaterialTheme.colorScheme.primary)
            }

            GoldCard(title = "MEMORY · WORKING / PERMANENT / GRAPH", icon = Icons.Default.Psychology) {
                ToggleLine("Pamięć robocza", prefDraft.workingMemoryEnabled) { prefDraft = prefDraft.copy(workingMemoryEnabled = it) }
                ToggleLine("Pamięć stała", prefDraft.permanentMemoryEnabled) { prefDraft = prefDraft.copy(permanentMemoryEnabled = it) }
                OutlinedTextField(
                    prefDraft.memoryTopK.toString(),
                    { prefDraft = prefDraft.copy(memoryTopK = it.toIntOrNull()?.coerceIn(1, 12) ?: prefDraft.memoryTopK) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Liczba wspomnień do retrieval") }
                )
                Text("Graf pamięci dostępny z Menu → Holographic Memory.", color = MaterialTheme.colorScheme.primary)
            }

            GoldCard(title = "INTERNET / WEB ACCESS", icon = Icons.Default.Public) {
                ToggleLine("Pobieraj treść podanych adresów HTTP(S)", prefDraft.webAccess) { prefDraft = prefDraft.copy(webAccess = it) }
                Text("Bez klucza wyszukiwarki aplikacja wykonuje bezpośredni fetch URL; nie udaje pełnego indeksu wyszukiwania.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            GoldCard(title = "GGUF · LOCAL INFERENCE", icon = Icons.Default.Memory) {
                Text("Wybrany model: " + draft.localModelPath.ifBlank { "nie wybrano" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                GoldButton("WYBIERZ GGUF Z TELEFONU", { ggufPicker.launch(arrayOf("*/*")) }, icon = Icons.Default.FolderOpen)
                OutlinedTextField(prefDraft.contextSize.toString(), { prefDraft = prefDraft.copy(contextSize = it.toIntOrNull()?.coerceIn(1024, 32768) ?: prefDraft.contextSize) }, Modifier.fillMaxWidth(), label = { Text("Context size") })
                OutlinedTextField(prefDraft.maxTokens.toString(), { prefDraft = prefDraft.copy(maxTokens = it.toIntOrNull()?.coerceIn(64, 8192) ?: prefDraft.maxTokens) }, Modifier.fillMaxWidth(), label = { Text("Max tokens") })
                OutlinedTextField(prefDraft.temperature.toString(), { prefDraft = prefDraft.copy(temperature = it.toFloatOrNull()?.coerceIn(0f, 2f) ?: prefDraft.temperature) }, Modifier.fillMaxWidth(), label = { Text("Temperature") })
                OutlinedTextField(prefDraft.topP.toString(), { prefDraft = prefDraft.copy(topP = it.toFloatOrNull()?.coerceIn(0.05f, 1f) ?: prefDraft.topP) }, Modifier.fillMaxWidth(), label = { Text("Top-P") })
                OutlinedTextField(prefDraft.threads.toString(), { prefDraft = prefDraft.copy(threads = it.toIntOrNull()?.coerceIn(1, 32) ?: prefDraft.threads) }, Modifier.fillMaxWidth(), label = { Text("CPU threads") })
                OutlinedTextField(prefDraft.gpuLayers.toString(), { prefDraft = prefDraft.copy(gpuLayers = it.toIntOrNull()?.coerceIn(0, 128) ?: prefDraft.gpuLayers) }, Modifier.fillMaxWidth(), label = { Text("GPU layers") })
                OutlinedTextField(prefDraft.repeatPenalty.toString(), { prefDraft = prefDraft.copy(repeatPenalty = it.toFloatOrNull()?.coerceIn(0.8f, 2f) ?: prefDraft.repeatPenalty) }, Modifier.fillMaxWidth(), label = { Text("Repeat penalty") })
            }

            GoldCard(title = "HUGGING FACE · GGUF DOWNLOADER", icon = Icons.Default.CloudDownload) {
                Text("Resolver: huggingface.co/{repo}/resolve/{revision}/{file}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(hfRepo, { hfRepo = it }, Modifier.fillMaxWidth(), label = { Text("Repo ID, np. Qwen/...") })
                OutlinedTextField(hfFile, { hfFile = it }, Modifier.fillMaxWidth(), label = { Text("Nazwa pliku .gguf") })
                OutlinedTextField(hfRevision, { hfRevision = it }, Modifier.fillMaxWidth(), label = { Text("Revision") })
                OutlinedTextField(hfToken, { hfToken = it }, Modifier.fillMaxWidth(), label = { Text("HF token (opcjonalny)") }, visualTransformation = PasswordVisualTransformation())
                GoldButton("POBIERZ GGUF", { viewModel.downloadHf(hfRepo, hfFile, hfRevision, hfToken) { hfStatus = it } }, enabled = hfRepo.isNotBlank() && hfFile.isNotBlank(), icon = Icons.Default.Download)
                if (hfStatus.isNotBlank()) Text(hfStatus, color = MaterialTheme.colorScheme.primary)
            }

            GoldCard(title = "CREATIVE API", icon = Icons.Default.AutoAwesome) {
                SecretField("ElevenLabs API key", elevenLabsDraft) { elevenLabsDraft = it }
                OutlinedTextField(
                    prefDraft.paulaElevenLabsVoiceId,
                    { prefDraft = prefDraft.copy(paulaElevenLabsVoiceId = it) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Paula · ElevenLabs voice ID") },
                    singleLine = true
                )
                Text(
                    "Po utworzeniu/wybraniu głosu Pauli w ElevenLabs wklej tutaj voice_id. Puste pole = fallback Android TTS.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SecretField("xAI API key", prefDraft.xaiKey) { prefDraft = prefDraft.copy(xaiKey = it) }
                SecretField("Canva Connect access token", canvaDraft) { canvaDraft = it }
                Text("Token i klucz ElevenLabs są przechowywane w EncryptedSharedPreferences. Produkcyjny OAuth Canva wymaga Authorization Code + PKCE.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            GoldButton(
                "ZAPISZ WSZYSTKO",
                {
                    viewModel.update(draft)
                    viewModel.updatePreferences(prefDraft)
                    viewModel.updateCanvaAccessToken(canvaDraft)
                    viewModel.updateElevenLabsKey(elevenLabsDraft)
                    onBack()
                },
                icon = Icons.Default.Save
            )
        }
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f))
        GoldSwitch(checked, onChecked)
    }
}

@Composable
private fun SecretField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
}

@Composable
private fun ProviderFields(config: ProviderConfig, onChange: (ProviderConfig) -> Unit) {
    when (config.activeProvider) {
        ProviderType.OPENAI -> {
            SecretField("OpenAI API key", config.openAiKey) { onChange(config.copy(openAiKey = it)) }
            OutlinedTextField(config.openAiModel, { onChange(config.copy(openAiModel = it)) }, Modifier.fillMaxWidth(), label = { Text("Model") })
        }
        ProviderType.ANTHROPIC -> {
            SecretField("Anthropic API key", config.anthropicKey) { onChange(config.copy(anthropicKey = it)) }
            OutlinedTextField(config.anthropicModel, { onChange(config.copy(anthropicModel = it)) }, Modifier.fillMaxWidth(), label = { Text("Model") })
        }
        ProviderType.GEMINI -> {
            SecretField("Gemini API key", config.geminiKey) { onChange(config.copy(geminiKey = it)) }
            OutlinedTextField(config.geminiModel, { onChange(config.copy(geminiModel = it)) }, Modifier.fillMaxWidth(), label = { Text("Model") })
        }
        ProviderType.OPENAI_COMPATIBLE -> {
            OutlinedTextField(config.compatibleBaseUrl, { onChange(config.copy(compatibleBaseUrl = it)) }, Modifier.fillMaxWidth(), label = { Text("Base URL") })
            SecretField("API key", config.compatibleKey) { onChange(config.copy(compatibleKey = it)) }
            OutlinedTextField(config.compatibleModel, { onChange(config.copy(compatibleModel = it)) }, Modifier.fillMaxWidth(), label = { Text("Model") })
        }
        ProviderType.LOCAL_GGUF -> Unit
    }
}

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
