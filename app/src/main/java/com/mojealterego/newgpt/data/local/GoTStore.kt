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

@Serializable
data class GoTNode(
    val id: String,
    val type: String,
    val content: String,
    val score: Float = 0.5f,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class GoTEdge(
    val from: String,
    val to: String,
    val relation: String,
    val weight: Float = 1f
)

@Serializable
data class GoTGraph(
    val nodes: List<GoTNode> = emptyList(),
    val edges: List<GoTEdge> = emptyList()
)

/** Stores compact reasoning summaries and dependencies, not hidden chain-of-thought. */
@Singleton
class GoTStore @Inject constructor(@ApplicationContext context: Context) {
    private val file = File(context.filesDir, "got/graph.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private fun load(): GoTGraph =
        if (!file.exists()) GoTGraph()
        else runCatching { json.decodeFromString<GoTGraph>(file.readText()) }.getOrDefault(GoTGraph())

    private fun save(graph: GoTGraph) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(graph))
    }

    suspend fun append(type: String, content: String, score: Float = 0.5f, parentIds: List<String> = emptyList()): String =
        withContext(Dispatchers.IO) {
            val id = "got:" + UUID.randomUUID()
            val node = GoTNode(id, type, content.trim().take(8000), score.coerceIn(0f, 1f))
            val graph = load()
            val edges = graph.edges.toMutableList()
            parentIds.distinct().take(12).forEach { parent ->
                edges += GoTEdge(parent, id, "supports", score.coerceIn(0.1f, 1f))
            }
            save(
                GoTGraph(
                    nodes = (graph.nodes + node).takeLast(1500),
                    edges = edges.distinctBy { it.from + "|" + it.to + "|" + it.relation }.takeLast(3000)
                )
            )
            id
        }

    suspend fun recent(limit: Int = 100): GoTGraph = withContext(Dispatchers.IO) {
        val graph = load()
        val nodes = graph.nodes.takeLast(limit.coerceIn(1, 500))
        val ids = nodes.map { it.id }.toSet()
        graph.copy(nodes = nodes, edges = graph.edges.filter { it.from in ids && it.to in ids })
    }

    suspend fun appendStage(
        inputId: String,
        stage: String,
        content: String,
        score: Float = 0.5f
    ): String = append(stage, content, score, listOf(inputId))

    suspend fun clear() = withContext(Dispatchers.IO) {
        if (file.exists()) file.delete()
    }
}
