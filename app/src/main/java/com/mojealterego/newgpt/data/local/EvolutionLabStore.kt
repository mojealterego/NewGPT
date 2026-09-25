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
data class EvolutionProposal(
    val id: String,
    val title: String,
    val rationale: String,
    val change: String,
    val evidence: String = "",
    val status: String = "PROPOSED",
    val score: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class EvolutionState(
    val dgmEnabled: Boolean = false,
    val rsiEnabled: Boolean = false,
    val humanApprovalRequired: Boolean = true,
    val sandboxOnly: Boolean = true,
    val proposals: List<EvolutionProposal> = emptyList()
)

@Singleton
class EvolutionLabStore @Inject constructor(@ApplicationContext context: Context) {
    private val file = File(context.filesDir, "evolution/evolution.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private fun load(): EvolutionState =
        if (!file.exists()) EvolutionState()
        else runCatching { json.decodeFromString<EvolutionState>(file.readText()) }.getOrDefault(EvolutionState())

    private fun save(state: EvolutionState) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(state))
    }

    suspend fun state(): EvolutionState = withContext(Dispatchers.IO) { load() }

    suspend fun updateFlags(dgm: Boolean, rsi: Boolean, approval: Boolean, sandbox: Boolean) =
        withContext(Dispatchers.IO) {
            save(load().copy(dgmEnabled = dgm, rsiEnabled = rsi, humanApprovalRequired = approval, sandboxOnly = sandbox))
        }

    suspend fun propose(title: String, rationale: String, change: String): EvolutionProposal =
        withContext(Dispatchers.IO) {
            val proposal = EvolutionProposal(UUID.randomUUID().toString(), title, rationale, change)
            val current = load()
            save(current.copy(proposals = (current.proposals + proposal).takeLast(100)))
            proposal
        }

    suspend fun evaluate(id: String, score: Float, keep: Boolean): Unit = withContext(Dispatchers.IO) {
        val current = load()
        save(current.copy(proposals = current.proposals.map { proposal ->
            if (proposal.id == id) proposal.copy(score = score.coerceIn(0f, 1f), status = if (keep) "KEPT" else "REJECTED") else proposal
        }))
    }
}
