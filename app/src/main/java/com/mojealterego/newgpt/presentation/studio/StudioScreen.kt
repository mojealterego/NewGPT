package com.mojealterego.newgpt.presentation.studio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.AppPreferencesStore
import com.mojealterego.newgpt.data.remote.CreativeStudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StudioViewModel @Inject constructor(
    private val prefs: AppPreferencesStore,
    private val service: CreativeStudioService
) : ViewModel() {
    var status by mutableStateOf("")
        private set

    fun speech(voice: String, text: String) = run("TTS") {
        val bytes = service.textToSpeech(prefs.preferences.value.elevenLabsKey, voice, text)
        File.createTempFile("newgpt-voice-", ".mp3").apply { writeBytes(bytes) }
        "Wygenerowano audio: " + absolutePath
    }

    fun music(prompt: String) = run("Music") {
        val bytes = service.music(prefs.preferences.value.elevenLabsKey, prompt)
        File.createTempFile("newgpt-music-", ".mp3").apply { writeBytes(bytes) }
        "Wygenerowano muzykę: " + absolutePath
    }

    fun video(prompt: String) = run("Video") {
        "Wideo: " + service.generateVideo(prefs.preferences.value.xaiKey, prompt)
    }

    private fun run(label: String, block: suspend () -> String) {
        viewModelScope.launch {
            status = "Generowanie " + label + "…"
            status = runCatching { block() }.getOrElse { "Błąd " + label + ": " + (it.message ?: "nieznany") }
        }
    }
}

@Composable
fun StudioScreen(onBack: () -> Unit, viewModel: StudioViewModel = hiltViewModel()) {
    var text by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CREATIVE STUDIO") },
                navigationIcon = { Button(onClick = onBack) { Text("‹") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(18.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ElevenLabs · Voice · Music · xAI Video", style = MaterialTheme.typography.headlineSmall)
            Text("Klucze API ustaw w Ustawieniach.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                label = { Text("Prompt / tekst") }
            )
            Button(
                onClick = { viewModel.speech("JBFqnCBsd6RMkjVDRZzb", text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank()
            ) { Text("GENERUJ GŁOS") }
            Button(
                onClick = { viewModel.music(text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank()
            ) { Text("GENERUJ MUZYKĘ") }
            Button(
                onClick = { viewModel.video(text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank()
            ) { Text("GENERUJ WIDEO") }
            Text(viewModel.status, color = MaterialTheme.colorScheme.primary)
            Text(
                "Canva i zewnętrzne App Buildery są przygotowane jako warstwa integracji API/webhook. Nie są oznaczane jako natywne integracje bez udokumentowanego API.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
