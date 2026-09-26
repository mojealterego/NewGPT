package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.cognitive.SemanticIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticIndexTest {
    @Test
    fun ranksMatchingKnowledge() {
        val index = SemanticIndex()
        index.upsert("one", "Kotlin Android agent runtime")
        index.upsert("two", "Photography and books")
        val hits = index.search("Android agent")
        assertEquals("one", hits.first().id)
        assertTrue(hits.first().score > 0f)
    }
}
