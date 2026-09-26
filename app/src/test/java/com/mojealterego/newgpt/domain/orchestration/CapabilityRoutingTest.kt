package com.mojealterego.newgpt.domain.orchestration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityRoutingTest {
    @Test
    fun routesMediaToCreativeGateway() = kotlinx.coroutines.runBlocking {
        val gateway = FakeCreativeGateway()
        val router = CapabilityRouter(gateway)

        val result = router.execute(
            MediaRequest(
                kind = MediaKind.VIDEO,
                prompt = "Create a cinematic trailer"
            )
        )

        assertTrue(result.accepted)
        assertEquals(MediaKind.VIDEO, gateway.lastKind)
    }
}

private class FakeCreativeGateway : CreativeGateway {
    var lastKind: MediaKind? = null

    override suspend fun generate(request: MediaRequest): MediaResult {
        lastKind = request.kind
        return MediaResult(true, "local://pending")
    }
}
