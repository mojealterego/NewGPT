package com.mojealterego.newgpt.domain.builder

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AppFactorySpec(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val platform: String = "android",
    val features: List<String> = emptyList(),
    val screens: List<String> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class AppFactoryArtifact(
    val path: String,
    val content: String
)

@Singleton
class AppFactoryEngine @Inject constructor() {
    private val json = Json { prettyPrint = true }

    fun plan(spec: AppFactorySpec): List<AppFactoryArtifact> {
        require(spec.name.isNotBlank())
        require(spec.packageName.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+")))
        return listOf(
            AppFactoryArtifact("app-factory/spec.json", json.encodeToString(spec)),
            AppFactoryArtifact("app-factory/BUILD_PLAN.md", buildPlan(spec))
        )
    }

    private fun buildPlan(spec: AppFactorySpec): String = buildString {
        appendLine("# " + spec.name)
        appendLine()
        appendLine("## Platform")
        appendLine(spec.platform)
        appendLine()
        appendLine("## Package")
        appendLine(spec.packageName)
        appendLine()
        appendLine("## Screens")
        spec.screens.forEach { appendLine("- " + it) }
        appendLine()
        appendLine("## Features")
        spec.features.forEach { appendLine("- " + it) }
        appendLine()
        appendLine("## Quality gates")
        appendLine("- compile")
        appendLine("- unit tests")
        appendLine("- installable artifact")
        appendLine("- regression verification")
    }
}
