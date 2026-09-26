package com.mojealterego.newgpt.data.voice

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.data.remote.PaulaVoiceDirectorService
import com.mojealterego.newgpt.domain.voice.VoiceChatEngine
import com.mojealterego.newgpt.domain.voice.VoiceSessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceChatEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val paulaVoice: PaulaVoiceDirectorService,
    private val secureSettings: SecureSettings,
    private val preferences: com.mojealterego.newgpt.data.local.AppPreferencesStore
) : VoiceChatEngine, RecognitionListener, TextToSpeech.OnInitListener {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(VoiceSessionState.IDLE)
    override val state: kotlinx.coroutines.flow.StateFlow<VoiceSessionState> = _state

    private val _transcript = kotlinx.coroutines.flow.MutableStateFlow("")
    override val transcript: kotlinx.coroutines.flow.StateFlow<String> = _transcript

    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    override val error: kotlinx.coroutines.flow.StateFlow<String?> = _error

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(this)
            }
        }
        tts = TextToSpeech(context, this)
    }

    override fun startListening() {
        val r = recognizer ?: run {
            fail("Rozpoznawanie mowy nie jest dostępne na tym urządzeniu.")
            return
        }
        stopSpeaking()
        _error.value = null
        _transcript.value = ""
        _state.value = VoiceSessionState.LISTENING

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("pl-PL"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        if (_state.value == VoiceSessionState.LISTENING) {
            _state.value = VoiceSessionState.IDLE
        }
    }

    override fun speak(text: String) {
        if (text.isBlank()) return
        stopSpeaking()

        val prefs = preferences.preferences.value
        val key = secureSettings.elevenLabsKey.value.trim()
        val voiceId = prefs.paulaElevenLabsVoiceId.trim()

        if (key.isNotBlank() && voiceId.isNotBlank()) {
            _error.value = null
            _state.value = VoiceSessionState.SPEAKING
            scope.launch {
                runCatching { paulaVoice.textToSpeech(key, voiceId, text) }
                    .onSuccess { playElevenLabsAudio(it) }
                    .onFailure {
                        _error.value = "ElevenLabs: " + (it.message ?: "nieznany błąd")
                        fallbackToAndroidTts(text)
                    }
            }
            return
        }

        fallbackToAndroidTts(text)
    }

    private fun fallbackToAndroidTts(text: String) {
        if (!ttsReady) {
            fail("Brak gotowego syntezatora mowy. Skonfiguruj ElevenLabs lub syntezator Android.")
            return
        }
        _state.value = VoiceSessionState.SPEAKING
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "newgpt-response")
    }

    private fun playElevenLabsAudio(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            fallbackToAndroidTts("Nie udało się wygenerować odpowiedzi głosowej.")
            return
        }

        val file = File(context.cacheDir, "paula-tts.mp3")
        file.writeBytes(bytes)
        audioFile = file

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                releaseMediaPlayer()
                _state.value = VoiceSessionState.IDLE
            }
            setOnErrorListener { _, _, _ ->
                releaseMediaPlayer()
                _error.value = "Nie udało się odtworzyć głosu Pauli."
                _state.value = VoiceSessionState.ERROR
                true
            }
            prepareAsync()
        }
    }

    override fun stopSpeaking() {
        scope.coroutineContext.cancelChildren()
        tts?.stop()
        releaseMediaPlayer()
        if (_state.value == VoiceSessionState.SPEAKING) {
            _state.value = VoiceSessionState.IDLE
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.setOnErrorListener(null)
        runCatching { mediaPlayer?.stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        audioFile?.delete()
        audioFile = null
    }

    override fun release() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        releaseMediaPlayer()
        scope.cancel()
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        if (_state.value == VoiceSessionState.LISTENING) {
            _state.value = VoiceSessionState.THINKING
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    override fun onPartialResults(results: Bundle?) {
        _transcript.value =
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
    }

    override fun onResults(results: Bundle?) {
        _transcript.value =
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
        _state.value = VoiceSessionState.IDLE
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_NO_MATCH ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
        ) {
            _state.value = VoiceSessionState.IDLE
        } else {
            fail("Rozpoznawanie mowy zakończone błędem: " + error)
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale.forLanguageTag("pl-PL")
        }
    }

    private fun fail(message: String) {
        _error.value = message
        _state.value = VoiceSessionState.ERROR
    }
}
