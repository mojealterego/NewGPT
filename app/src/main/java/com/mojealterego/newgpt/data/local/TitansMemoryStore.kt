package com.mojealterego.newgpt.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Serializable
data class TitansMemory(
    val id: String,
    val key: List<Float>,
    val value: String,
    val surprise: Float,
    val importance: Float,
    val usage: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/** Titans-inspired application memory gate; not the Titans neural architecture itself. */
@Singleton
class TitansMemoryStore @Inject constructor(@ApplicationContext context: Context) {
    private val file = File(context.filesDir, "memory/titans.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val embedding = HashEmbeddingEngine()

    private fun load(): List<TitansMemory> =
        if (!file.exists()) emptyList()
        else runCatching { json.decodeFromString<List<TitansMemory>>(file.readText()) }.getOrDefault(emptyList())

    private fun save(items: List<TitansMemory>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items.takeLast(1000)))
    }

    suspend fun observe(text: String): Float = withContext(Dispatchers.IO) {
        val key = embedding.embed(text)
        val nearest = load().maxOfOrNull { HashEmbeddingEngine.cosine(key, it.key.toFloatArray()) } ?: 0f
        (1f - ((nearest + 1f) / 2f)).coerceIn(0f, 1f)
    }

    suspend fun consolidate(text: String, importance: Float = 0.5f): Boolean = withContext(Dispatchers.IO) {
        val clean = text.trim()
        if (clean.isBlank()) return@withContext false
        val key = embedding.embed(clean)
        val current = load().toMutableList()
        val nearest = current.maxOfOrNull { HashEmbeddingEngine.cosine(key, it.key.toFloatArray()) } ?: 0f
        val surprise = (1f - ((nearest + 1f) / 2f)).coerceIn(0f, 1f)
        val gate = max(importance.coerceIn(0f, 1f), surprise)
        if (gate < 0.25f) return@withContext false
        current += TitansMemory(
            id = "titans:" + UUID.randomUUID(),
            key = key.toList(),
            value = clean.take(12000),
            surprise = surprise,
            importance = importance.coerceIn(0f, 1f)
        )
        save(current)
        true
    }

    suspend fun retrieve(query: String, limit: Int = 5): List<TitansMemory> = withContext(Dispatchers.IO) {
        val key = embedding.embed(query)
        val current = load().toMutableList()
        val ranked = current
            .map { it to HashEmbeddingEngine.cosine(key, it.key.toFloatArray()) }
            .sortedByDescending { it.second + it.first.importance * 0.2f + it.first.surprise * 0.1f }
            .take(limit.coerceIn(1, 20))
        if (ranked.isNotEmpty()) {
            val used = ranked.map { it.first.id }.toSet()
            val updated = current.map { item ->
                if (item.id in used) item.copy(usage = item.usage + 1, updatedAt = System.currentTimeMillis())
                else item
            }
            save(updated.sortedByDescending { it.updatedAt }.takeLast(1000))
        }
        ranked.map { it.first }
    }

    suspend fun consolidateWithDecay(
        text: String,
        importance: Float = 0.5f,
        decayAfterMs: Long = 7L * 24L * 60L * 60L * 1000L
    ): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val decayed = load().map { item ->
            val age = (now - item.updatedAt).coerceAtLeast(0L)
            val factor = if (age <= decayAfterMs) 1f else 0.85f
            item.copy(
                importance = (item.importance * factor).coerceIn(0f, 1f),
                surprise = (item.surprise * factor).coerceIn(0f, 1f)
            )
        }.filter { it.importance >= 0.05f || it.surprise >= 0.05f }
        save(decayed)
        consolidate(text, importance)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        if (file.exists()) file.delete()
    }
}
