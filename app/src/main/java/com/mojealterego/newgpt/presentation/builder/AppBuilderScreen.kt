package com.mojealterego.newgpt.presentation.builder

import com.mojealterego.newgpt.presentation.theme.BrandGlobalHeader
import com.mojealterego.newgpt.presentation.theme.BrandPageHeader
import com.mojealterego.newgpt.presentation.theme.LuxuryCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
    BrandBackground {
    var brief by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Kotlin + Jetpack Compose / Android") }
    Scaffold(
            containerColor = Color.Transparent,
        topBar = { BrandGlobalHeader(onMenu = onBack) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandPageHeader("AI APP BUILDER", "OD POMYSŁU DO SPECYFIKACJI PRODUKCYJNEJ", onBack)
            Text("Od pomysłu do specyfikacji produkcyjnej", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(platform, { platform = it }, Modifier.fillMaxWidth(), label = { Text("Platforma / stack") })
            OutlinedTextField(
                brief, { brief = it }, Modifier.fillMaxWidth(),
                minLines = 7, label = { Text("Opisz aplikację") }
            )
            Button(
                onClick = { viewModel.generate(brief, platform) },
                enabled = brief.isNotBlank() && !viewModel.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (viewModel.busy) "GENEROWANIE..." else "ZAPROJEKTUJ APLIKACJĘ") }
            if (viewModel.result.isNotBlank()) {
                Text(viewModel.result, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    }
}
