package com.mojealterego.newgpt.data.local

import android.content.Context
import android.net.Uri
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

@Serializable
data class RagDocument(
    val id: String,
    val name: String,
    val text: String,
    val addedAt: Long,
    val chunkIndex: Int = 0,
    val embedding: List<Float> = emptyList(),
    val sourceStart: Int = 0,
    val sourceEnd: Int = 0,
    val contentHash: Int = 0
)

@Singleton
class LocalRagStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val file get() = File(context.filesDir, "rag/documents.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val embeddingEngine: EmbeddingEngine = HashEmbeddingEngine(384)

    private fun load(): MutableList<RagDocument> {
        if (!file.exists()) return mutableListOf()
        return runCatching {
            json.decodeFromString<List<RagDocument>>(file.readText()).map { doc ->
                if (doc.embedding.isEmpty()) doc.copy(embedding = embeddingEngine.embed(doc.text).toList()) else doc
            }.toMutableList()
        }.getOrDefault(mutableListOf())
    }

    private fun save(items: List<RagDocument>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items.takeLast(5000)))
    }

    suspend fun importUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val name = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "document" } ?: "document"
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Nie można odczytać pliku.")
        val clean = text.take(2_000_000)
        val chunkSize = 1400
        val overlap = 180
        val chunks = buildList {
            var start = 0
            var index = 0
            while (start < clean.length) {
                val end = (start + chunkSize).coerceAtMost(clean.length)
                val chunk = clean.substring(start, end).trim()
                if (chunk.isNotBlank()) {
                    add(
                        RagDocument(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            text = chunk,
                            addedAt = System.currentTimeMillis(),
                            chunkIndex = index++,
                            embedding = embeddingEngine.embed(chunk).toList(),
                            sourceStart = start,
                            sourceEnd = end,
                            contentHash = chunk.hashCode()
                        )
                    )
                }
                if (end == clean.length) break
                start = (end - overlap).coerceAtLeast(start + 1)
            }
        }
        val existing = load()
        val keys = existing.map { it.name + "|" + it.chunkIndex + "|" + it.contentHash }.toHashSet()
        val fresh = chunks.filter { (it.name + "|" + it.chunkIndex + "|" + it.contentHash) !in keys }
        save(existing.apply { addAll(fresh) })
        name + " · " + chunks.size + " chunks"
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) { load().size }

    suspend fun clear() = withContext(Dispatchers.IO) { if (file.exists()) file.delete() }

    suspend fun retrieve(query: String, topK: Int): List<RagDocument> = withContext(Dispatchers.IO) {
        val queryTerms = query.lowercase(Locale.ROOT)
            .split(Regex("""[^\p{L}\p{Nd}]+"""))
            .filter { it.length >= 3 }
            .distinct()
        if (queryTerms.isEmpty()) return@withContext emptyList()

        val queryEmbedding = embeddingEngine.embed(query)
        load().map { doc ->
            val haystack = doc.text.lowercase(Locale.ROOT)
            val lexical = queryTerms.sumOf { term ->
                minOf(Regex(Regex.escape(term)).findAll(haystack).count(), 8)
            }.toFloat() / (queryTerms.size * 2f).coerceAtLeast(1f)
            val semantic = HashEmbeddingEngine.cosine(queryEmbedding, doc.embedding.toFloatArray())
            val exactPhrase = if (query.trim().length >= 6 && haystack.contains(query.trim().lowercase(Locale.ROOT))) 0.15f else 0f
            val semanticScore = ((semantic + 1f) / 2f).coerceIn(0f, 1f)
            val hybrid = semanticScore * 0.60f + lexical.coerceIn(0f, 1f) * 0.25f + exactPhrase
            doc to hybrid
        }
            .filter { it.second > 0.05f }
            .sortedByDescending { it.second }
            .take(topK.coerceIn(1, 20))
            .map { it.first.copy(text = it.first.text.take(7000)) }
    }
}
