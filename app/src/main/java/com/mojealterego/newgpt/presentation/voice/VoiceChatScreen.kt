package com.mojealterego.newgpt.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.domain.voice.VoiceChatEngine
import com.mojealterego.newgpt.domain.voice.VoiceSessionState
import com.mojealterego.newgpt.domain.usecase.SendMessageUseCase
import com.mojealterego.newgpt.data.local.SecureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceChatViewModel @Inject constructor(
    private val voice: VoiceChatEngine,
    private val sendMessage: SendMessageUseCase,
    private val settings: SecureSettings
) : ViewModel() {
    val state = voice.state
    val transcript = voice.transcript
    val error = voice.error

    private var lastSubmitted = ""

    fun listen() = voice.startListening()
    fun stop() = voice.stopListening()
    fun stopSpeaking() = voice.stopSpeaking()

    fun submitTranscript() {
        val text = transcript.value.trim()
        if (text.isBlank() || text == lastSubmitted) return
        lastSubmitted = text
        viewModelScope.launch {
            try {
                sendMessage("default", text, settings.config.value)
            } catch (t: Throwable) {
                voice.speak("Nie udało się wysłać wiadomości.")
            }
        }
    }

    fun speak(text: String) {
        voice.speak(text)
    }

    override fun onCleared() {
        voice.release()
        super.onCleared()
    }
}

@Composable
fun VoiceChatScreen(
    onBack: () -> Unit,
    viewModel: VoiceChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) viewModel.listen()
    }

    LaunchedEffect(state) {
        if (state == VoiceSessionState.THINKING) viewModel.submitTranscript()
    }

    Scaffold(
        containerColor = Color(0xFF050506),
        topBar = {
            TopAppBar(
                title = { Text("NEWGPT · VOICE") },
                navigationIcon = { IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("VOICE CONTROL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(14.dp))
            Text(
                when (state) {
                    VoiceSessionState.LISTENING -> "Słucham…"
                    VoiceSessionState.THINKING -> "Przetwarzam…"
                    VoiceSessionState.SPEAKING -> "Odpowiadam…"
                    VoiceSessionState.ERROR -> "Błąd"
                    VoiceSessionState.IDLE -> "Naciśnij mikrofon"
                },
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                color = Color(0xFF111114),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (state == VoiceSessionState.SPEAKING) Icons.Default.VolumeUp else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(76.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                transcript.ifBlank { "Rozmowa głosowa z NewGPT" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                FilledTonalButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.listen()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = state != VoiceSessionState.LISTENING && state != VoiceSessionState.THINKING
                ) { Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text("Mów") }
                OutlinedButton(onClick = { viewModel.stop(); viewModel.stopSpeaking() }) {
                    Icon(Icons.Default.Stop, null); Spacer(Modifier.width(8.dp)); Text("Stop")
                }
            }
            if (permissionDenied) {
                Spacer(Modifier.height(12.dp))
                Text("NewGPT potrzebuje dostępu do mikrofonu.", color = MaterialTheme.colorScheme.error)
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
