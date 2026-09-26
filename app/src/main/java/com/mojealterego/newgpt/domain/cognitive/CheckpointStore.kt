package com.mojealterego.newgpt.domain.cognitive

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExecutionCheckpoint(
    val executionId: String,
    val stage: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Singleton
class CheckpointStore @Inject constructor() {
    private val checkpoints = ConcurrentHashMap<String, ExecutionCheckpoint>()
    fun save(checkpoint: ExecutionCheckpoint) { checkpoints[checkpoint.executionId] = checkpoint }
    fun load(executionId: String): ExecutionCheckpoint? = checkpoints[executionId]
    fun clear(executionId: String) { checkpoints.remove(executionId) }
}
