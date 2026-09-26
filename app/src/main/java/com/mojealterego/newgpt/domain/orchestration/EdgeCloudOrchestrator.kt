package com.mojealterego.newgpt.domain.orchestration

enum class NetworkPolicy {
    OFFLINE_ONLY,
    CLOUD_ALLOWED
}

sealed interface UnifiedRequest {
    data class VoiceChat(val text: String) : UnifiedRequest
    data class Code(val source: String) : UnifiedRequest
}

data class VerificationResult(
    val accepted: Boolean,
    val verifiedBy: String
)

data class AvatarOutput(
    val text: String,
    val audioToken: String? = null
)

data class UnifiedResult(
    val text: String,
    val verified: Boolean = false,
    val verifiedBy: String? = null,
    val avatar: AvatarOutput? = null
)

interface LocalInferenceEngine {
    suspend fun generate(prompt: String): String
}

interface CodeVerificationGateway {
    suspend fun verify(code: String): VerificationResult
}

interface DigitalAvatarGateway {
    suspend fun speak(text: String): AvatarOutput
}

class EdgeCloudOrchestrator(
    private val local: LocalInferenceEngine,
    private val cloudVerifier: CodeVerificationGateway,
    private val avatar: DigitalAvatarGateway
) {
    suspend fun handle(
        request: UnifiedRequest,
        networkPolicy: NetworkPolicy
    ): UnifiedResult = when (request) {
        is UnifiedRequest.VoiceChat -> {
            val text = local.generate(request.text)
            UnifiedResult(text = text, avatar = avatar.speak(text))
        }

        is UnifiedRequest.Code -> {
            if (networkPolicy == NetworkPolicy.OFFLINE_ONLY) {
                UnifiedResult(text = local.generate(request.source))
            } else {
                val verification = cloudVerifier.verify(request.source)
                UnifiedResult(
                    text = request.source,
                    verified = verification.accepted,
                    verifiedBy = verification.verifiedBy
                )
            }
        }
    }
}
