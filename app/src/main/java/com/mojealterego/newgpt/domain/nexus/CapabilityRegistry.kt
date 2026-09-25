package com.mojealterego.newgpt.domain.nexus

data class CapabilityManifest(
    val id: String,
    val category: String,
    val description: String,
    val requiresConfirmation: Boolean = true,
    val risk: Int = 0,
    val permissions: Set<String> = emptySet()
)

class CapabilityRegistry {
    private val items = linkedMapOf<String, CapabilityManifest>()

    fun register(item: CapabilityManifest) {
        items[item.id] = item
    }

    fun all(): List<CapabilityManifest> = items.values.toList()

    fun find(query: String): List<CapabilityManifest> = items.values.filter {
        it.id.contains(query, true) || it.description.contains(query, true) || it.category.contains(query, true)
    }
}
