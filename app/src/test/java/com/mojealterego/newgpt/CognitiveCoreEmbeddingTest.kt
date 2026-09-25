package com.mojealterego.newgpt

import com.mojealterego.newgpt.data.local.HashEmbeddingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CognitiveCoreEmbeddingTest {
    @Test
    fun identical_text_is_deterministic_and_normalized() {
        val engine = HashEmbeddingEngine(384)
        val a = engine.embed("NowyGPT pamięć holograficzna")
        val b = engine.embed("NowyGPT pamięć holograficzna")
        assertEquals(384, a.size)
        assertEquals(a.toList(), b.toList())
        assertEquals(1f, HashEmbeddingEngine.cosine(a, b), 0.0001f)
    }

    @Test
    fun unrelated_text_has_lower_similarity_than_identical_text() {
        val engine = HashEmbeddingEngine(384)
        val a = engine.embed("fotografia uliczna Cieszyn")
        val b = engine.embed("private LTE 5G core")
        assertTrue(HashEmbeddingEngine.cosine(a, b) < 0.99f)
    }
}
