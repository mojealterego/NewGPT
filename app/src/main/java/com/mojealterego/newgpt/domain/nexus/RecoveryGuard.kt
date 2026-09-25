package com.mojealterego.newgpt.domain.nexus

data class RecoveryManifest(
    val formatVersion: Int,
    val createdAt: Long,
    val appVersion: String,
    val encrypted: Boolean,
    val includesMemory: Boolean,
    val includesRag: Boolean,
    val includesSettings: Boolean
)

class RecoveryGuard {
    fun validate(manifest: RecoveryManifest, currentFormatVersion: Int): List<String> {
        val errors = mutableListOf<String>()
        if (manifest.formatVersion <= 0) errors += "INVALID_FORMAT_VERSION"
        if (manifest.formatVersion > currentFormatVersion) errors += "NEWER_FORMAT"
        if (!manifest.encrypted && manifest.includesMemory) errors += "SENSITIVE_DATA_MUST_BE_ENCRYPTED"
        if (manifest.appVersion.isBlank()) errors += "MISSING_APP_VERSION"
        return errors
    }
}
