package com.mojealterego.newgpt.domain.nexus

data class ModelDescriptor(
    val id: String,
    val provider: String,
    val family: String,
    val local: Boolean,
    val multimodal: Set<MediaKind>,
    val contextTokens: Int,
    val quantization: String? = null,
    val artifactSha256: String? = null,
    val license: String? = null,
    val enabled: Boolean = true
)

class ModelCatalog {
    private val models = linkedMapOf<String, ModelDescriptor>()

    fun register(model: ModelDescriptor) {
        require(model.id.isNotBlank())
        require(model.contextTokens > 0)
        models[model.id] = model
    }

    fun all(): List<ModelDescriptor> = models.values.toList()

    fun find(query: String): List<ModelDescriptor> =
        models.values.filter {
            it.id.contains(query, true) ||
                it.provider.contains(query, true) ||
                it.family.contains(query, true)
        }

    fun compatible(kind: MediaKind): List<ModelDescriptor> =
        models.values.filter { it.enabled && kind in it.multimodal }

    fun verifyArtifact(id: String, sha256: String): Boolean =
        models[id]?.artifactSha256?.equals(sha256, ignoreCase = true) == true
}
