package com.mojealterego.newgpt.data.local

import android.content.Context
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
data class MemoryNode(
    val id: String,
    val label: String,
    val type: String,
    val weight: Float = 1f,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class MemoryEdge(
    val from: String,
    val to: String,
    val relation: String,
    val weight: Float = 1f
)

@Serializable
data class MemoryGraph(
    val nodes: List<MemoryNode> = emptyList(),
    val edges: List<MemoryEdge> = emptyList()
)

@Serializable
data class MemoryRecord(
    val id: String,
    val kind: String,
    val text: String,
    val createdAt: Long
)

@Singleton
class MemoryGraphStore @Inject constructor(@ApplicationContext context: Context) {
    private val file = File(context.filesDir, "memory/memory_graph.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private fun load(): Pair<MemoryGraph, List<MemoryRecord>> {
        if (!file.exists()) return MemoryGraph() to emptyList()
        return runCatching {
            val payload = json.decodeFromString<MemoryPayload>(file.readText())
            payload.graph to payload.records
        }.getOrDefault(MemoryGraph() to emptyList())
    }

    private fun save(graph: MemoryGraph, records: List<MemoryRecord>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(MemoryPayload(graph, records.takeLast(500))))
    }

    suspend fun rememberWorking(text: String) = remember("working", text)

    suspend fun rememberPermanent(text: String) = remember("permanent", text)

    suspend fun remember(kind: String, text: String) = withContext(Dispatchers.IO) {
        val clean = text.trim().take(12000)
        if (clean.isBlank()) return@withContext
        val (oldGraph, oldRecords) = load()
        val now = System.currentTimeMillis()
        val record = MemoryRecord(UUID.randomUUID().toString(), kind, clean, now)
        val terms = terms(clean).take(24)
        val nodes = oldGraph.nodes.toMutableList()
        val edges = oldGraph.edges.toMutableList()
        val memoryId = "memory:" + record.id
        nodes += MemoryNode(memoryId, clean.take(72), kind, if (kind == "permanent") 2f else 1f, now)
        val termIds = terms.map { term ->
            val id = "concept:" + term
            if (nodes.none { node -> node.id == id }) nodes += MemoryNode(id, term, "concept", 1f, now)
            id
        }
        termIds.forEach { conceptId ->
            edges += MemoryEdge(memoryId, conceptId, "associated_with", 1f)
        }
        termIds.zipWithNext().forEach { pair ->
            edges += MemoryEdge(pair.first, pair.second, "co_occurs", 0.5f)
        }
        val compactEdges = edges.distinctBy { edge -> edge.from + "|" + edge.to + "|" + edge.relation }.takeLast(2500)
        save(MemoryGraph(nodes.distinctBy { node -> node.id }.takeLast(1200), compactEdges), oldRecords + record)
    }

    suspend fun retrieve(query: String, limit: Int = 5): List<MemoryRecord> = withContext(Dispatchers.IO) {
        val (_, records) = load()
        val terms = terms(query).toSet()
        records.asSequence()
            .map { record ->
                val score = terms.sumOf { term ->
                    Regex(Regex.escape(term), RegexOption.IGNORE_CASE).findAll(record.text).count().coerceAtMost(6)
                } + if (record.kind == "permanent") 1 else 0
                record to score
            }
            .filter { pair -> pair.second > 0 }
            .sortedByDescending { pair -> pair.second }
            .take(limit.coerceIn(1, 12))
            .map { pair -> pair.first }
            .toList()
    }

    suspend fun graph(): MemoryGraph = withContext(Dispatchers.IO) { load().first }

    suspend fun clearWorking() = withContext(Dispatchers.IO) {
        val (graph, records) = load()
        val ids = records.filter { record -> record.kind == "working" }.map { record -> "memory:" + record.id }.toSet()
        save(
            graph.copy(
                nodes = graph.nodes.filterNot { node -> node.id in ids },
                edges = graph.edges.filterNot { edge -> edge.from in ids }
            ),
            records.filter { record -> record.kind != "working" }
        )
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        if (file.exists()) file.delete()
    }

    private fun terms(text: String): List<String> =
        text.lowercase(Locale.ROOT)
            .split(Regex("""[^p{L}p{Nd}]+"""))
            .filter { term -> term.length >= 4 }
            .distinct()
            .filterNot { term -> term in STOPWORDS }

    @Serializable
    private data class MemoryPayload(
        val graph: MemoryGraph,
        val records: List<MemoryRecord>
    )

    companion object {
        private val STOPWORDS = setOf(
            "jest","jestem","jako","który","która","które","oraz","albo","tego","taka","takie",
            "this","that","with","from","have","your","about","into","then","when","where"
        )
    }
}
