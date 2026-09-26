package com.mojealterego.newgpt.presentation.paula

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.domain.assistant.PaulaRegistry
import com.mojealterego.newgpt.domain.usecase.SendMessageUseCase
import com.mojealterego.newgpt.domain.voice.VoiceChatEngine
import com.mojealterego.newgpt.domain.voice.VoiceSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaulaVideoChatViewModel @Inject constructor(
    private val voice: VoiceChatEngine,
    private val sendMessage: SendMessageUseCase,
    private val settings: SecureSettings
) : ViewModel() {
    val state = voice.state
    val transcript = voice.transcript
    val error = voice.error
    val assistant = PaulaRegistry.default

    fun listen() = voice.startListening()
    fun stop() = voice.stopListening()
    fun stopSpeaking() = voice.stopSpeaking()
    fun speak(text: String) = voice.speak(text)

    fun submit() {
        val text = transcript.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                sendMessage("paula", text, settings.config.value)
            } catch (_: Throwable) {
                voice.speak("Nie udało mi się wykonać tego polecenia.")
            }
        }
    }

    override fun onCleared() {
        voice.release()
        super.onCleared()
    }
}

@Composable
fun PaulaVideoChatScreen(
    onBack: () -> Unit,
    viewModel: PaulaVideoChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            volume = 0f
            val id = context.resources.getIdentifier("paula_default", "raw", context.packageName)
            if (id != 0) {
                setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$id"))
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(player) { onDispose { player.release() } }

    LaunchedEffect(state) {
        if (state == VoiceSessionState.THINKING) viewModel.submit()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("PAULA · VIDEO CHAT") },
                navigationIcon = { IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) } },
                actions = { IconButton(onClick = viewModel::stopSpeaking) { Icon(Icons.Default.Stop, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xCC050506),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            AndroidView(
                factory = { PlayerView(it).apply {
                    useController = false
                    player = player
                    layoutParams = ViewGroup.LayoutParams(-1, -1)
                }},
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xDD050506)
            ) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PAULA", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(
                        when (state) {
                            VoiceSessionState.LISTENING -> "Słucham…"
                            VoiceSessionState.THINKING -> "Pracuję…"
                            VoiceSessionState.SPEAKING -> "Odpowiadam…"
                            VoiceSessionState.ERROR -> "Błąd"
                            VoiceSessionState.IDLE -> "Gotowa do rozmowy"
                        },
                        color = Color.White
                    )
                    if (transcript.isNotBlank()) Text(transcript, color = Color(0xFFD7D7D7), modifier = Modifier.padding(top = 6.dp))
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(onClick = viewModel::listen) {
                            Icon(Icons.Default.Mic, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Mów")
                        }
                        OutlinedButton(onClick = { viewModel.stop(); viewModel.stopSpeaking() }) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Stop")
                        }
                        IconButton(onClick = { viewModel.speak("Jestem Paula. W czym mogę Ci pomóc?") }) {
                            Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp)) }
                }
            }
        }
    }
}
