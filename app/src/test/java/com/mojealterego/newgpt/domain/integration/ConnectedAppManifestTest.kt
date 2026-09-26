package com.mojealterego.newgpt.domain.integration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectedAppManifestTest {
    @Test
    fun acceptsReadOnlyManifest() {
        val manifest = ConnectedAppManifest(
            id = "example-reader",
            name = "Example Reader",
            version = "1.0.0",
            category = "Authors",
            protocols = listOf(FabricProtocol.REST.name),
            actions = listOf(
                ConnectorActionManifest(
                    id = "search",
                    description = "Search content",
                    risk = "READ_ONLY"
                )
            )
        )
        assertTrue(ConnectedAppManifestValidator.validate(manifest).valid)
    }

    @Test
    fun rejectsWriteActionWithoutConfirmation() {
        val manifest = ConnectedAppManifest(
            id = "unsafe-writer",
            name = "Unsafe Writer",
            version = "1.0.0",
            category = "Authors",
            protocols = listOf(FabricProtocol.REST.name),
            actions = listOf(
                ConnectorActionManifest(
                    id = "publish",
                    description = "Publish content",
                    risk = "WRITE",
                    requiresConfirmation = false
                )
            )
        )
        assertFalse(ConnectedAppManifestValidator.validate(manifest).valid)
    }

    @Test
    fun rejectsUnsupportedProtocol() {
        val manifest = ConnectedAppManifest(
            id = "bad-protocol",
            name = "Bad Protocol",
            version = "1.0.0",
            category = "Test",
            protocols = listOf("MAGIC")
        )
        assertFalse(ConnectedAppManifestValidator.validate(manifest).valid)
    }
}
