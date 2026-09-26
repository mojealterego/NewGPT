package com.mojealterego.newgpt.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.mojealterego.newgpt.domain.voice.VoiceChatEngine
import com.mojealterego.newgpt.domain.voice.VoiceSessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceChatEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceChatEngine, RecognitionListener, TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(VoiceSessionState.IDLE)
    override val state: StateFlow<VoiceSessionState> = _state

    private val _transcript = MutableStateFlow("")
    override val transcript: StateFlow<String> = _transcript

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { it.setRecognitionListener(this) }
        }
        tts = TextToSpeech(context, this)
    }

    override fun startListening() {
        val r = recognizer ?: run {
            fail("Rozpoznawanie mowy nie jest dostępne na tym urządzeniu.")
            return
        }
        _error.value = null
        _transcript.value = ""
        _state.value = VoiceSessionState.LISTENING
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        if (_state.value == VoiceSessionState.LISTENING) _state.value = VoiceSessionState.IDLE
    }

    override fun speak(text: String) {
        if (!ttsReady || text.isBlank()) return
        _state.value = VoiceSessionState.SPEAKING
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "newgpt-response")
    }

    override fun stopSpeaking() {
        tts?.stop()
        if (_state.value == VoiceSessionState.SPEAKING) _state.value = VoiceSessionState.IDLE
    }

    override fun release() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() { if (_state.value == VoiceSessionState.LISTENING) _state.value = VoiceSessionState.THINKING }
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    override fun onPartialResults(results: Bundle?) {
        _transcript.value = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
    }

    override fun onResults(results: Bundle?) {
        _transcript.value = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        _state.value = VoiceSessionState.IDLE
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            _state.value = VoiceSessionState.IDLE
        } else {
            fail("Rozpoznawanie mowy zakończone błędem: $error")
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) tts?.language = Locale.getDefault()
    }

    private fun fail(message: String) {
        _error.value = message
        _state.value = VoiceSessionState.ERROR
    }
}
