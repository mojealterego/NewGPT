package com.mojealterego.newgpt.domain.nexus

import org.junit.Assert.*
import org.junit.Test

class ModelCatalogTest {
    @Test
    fun catalog_filters_and_verifies_artifacts() {
        val catalog = ModelCatalog()
        catalog.register(
            ModelDescriptor(
                id = "local-qwen",
                provider = "local",
                family = "Qwen",
                local = true,
                multimodal = setOf(MediaKind.TEXT, MediaKind.IMAGE),
                contextTokens = 8192,
                quantization = "Q4_K_M",
                artifactSha256 = "abc"
            )
        )
        assertEquals(1, catalog.find("qwen").size)
        assertEquals(1, catalog.compatible(MediaKind.IMAGE).size)
        assertTrue(catalog.verifyArtifact("local-qwen", "ABC"))
    }

    @Test
    fun recovery_rejects_unencrypted_sensitive_backup() {
        val errors = RecoveryGuard().validate(
            RecoveryManifest(1, 1L, "0.1.0", encrypted = false, includesMemory = true, includesRag = true, includesSettings = true),
            currentFormatVersion = 1
        )
        assertTrue(errors.contains("SENSITIVE_DATA_MUST_BE_ENCRYPTED"))
    }
}
