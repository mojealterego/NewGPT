package com.mojealterego.newgpt.domain.nexus

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class NexusSnapshot(
    val id: String,
    val createdAt: Long,
    val label: String,
    val payload: String
)

/**
 * Local point-in-time recovery for Nexus state/configuration.
 * It is snapshot recovery, not database WAL/PITR infrastructure.
 */
class PitrSnapshotStore(context: Context) {
    private val directory = File(context.filesDir, "nexus/pitr")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun create(label: String, payload: String): NexusSnapshot =
        withContext(Dispatchers.IO) {
            directory.mkdirs()
            val snapshot = NexusSnapshot(
                id = "snapshot:" + UUID.randomUUID(),
                createdAt = System.currentTimeMillis(),
                label = label.take(160),
                payload = payload.take(2_000_000)
            )
            File(directory, snapshot.id + ".json").writeText(json.encodeToString(snapshot))
            snapshot
        }

    suspend fun list(): List<NexusSnapshot> = withContext(Dispatchers.IO) {
        directory.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    json.decodeFromString<NexusSnapshot>(file.readText())
                }.getOrNull()
            }
            ?.sortedByDescending { it.createdAt }
            .orEmpty()
    }

    suspend fun restore(snapshotId: String): NexusSnapshot? = withContext(Dispatchers.IO) {
        val file = File(directory, snapshotId + ".json")
        if (!file.exists()) null
        else runCatching {
            json.decodeFromString<NexusSnapshot>(file.readText())
        }.getOrNull()
    }
}
