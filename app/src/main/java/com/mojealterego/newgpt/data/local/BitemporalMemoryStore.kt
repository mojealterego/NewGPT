package com.mojealterego.newgpt.data.local

import android.content.Context
import com.mojealterego.newgpt.domain.cognitive.BitemporalInterval
import com.mojealterego.newgpt.domain.cognitive.CognitiveMemoryItem
import com.mojealterego.newgpt.domain.cognitive.MemoryKind
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitemporalMemoryStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val file = File(context.filesDir, "cognitive/memory_v2.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    suspend fun remember(
        content: String,
        kind: MemoryKind,
        source: String,
        validAt: Long = System.currentTimeMillis(),
        confidence: Float = 0.5f,
        importance: Float = 0.5f,
        provenance: List<String> = emptyList()
    ): CognitiveMemoryItem = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val item = CognitiveMemoryItem(
            id = UUID.randomUUID().toString(),
            kind = kind,
            content = content.trim().take(32_000),
            source = source,
            confidence = confidence.coerceIn(0f, 1f),
            interval = BitemporalInterval(
                validFrom = validAt,
                recordedFrom = now
            ),
            concepts = extractConcepts(content),
            provenance = provenance.take(32),
            importance = importance.coerceIn(0f, 1f)
        )
        val state = load()
        save((state + item).takeLast(5000))
        item
    }

    suspend fun query(
        query: String,
        atValidTime: Long? = null,
        atRecordedTime: Long? = null,
        limit: Int = 12
    ): List<CognitiveMemoryItem> = withContext(Dispatchers.IO) {
        val terms = extractConcepts(query).toSet()
        val validAt = atValidTime ?: Long.MAX_VALUE
        val recordedAt = atRecordedTime ?: Long.MAX_VALUE

        load().asSequence()
            .filter { item ->
                item.interval.validFrom <= validAt &&
                    (item.interval.validTo == null || validAt < item.interval.validTo!!) &&
                    item.interval.recordedFrom <= recordedAt &&
                    (item.interval.recordedTo == null || recordedAt < item.interval.recordedTo!!)
            }
            .map { item ->
                val overlap = terms.count { term -> term in item.concepts }
                val lexical = if (query.isNotBlank() && item.content.contains(query, ignoreCase = true)) 3 else 0
                item to (overlap * 2 + lexical + item.importance)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit.coerceIn(1, 50))
            .map { it.first }
            .toList()
    }

    suspend fun pointInTime(at: Long): List<CognitiveMemoryItem> =
        query("", atValidTime = at, atRecordedTime = at, limit = 5000)

    suspend fun closeFact(id: String, validTo: Long, recordedTo: Long = System.currentTimeMillis()) =
        withContext(Dispatchers.IO) {
            val updated = load().map { item ->
                if (item.id == id) item.copy(
                    interval = item.interval.copy(validTo = validTo, recordedTo = recordedTo)
                ) else item
            }
            save(updated)
        }

    suspend fun all(): List<CognitiveMemoryItem> = withContext(Dispatchers.IO) { load() }

    private fun load(): List<CognitiveMemoryItem> {
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<MemoryState>(file.readText()).items
        }.getOrDefault(emptyList())
    }

    private fun save(items: List<CognitiveMemoryItem>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(MemoryState(items)))
    }

    private fun extractConcepts(text: String): List<String> =
        text.lowercase(Locale.ROOT)
            .split(Regex("""[^\p{L}\p{Nd}]+"""))
            .filter { it.length >= 4 }
            .distinct()
            .take(48)

    @Serializable
    private data class MemoryState(val items: List<CognitiveMemoryItem> = emptyList())
}
