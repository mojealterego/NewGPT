package com.mojealterego.newgpt.domain.integration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ConnectorActionManifest(
    val id: String,
    val description: String,
    val risk: String = "READ_ONLY",
    val requiresConfirmation: Boolean = risk != "READ_ONLY",
    val inputSchema: Map<String, String> = emptyMap(),
    val outputSchema: Map<String, String> = emptyMap()
)

@Serializable
data class ConnectedAppManifest(
    val id: String,
    val name: String,
    val version: String,
    val category: String,
    val protocols: List<String>,
    val auth: List<String> = emptyList(),
    val oauthScopes: List<String> = emptyList(),
    val dataClassifications: List<String> = listOf("PUBLIC"),
    val actions: List<ConnectorActionManifest> = emptyList(),
    val webhookEvents: List<String> = emptyList(),
    val privacyPolicyUrl: String? = null,
    val providerHomepage: String? = null,
    val signature: String? = null
)

data class ManifestValidation(
    val valid: Boolean,
    val errors: List<String>
)

object ConnectedAppManifestValidator {
    private val allowedRisks = setOf("READ_ONLY", "WRITE", "SENSITIVE")
    private val allowedProtocols = FabricProtocol.entries.map { it.name }.toSet()

    fun validate(manifest: ConnectedAppManifest): ManifestValidation {
        val errors = buildList {
            if (!manifest.id.matches(Regex("[a-z0-9][a-z0-9._-]{1,127}"))) add("Invalid connector id")
            if (manifest.name.isBlank()) add("Connector name is required")
            if (manifest.version.isBlank()) add("Connector version is required")
            if (manifest.category.isBlank()) add("Connector category is required")
            if (manifest.protocols.isEmpty()) add("At least one protocol is required")
            manifest.protocols.filterNot { it in allowedProtocols }.forEach {
                add("Unsupported protocol: $it")
            }
            manifest.actions.forEach { action ->
                if (!action.id.matches(Regex("[a-z0-9][a-z0-9._-]{0,127}"))) {
                    add("Invalid action id: ${action.id}")
                }
                if (action.risk !in allowedRisks) {
                    add("Unsupported action risk: ${action.risk}")
                }
                if (action.risk != "READ_ONLY" && !action.requiresConfirmation) {
                    add("Non-read action must require confirmation: ${action.id}")
                }
            }
            if (manifest.actions.size > ConnectedAppFabricPolicy.MAX_ACTIONS_PER_APP) {
                add("Too many actions: ${manifest.id}")
            }
            if (manifest.privacyPolicyUrl != null && !manifest.privacyPolicyUrl.startsWith("https://")) {
                add("Privacy policy must use HTTPS")
            }
        }
        return ManifestValidation(errors.isEmpty(), errors)
    }
}

object ConnectedAppManifestCodec {
    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    fun encode(manifest: ConnectedAppManifest): String = json.encodeToString(manifest)

    fun decode(raw: String): ConnectedAppManifest {
        val manifest = json.decodeFromString<ConnectedAppManifest>(raw)
        val validation = ConnectedAppManifestValidator.validate(manifest)
        require(validation.valid) { validation.errors.joinToString("; ") }
        return manifest
    }
}

class ConnectedAppManifestRegistry(
    private val maxEntries: Int = ConnectedAppFabricPolicy.TARGET_CATALOG_CAPACITY
) {
    private val manifests = LinkedHashMap<String, ConnectedAppManifest>()

    @Synchronized
    fun register(manifest: ConnectedAppManifest) {
        val validation = ConnectedAppManifestValidator.validate(manifest)
        require(validation.valid) { validation.errors.joinToString("; ") }
        require(manifests.containsKey(manifest.id) || manifests.size < maxEntries) {
            "Connected App Fabric capacity reached"
        }
        manifests[manifest.id] = manifest
    }

    @Synchronized
    fun remove(id: String): Boolean = manifests.remove(id) != null

    @Synchronized
    fun find(id: String): ConnectedAppManifest? = manifests[id]

    @Synchronized
    fun search(query: String): List<ConnectedAppManifest> {
        if (query.isBlank()) return manifests.values.take(ConnectedAppFabricPolicy.MAX_SEARCH_RESULTS)
        return manifests.values
            .filter {
                it.id.contains(query, true) ||
                    it.name.contains(query, true) ||
                    it.category.contains(query, true) ||
                    it.actions.any { action -> action.id.contains(query, true) || action.description.contains(query, true) }
            }
            .take(ConnectedAppFabricPolicy.MAX_SEARCH_RESULTS)
    }

    @Synchronized
    fun size(): Int = manifests.size
}
