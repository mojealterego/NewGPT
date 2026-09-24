package com.mojealterego.newgpt.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RagDocument(
    val id: String,
    val name: String,
    val text: String,
    val addedAt: Long
)

@Singleton
class LocalRagStore @Inject constructor(private val context: Context) {
    private val file get() = File(context.filesDir, "rag/documents.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private fun load(): MutableList<RagDocument> {
        if (!file.exists()) return mutableListOf()
        return runCatching { json.decodeFromString<List<RagDocument>>(file.readText()).toMutableList() }
            .getOrDefault(mutableListOf())
    }

    private fun save(items: List<RagDocument>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items))
    }

    suspend fun importUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val name = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "document" } ?: "document"
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Nie można odczytać pliku.")
        val clean = text.take(2_000_000)
        val item = RagDocument(java.util.UUID.randomUUID().toString(), name, clean, System.currentTimeMillis())
        save(load().apply { add(item) })
        name
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) { load().size }

    suspend fun clear() = withContext(Dispatchers.IO) { if (file.exists()) file.delete() }

    suspend fun retrieve(query: String, topK: Int): List<RagDocument> = withContext(Dispatchers.IO) {
        val terms = query.lowercase(Locale.ROOT)
            .split(Regex("""[^p{L}p{Nd}]+"""))
            .filter { it.length >= 3 }
            .distinct()
        if (terms.isEmpty()) return@withContext emptyList()
        load().map { doc ->
            val haystack = doc.text.lowercase(Locale.ROOT)
            val score = terms.sumOf { term ->
                minOf(Regex(Regex.escape(term)).findAll(haystack).count(), 8)
            }
            doc to score
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(topK.coerceIn(1, 12))
            .map { it.first.copy(text = it.first.text.take(6000)) }
    }
}
