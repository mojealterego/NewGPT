package com.mojealterego.newgpt.domain.cognitive

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceLedger @Inject constructor() {
    private val items = ArrayDeque<EvidenceItem>()

    @Synchronized
    fun record(
        type: EvidenceType,
        statement: String,
        source: String? = null,
        confidence: Float = 0.5f
    ): EvidenceItem {
        val item = EvidenceItem(
            id = UUID.randomUUID().toString(),
            type = type,
            statement = statement.trim().take(8000),
            source = source,
            confidence = confidence.coerceIn(0f, 1f)
        )
        items.addLast(item)
        while (items.size > 2000) items.removeFirst()
        return item
    }

    @Synchronized
    fun snapshot(): List<EvidenceItem> = items.toList()

    @Synchronized
    fun clear() = items.clear()
}
