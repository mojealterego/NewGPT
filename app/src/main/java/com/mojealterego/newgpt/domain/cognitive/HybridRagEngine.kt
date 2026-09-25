package com.mojealterego.newgpt.domain.cognitive

import com.mojealterego.newgpt.data.local.BitemporalMemoryStore
import com.mojealterego.newgpt.data.local.LocalRagStore
import javax.inject.Inject
import javax.inject.Singleton

data class RagHit(
    val text: String,
    val source: String,
    val score: Float,
    val temporal: Boolean
)

@Singleton
class HybridRagEngine @Inject constructor(
    private val documents: LocalRagStore,
    private val memory: BitemporalMemoryStore
) {
    suspend fun search(
        query: String,
        temporalAt: Long? = null,
        topK: Int = 8
    ): List<RagHit> {
        val documentHits = documents.retrieve(query, topK)
            .mapIndexed { index, doc ->
                RagHit(
                    text = doc.text,
                    source = doc.name,
                    score = 1f / (index + 1),
                    temporal = false
                )
            }

        val memoryHits = memory.query(
            query = query,
            atValidTime = temporalAt,
            atRecordedTime = temporalAt,
            limit = topK
        ).map { item ->
            RagHit(
                text = item.content,
                source = item.source,
                score = 1.25f * item.confidence + 0.75f * item.importance,
                temporal = temporalAt != null
            )
        }

        return (documentHits + memoryHits)
            .sortedByDescending { it.score }
            .distinctBy { it.source + "|" + it.text.take(160) }
            .take(topK.coerceIn(1, 20))
    }
}
