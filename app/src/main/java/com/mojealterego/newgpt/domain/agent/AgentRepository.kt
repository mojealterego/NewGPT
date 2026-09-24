package com.mojealterego.newgpt.domain.agent

import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    val agents: Flow<List<AgentDefinition>>
    suspend fun get(id: String): AgentDefinition?
    suspend fun upsert(agent: AgentDefinition)
    suspend fun delete(id: String)
    suspend fun resetToDefaults()
}
