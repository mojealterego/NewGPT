package com.mojealterego.newgpt.domain.cognitive

import java.util.Locale
import kotlin.math.ln
import javax.inject.Inject
import javax.inject.Singleton

data class SemanticHit(
    val id: String,
    val text: String,
    val score: Float,
    val matchedTerms: List<String>
)

@Singleton
class SemanticIndex @Inject constructor() {
    private data class Entry(val id: String, val text: String, val terms: List<String>)
    private val entries = LinkedHashMap<String, Entry>()

    @Synchronized
    fun upsert(id: String, text: String) {
        val clean = text.trim().take(30_000)
        if (clean.isBlank()) return
        entries[id] = Entry(id, clean, terms(clean))
        if (entries.size > 5000) entries.remove(entries.keys.first())
    }

    @Synchronized
    fun remove(id: String) { entries.remove(id) }

    @Synchronized
    fun search(query: String, topK: Int = 8): List<SemanticHit> {
        val q = terms(query).toSet()
        if (q.isEmpty()) return emptyList()
        val df = q.associateWith { term -> entries.values.count { term in it.terms } }
        return entries.values.mapNotNull { entry ->
            val matched = q.filter { it in entry.terms }
            if (matched.isEmpty()) return@mapNotNull null
            val score = matched.sumOf { term ->
                val tf = entry.terms.count { it == term }.toDouble()
                tf * (ln((entries.size + 1.0) / (df.getValue(term) + 1.0)) + 1.0)
            }.toFloat()
            SemanticHit(entry.id, entry.text, score, matched)
        }.sortedByDescending { it.score }.take(topK.coerceIn(1, 50))
    }

    private fun terms(text: String): List<String> =
        text.lowercase(Locale.ROOT)
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .filter { it.length >= 3 }
            .distinct()
}
