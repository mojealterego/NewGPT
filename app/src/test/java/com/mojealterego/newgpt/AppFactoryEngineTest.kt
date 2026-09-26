package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.builder.AppFactoryEngine
import com.mojealterego.newgpt.domain.builder.AppFactorySpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFactoryEngineTest {
    @Test
    fun producesVersionedBuildArtifacts() {
        val result = AppFactoryEngine().plan(
            AppFactorySpec(
                name = "Demo",
                packageName = "com.example.demo",
                features = listOf("chat"),
                screens = listOf("home")
            )
        )
        assertEquals(2, result.size)
        assertTrue(result.any { it.path.endsWith("spec.json") })
        assertTrue(result.any { it.content.contains("Quality gates") })
    }
}
