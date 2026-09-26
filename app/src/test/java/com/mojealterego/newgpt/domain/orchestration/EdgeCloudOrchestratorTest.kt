package com.mojealterego.newgpt.domain.orchestration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeCloudOrchestratorTest {
    @Test
    fun offlineVoiceRequestUsesLocalPath() = kotlinx.coroutines.runBlocking {
        val local = FakeLocalEngine("local answer")
        val cloud = FakeCloudVerifier()
        val avatar = FakeAvatar()
        val orchestrator = EdgeCloudOrchestrator(local, cloud, avatar)

        val result = orchestrator.handle(
            UnifiedRequest.VoiceChat("hello"),
            NetworkPolicy.OFFLINE_ONLY
        )

        assertEquals("local answer", result.text)
        assertEquals(1, local.calls)
        assertEquals(0, cloud.calls)
        assertEquals(1, avatar.calls)
    }

    @Test
    fun codeRequestUsesCloudWhenAllowed() = kotlinx.coroutines.runBlocking {
        val local = FakeLocalEngine("local")
        val cloud = FakeCloudVerifier()
        val avatar = FakeAvatar()
        val orchestrator = EdgeCloudOrchestrator(local, cloud, avatar)

        val result = orchestrator.handle(
            UnifiedRequest.Code("fun x() = 1"),
            NetworkPolicy.CLOUD_ALLOWED
        )

        assertTrue(result.verified)
        assertEquals(1, cloud.calls)
    }
}

private class FakeLocalEngine(private val answer: String) : LocalInferenceEngine {
    var calls = 0
    override suspend fun generate(prompt: String): String {
        calls++
        return answer
    }
}

private class FakeCloudVerifier : CodeVerificationGateway {
    var calls = 0
    override suspend fun verify(code: String): VerificationResult {
        calls++
        return VerificationResult(true, "cloud-verifier")
    }
}

private class FakeAvatar : DigitalAvatarGateway {
    var calls = 0
    override suspend fun speak(text: String): AvatarOutput {
        calls++
        return AvatarOutput(text)
    }
}
