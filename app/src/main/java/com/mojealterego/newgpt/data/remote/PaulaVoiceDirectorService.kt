package com.mojealterego.newgpt.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PaulaVoicePreview(
    val audioBase64: String,
    val generatedVoiceId: String,
    val mediaType: String,
    val durationSeconds: Double? = null,
    val language: String? = null
)

@Serializable
data class PaulaVoiceDesignResult(
    val previews: List<PaulaVoicePreview> = emptyList(),
    val text: String? = null
)

@Serializable
data class PaulaCreatedVoice(
    val voiceId: String,
    val name: String? = null
)

@Serializable
data class PaulaClonedVoice(
    val voiceId: String,
    val requiresVerification: Boolean = false
)

@Serializable
private data class VoiceDesignResponse(
    val previews: List<PreviewDto> = emptyList(),
    val text: String? = null
)

@Serializable
private data class PreviewDto(
    val audio_base_64: String,
    val generated_voice_id: String,
    val media_type: String,
    val duration_secs: Double? = null,
    val language: String? = null
)

@Serializable
private data class CreateVoiceResponse(
    val voice_id: String,
    val name: String? = null
)

@Serializable
private data class CloneVoiceResponse(
    val voice_id: String,
    val requires_verification: Boolean = false
)

@Singleton
class PaulaVoiceDirectorService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun designVoice(
        apiKey: String,
        description: String,
        text: String? = null
    ): PaulaVoiceDesignResult {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        require(description.length in 20..1000) { "Opis głosu musi mieć 20–1000 znaków." }

        val payload = buildString {
            append("""{"voice_description":""" + Json.encodeToString(description))
            if (!text.isNullOrBlank()) {
                require(text.length in 100..1000) { "Tekst preview musi mieć 100–1000 znaków." }
                append(""","text":""" + Json.encodeToString(text))
            }
            append("}")
        }

        val response = client.post("https://api.elevenlabs.io/v1/text-to-voice/design") {
            header("xi-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        check(response.status.value in 200..299) {
            "ElevenLabs Voice Design HTTP " + response.status.value + ": " + response.bodyAsText()
        }

        val data = Json.decodeFromString<VoiceDesignResponse>(response.bodyAsText())
        return PaulaVoiceDesignResult(
            previews = data.previews.map {
                PaulaVoicePreview(
                    audioBase64 = it.audio_base_64,
                    generatedVoiceId = it.generated_voice_id,
                    mediaType = it.media_type,
                    durationSeconds = it.duration_secs,
                    language = it.language
                )
            },
            text = data.text
        )
    }

    suspend fun createVoiceFromPreview(
        apiKey: String,
        generatedVoiceId: String,
        name: String = "Paula",
        description: String = PaulaVoiceProfile.DESCRIPTION
    ): PaulaCreatedVoice {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        require(generatedVoiceId.isNotBlank()) { "Brak generated_voice_id." }

        val body =
            """{"voice_name":""" + Json.encodeToString(name) +
            ""","voice_description":""" + Json.encodeToString(description) +
            ""","generated_voice_id":""" + Json.encodeToString(generatedVoiceId) + "}"

        val response = client.post("https://api.elevenlabs.io/v1/text-to-voice") {
            header("xi-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        check(response.status.value in 200..299) {
            "ElevenLabs Create Voice HTTP " + response.status.value + ": " + response.bodyAsText()
        }

        val data = Json.decodeFromString<CreateVoiceResponse>(response.bodyAsText())
        return PaulaCreatedVoice(data.voice_id, data.name)
    }

    suspend fun cloneInstantVoice(
        apiKey: String,
        audio: ByteArray,
        fileName: String = "paula-reference.mp3",
        name: String = "Paula"
    ): PaulaClonedVoice {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        require(audio.isNotEmpty()) { "Materiał referencyjny jest pusty." }

        val response = client.submitFormWithBinaryData(
            url = "https://api.elevenlabs.io/v1/voices/add",
            formData = formData {
                append("name", name)
                append(
                    "files",
                    fileName,
                    ContentType.Audio.MPEG,
                    audio.size.toLong()
                ) {
                    writeFully(audio)
                }
                append("description", PaulaVoiceProfile.DESCRIPTION)
            }
        ) {
            header("xi-api-key", apiKey)
        }

        check(response.status.value in 200..299) {
            "ElevenLabs Voice Clone HTTP " + response.status.value + ": " + response.bodyAsText()
        }

        val data = Json.decodeFromString<CloneVoiceResponse>(response.bodyAsText())
        return PaulaClonedVoice(data.voice_id, data.requires_verification)
    }

    suspend fun textToSpeech(
        apiKey: String,
        voiceId: String,
        text: String
    ): ByteArray {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        require(voiceId.isNotBlank()) { "Brak voice_id Pauli." }
        require(text.isNotBlank()) { "Tekst Pauli jest pusty." }

        val body =
            """{"text":""" + Json.encodeToString(text) +
            ""","model_id":"eleven_multilingual_v2"}"""

        val response = client.post(
            "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "?output_format=mp3_44100_128"
        ) {
            header("xi-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        check(response.status.value in 200..299) {
            "ElevenLabs TTS HTTP " + response.status.value + ": " + response.bodyAsText()
        }
        return response.readBytes()
    }
}

object PaulaVoiceProfile {
    const val ID = "paula-v1"
    const val LOCALE = "pl-PL"
    const val DESCRIPTION =
        "Adult Polish woman, approximately 23–26 years old. Native Polish pronunciation, natural pl-PL accent, medium to slightly low feminine register. Warm, smooth, soft, rich and realistic timbre with natural intimate resonance. Calm, intelligent, confident and conversational. Controlled pace, natural pauses, precise articulation and subtle natural breath. Warm, subtly sensual, flirtatious and coquettish when appropriate, capable of a restrained seductive tone while remaining natural and adult. Also capable of a firm, assertive professional delivery. Never childlike or teenage. Never robotic, synthetic, announcer-like, excessively breathy, pornographic, caricatured, nasal, overly bright or overacted."
}
