package com.mojealterego.newgpt.domain.nexus

data class Workspace(
    val id: String,
    val name: String,
    val description: String = "",
    val agentIds: List<String> = emptyList(),
    val modelIds: List<String> = emptyList(),
    val workflowIds: List<String> = emptyList(),
    val knowledgeSourceIds: List<String> = emptyList(),
    val privateMode: Boolean = false
)

class WorkspaceRegistry {
    private val workspaces = linkedMapOf<String, Workspace>()

    fun upsert(workspace: Workspace) {
        require(workspace.id.isNotBlank())
        require(workspace.name.isNotBlank())
        workspaces[workspace.id] = workspace
    }

    fun get(id: String): Workspace? = workspaces[id]
    fun all(): List<Workspace> = workspaces.values.toList()
    fun delete(id: String) { workspaces.remove(id) }
}
