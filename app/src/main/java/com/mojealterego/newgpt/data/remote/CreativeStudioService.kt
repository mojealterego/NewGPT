package com.mojealterego.newgpt.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ElevenSpeechRequest(val text: String, val model_id: String = "eleven_multilingual_v2")
@Serializable
private data class ElevenMusicRequest(val prompt: String, val model_id: String = "music_v2_5", val music_length_ms: Int? = null)
@Serializable
private data class XaiVideoRequest(
    val model: String = "grok-imagine-video-1.5",
    val prompt: String,
    val duration: Int = 6,
    val aspect_ratio: String = "16:9",
    val resolution: String = "720p"
)
@Serializable private data class XaiVideoStart(val request_id: String)
@Serializable private data class XaiVideoResult(val status: String, val video: XaiVideoOutput? = null)
@Serializable private data class XaiVideoOutput(val url: String)
@Serializable private data class CanvaDesignType(val type: String, val name: String)
@Serializable private data class CanvaCreateRequest(
    val type: String = "type_and_asset",
    val design_type: CanvaDesignType,
    val title: String
)
@Serializable private data class CanvaUrls(val edit_url: String, val view_url: String)
@Serializable private data class CanvaDesign(val id: String, val urls: CanvaUrls, val title: String? = null)
@Serializable private data class CanvaCreateResponse(val design: CanvaDesign)

@Singleton
class CreativeStudioService @Inject constructor(private val client: HttpClient) {
    suspend fun textToSpeech(apiKey: String, voiceId: String, text: String): ByteArray {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        val response = client.post("https://api.elevenlabs.io/v1/text-to-speech/$voiceId?output_format=mp3_44100_128") {
            header("xi-api-key", apiKey)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(ElevenSpeechRequest(text))
        }
        check(response.status.value in 200..299) { "ElevenLabs HTTP " + response.status.value }
        return response.readBytes()
    }

    suspend fun music(apiKey: String, prompt: String, lengthMs: Int? = null): ByteArray {
        require(apiKey.isNotBlank()) { "Brak klucza ElevenLabs." }
        val response = client.post("https://api.elevenlabs.io/v1/music?output_format=mp3_44100_128") {
            header("xi-api-key", apiKey)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(ElevenMusicRequest(prompt = prompt, music_length_ms = lengthMs))
        }
        check(response.status.value in 200..299) { "ElevenLabs HTTP " + response.status.value }
        return response.readBytes()
    }

    suspend fun createCanvaDesign(accessToken: String, title: String, preset: String = "presentation"): String {
        require(accessToken.isNotBlank()) { "Brak Canva Connect access token." }
        require(preset in setOf("doc", "email", "presentation", "whiteboard")) { "Nieobsługiwany preset Canva." }
        val response = client.post("https://api.canva.com/rest/v1/designs") {
            header(HttpHeaders.Authorization, "Bearer " + accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(CanvaCreateRequest(design_type = CanvaDesignType("preset", preset), title = title.take(255)))
        }
        check(response.status.value in 200..299) { "Canva HTTP " + response.status.value + ": " + response.bodyAsText() }
        return Json.decodeFromString<CanvaCreateResponse>(response.bodyAsText()).design.urls.edit_url
    }

    suspend fun generateVideo(apiKey: String, prompt: String): String {
        require(apiKey.isNotBlank()) { "Brak klucza xAI." }
        val start = client.post("https://api.x.ai/v1/videos/generations") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(XaiVideoRequest(prompt = prompt))
        }
        check(start.status.value in 200..299) { "xAI HTTP " + start.status.value + ": " + start.bodyAsText() }
        val requestId = Json.decodeFromString<XaiVideoStart>(start.bodyAsText()).request_id
        repeat(60) {
            delay(5000)
            val result = client.get("https://api.x.ai/v1/videos/$requestId") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            check(result.status.value in 200..299) { "xAI HTTP " + result.status.value }
            val data = Json.decodeFromString<XaiVideoResult>(result.bodyAsText())
            if (data.status == "done") return data.video?.url ?: error("xAI nie zwrócił URL filmu.")
            if (data.status == "failed" || data.status == "expired") {
                error("Generowanie filmu zakończone statusem " + data.status + ".")
            }
        }
        error("Przekroczono limit oczekiwania na generowanie filmu.")
    }
}
