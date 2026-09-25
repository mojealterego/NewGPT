package com.mojealterego.newgpt.domain.cognitive

import javax.inject.Inject
import javax.inject.Singleton

data class LocalModelDescriptor(
    val id: String,
    val displayName: String,
    val path: String,
    val contextTokens: Int,
    val parameterBillions: Float?,
    val quantization: String?,
    val gpuLayers: Int,
    val capabilities: Set<String>
)

@Singleton
class LocalModelRegistry @Inject constructor() {
    private val models = LinkedHashMap<String, LocalModelDescriptor>()

    @Synchronized
    fun register(model: LocalModelDescriptor) {
        require(model.id.isNotBlank())
        require(model.path.isNotBlank())
        models[model.id] = model
    }

    @Synchronized
    fun remove(id: String) {
        models.remove(id)
    }

    @Synchronized
    fun list(): List<LocalModelDescriptor> = models.values.toList()

    @Synchronized
    fun bestFor(capability: String): LocalModelDescriptor? =
        models.values
            .filter { capability in it.capabilities }
            .maxByOrNull { it.contextTokens }
}
