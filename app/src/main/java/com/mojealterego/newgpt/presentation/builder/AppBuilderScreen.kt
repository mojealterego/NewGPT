package com.mojealterego.newgpt.presentation.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.presentation.theme.*
import com.mojealterego.newgpt.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppBuilderViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val secureSettings: SecureSettings
) : ViewModel() {
    var result by mutableStateOf("")
        private set
    var busy by mutableStateOf(false)
        private set

    fun generate(brief: String, platform: String) {
        viewModelScope.launch {
            busy = true
            result = "Projektowanie..."
            val system = """Jesteś NewGPT App Builder. Z briefu tworzysz produkcyjną specyfikację aplikacji.
Platforma: $platform.
Zwróć: 1) cel, 2) ekrany i nawigację, 3) model danych, 4) logikę, 5) API/integracje, 6) bezpieczeństwo, 7) testy, 8) strukturę projektu i kluczowe pliki, 9) kolejność implementacji.
Nie twierdź, że aplikacja została zbudowana lub wdrożona. Twórz konkretne kontrakty gotowe do przekazania agentowi Coder."""
            result = repository.sendAgentMessage(
                conversationId = "app-builder",
                prompt = brief,
                config = secureSettings.config.value,
                systemPrompt = system
            )
            busy = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBuilderScreen(onBack: () -> Unit, viewModel: AppBuilderViewModel = hiltViewModel()) {
    var brief by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Kotlin + Jetpack Compose / Android") }
    PremiumScaffold(
        selected = "Narzędzia",
        title = "AI APP BUILDER",
        subtitle = "OD POMYSŁU DO SPECYFIKACJI PRODUKCYJNEJ",
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
            GoldCard(title = "PLATFORMA / STACK", icon = Icons.Default.Layers) {
                OutlinedTextField(platform, { platform = it }, Modifier.fillMaxWidth(), label = { Text("Platforma / stack") })
            }
            GoldCard(title = "OPISZ APLIKACJĘ", icon = Icons.Default.Description) {
                OutlinedTextField(
                    brief, { brief = it }, Modifier.fillMaxWidth(),
                    minLines = 8,
                    label = { Text("Brief produkcyjny") },
                    placeholder = { Text("Cel, funkcje, użytkownicy, styl, integracje, wymagania…") }
                )
                GoldButton(
                    if (viewModel.busy) "PROJEKTOWANIE…" else "ZAPROJEKTUJ APLIKACJĘ",
                    { viewModel.generate(brief, platform) },
                    enabled = brief.isNotBlank() && !viewModel.busy,
                    icon = Icons.Default.Build
                )
            }
            GoldCard(title = "PODGLĄD SPECYFIKACJI", icon = Icons.Default.AutoAwesome) {
                Text("Architektura · UI/UX · Funkcje · Zależności · Bezpieczeństwo · Build APK/AAB", color = MaterialTheme.colorScheme.primary)
                if (viewModel.result.isNotBlank()) {
                    HorizontalDivider(color = Color(0x555F4A18))
                    Text(viewModel.result, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("Wynik pojawi się po wygenerowaniu specyfikacji.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlineGoldButton("MOJE PROJEKTY", {}, Modifier.weight(1f), Icons.Default.Folder)
                OutlineGoldButton("SZABLONY", {}, Modifier.weight(1f), Icons.Default.Description)
                OutlineGoldButton("POMYSŁY", {}, Modifier.weight(1f), Icons.Default.Lightbulb)
            }
        }
    }
}
