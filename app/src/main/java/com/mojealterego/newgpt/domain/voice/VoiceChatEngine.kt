package com.mojealterego.newgpt.domain.voice

import kotlinx.coroutines.flow.StateFlow

enum class VoiceSessionState { IDLE, LISTENING, THINKING, SPEAKING, ERROR }

interface VoiceChatEngine {
    val state: StateFlow<VoiceSessionState>
    val transcript: StateFlow<String>
    val error: StateFlow<String?>

    fun startListening()
    fun stopListening()
    fun speak(text: String)
    fun stopSpeaking()
    fun release()
}
