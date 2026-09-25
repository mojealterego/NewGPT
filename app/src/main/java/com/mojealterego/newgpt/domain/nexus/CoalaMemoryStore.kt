package com.mojealterego.newgpt.domain.nexus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
enum class CoalaMemoryType { WORKING, EPISODIC, SEMANTIC, PROCEDURAL }

@Serializable
data class CoalaMemory(
    val id: String,
    val type: CoalaMemoryType,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = createdAt,
    val importance: Float = 0.5f
)

class CoalaMemoryStore(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): List<CoalaMemory> =
        if (!file.exists()) emptyList()
        else runCatching {
            json.decodeFromString<List<CoalaMemory>>(file.readText())
        }.getOrDefault(emptyList())

    private fun save(items: List<CoalaMemory>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items.takeLast(4000)))
    }

    suspend fun remember(
        type: CoalaMemoryType,
        content: String,
        tags: List<String> = emptyList(),
        importance: Float = 0.5f
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val item = CoalaMemory(
            id = "coala:" + UUID.randomUUID(),
            type = type,
            content = content.trim().take(12000),
            tags = tags.distinct().take(24),
            createdAt = now,
            lastUsedAt = now,
            importance = importance.coerceIn(0f, 1f)
        )
        save(load() + item)
        item.id
    }

    suspend fun retrieve(
        query: String,
        type: CoalaMemoryType? = null,
        limit: Int = 8
    ): List<CoalaMemory> = withContext(Dispatchers.IO) {
        val terms = query.lowercase()
            .split(Regex("[^A-Za-z0-9_]+"))
            .filter { it.length >= 3 }
            .toSet()
        load().asSequence()
            .filter { type == null || it.type == type }
            .map { item ->
                val haystack = item.content.lowercase()
                    .split(Regex("[^A-Za-z0-9_]+"))
                val score = terms.sumOf { term ->
                    haystack.count { it == term }.coerceAtMost(6)
                } + (item.importance * 2f).toInt()
                item to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit.coerceIn(1, 50))
            .map { it.first.copy(lastUsedAt = System.currentTimeMillis()) }
            .toList()
    }

    suspend fun all(): List<CoalaMemory> = withContext(Dispatchers.IO) { load() }
}
