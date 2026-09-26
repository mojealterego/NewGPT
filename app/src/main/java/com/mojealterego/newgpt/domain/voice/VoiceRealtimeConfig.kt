package com.mojealterego.newgpt.domain.voice

data class VoiceRealtimeConfig(
    val enabled: Boolean = false,
    val ephemeralSessionEndpoint: String = "",
    val model: String = "gpt-realtime",
    val voice: String = "marin",
    val instructions: String = "Jesteś głosowym asystentem NewGPT. Odpowiadaj naturalnie i zwięźle."
)
