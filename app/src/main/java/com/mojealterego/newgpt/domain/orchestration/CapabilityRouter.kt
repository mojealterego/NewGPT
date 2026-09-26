package com.mojealterego.newgpt.domain.orchestration

enum class MediaKind {
    IMAGE,
    VIDEO,
    VOICE,
    MUSIC
}

data class MediaRequest(
    val kind: MediaKind,
    val prompt: String,
    val referenceIds: List<String> = emptyList()
)

data class MediaResult(
    val accepted: Boolean,
    val outputLocation: String? = null,
    val error: String? = null
)

interface CreativeGateway {
    suspend fun generate(request: MediaRequest): MediaResult
}

class CapabilityRouter(
    private val creative: CreativeGateway
) {
    suspend fun execute(request: MediaRequest): MediaResult =
        creative.generate(request)
}
