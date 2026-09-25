package com.mojealterego.newgpt.data.local

import kotlin.math.sqrt

interface EmbeddingEngine {
    fun embed(text: String): FloatArray
}

/** Offline deterministic fallback. It is a pluggable fallback, not a neural embedding model. */
class HashEmbeddingEngine(private val dimensions: Int = 384) : EmbeddingEngine {
    override fun embed(text: String): FloatArray {
        val vector = FloatArray(dimensions)
        val tokens = text.lowercase()
            .split(Regex("""[^\p{L}\p{Nd}]+"""))
            .filter { it.length >= 2 }
        if (tokens.isEmpty()) return vector
        tokens.forEach { token ->
            val hash = token.hashCode()
            val index = (hash and Int.MAX_VALUE) % dimensions
            val sign = if ((hash and 1) == 0) 1f else -1f
            vector[index] += sign
            if (token.length > 5) {
                val index2 = ((hash * 31) and Int.MAX_VALUE) % dimensions
                vector[index2] += sign * 0.5f
            }
        }
        return normalize(vector)
    }

    companion object {
        fun normalize(input: FloatArray): FloatArray {
            val norm = sqrt(input.fold(0.0) { acc, value -> acc + value * value }).toFloat()
            if (norm <= 1e-7f) return input
            return FloatArray(input.size) { index -> input[index] / norm }
        }

        fun cosine(a: FloatArray, b: FloatArray): Float {
            if (a.isEmpty() || b.isEmpty() || a.size != b.size) return 0f
            var dot = 0f
            var na = 0f
            var nb = 0f
            for (i in a.indices) {
                dot += a[i] * b[i]
                na += a[i] * a[i]
                nb += b[i] * b[i]
            }
            if (na <= 1e-7f || nb <= 1e-7f) return 0f
            return (dot / (sqrt(na) * sqrt(nb))).coerceIn(-1f, 1f)
        }
    }
}
