package com.mojealterego.newgpt.domain.cognitive

import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AuditEvent(
    val id: String = UUID.randomUUID().toString(),
    val executionId: String,
    val actor: String,
    val action: String,
    val capability: String? = null,
    val decision: String,
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class ExecutionAudit @Inject constructor() {
    private val events = ArrayDeque<AuditEvent>()

    @Synchronized
    fun record(event: AuditEvent) {
        events.addLast(event)
        while (events.size > 2000) events.removeFirst()
    }

    @Synchronized
    fun recent(limit: Int = 100): List<AuditEvent> =
        events.takeLast(limit.coerceIn(1, 500))
}
