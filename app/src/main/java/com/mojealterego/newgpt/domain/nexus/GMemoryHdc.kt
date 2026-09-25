package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import kotlin.math.sqrt

@Serializable
data class GMemoryNode(
    val id: String,
    val layer: String,
    val label: String,
    val payload: String,
    val links: List<String> = emptyList(),
    val weight: Float = 1f
)

class GMemoryStore(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): List<GMemoryNode> =
        if (!file.exists()) emptyList()
        else runCatching {
            json.decodeFromString<List<GMemoryNode>>(file.readText())
        }.getOrDefault(emptyList())

    private fun save(items: List<GMemoryNode>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items.takeLast(6000)))
    }

    suspend fun add(
        layer: String,
        label: String,
        payload: String,
        links: List<String> = emptyList(),
        weight: Float = 1f
    ): String = withContext(Dispatchers.IO) {
        require(layer in setOf("insight", "query", "interaction"))
        val item = GMemoryNode(
            "gm:" + UUID.randomUUID(),
            layer,
            label.take(200),
            payload.take(12000),
            links.distinct().take(20),
            weight.coerceIn(0f, 1f)
        )
        save(load() + item)
        item.id
    }

    suspend fun traverse(seed: String, maxDepth: Int = 3): List<GMemoryNode> =
        withContext(Dispatchers.IO) {
            val nodes = load().associateBy { it.id }
            val result = LinkedHashSet<GMemoryNode>()
            var frontier = nodes.values
                .filter { it.id == seed || it.label.contains(seed, true) }
                .map { it.id }
                .toSet()

            repeat(maxDepth.coerceIn(1, 8)) {
                if (frontier.isEmpty()) return@repeat
                frontier.forEach { id -> nodes[id]?.let { result += it } }
                frontier = frontier
                    .flatMap { id -> nodes[id]?.links ?: emptyList() }
                    .filter { it in nodes }
                    .toSet()
            }
            result.toList()
        }

    suspend fun counts(): Map<String, Int> = withContext(Dispatchers.IO) {
        load().groupingBy { it.layer }.eachCount()
    }
}

class HdcMemory(private val dimensions: Int = 512) {
    private fun hash(text: String, index: Int): Long {
        var h = 1125899906842597L + index
        for (c in text) h = 31L * h + c.code
        return h
    }

    fun encode(text: String): FloatArray = FloatArray(dimensions) { i ->
        if (((hash(text.lowercase(), i) ushr 1) % 2L) == 0L) -1f else 1f
    }

    fun bind(a: FloatArray, b: FloatArray): FloatArray {
        require(a.size == b.size)
        return FloatArray(a.size) { i -> a[i] * b[i] }
    }

    fun bundle(vectors: List<FloatArray>): FloatArray {
        require(vectors.isNotEmpty())
        val out = FloatArray(vectors.first().size)
        vectors.forEach { vector ->
            require(vector.size == out.size)
            for (i in out.indices) out[i] += vector[i]
        }
        return FloatArray(out.size) { i -> if (out[i] >= 0f) 1f else -1f }
    }

    fun similarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size)
        var dot = 0f
        var aa = 0f
        var bb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            aa += a[i] * a[i]
            bb += b[i] * b[i]
        }
        return if (aa == 0f || bb == 0f) 0f
        else dot / sqrt(aa.toDouble() * bb.toDouble()).toFloat()
    }
}
