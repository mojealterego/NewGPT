package com.mojealterego.newgpt.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mojealterego.newgpt.domain.agent.AgentDefinition
import com.mojealterego.newgpt.domain.agent.AgentRepository
import com.mojealterego.newgpt.domain.agent.defaultAgents
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.agentDataStore by preferencesDataStore(name = "newgpt_agents")

@Singleton
class AgentStore @Inject constructor(@ApplicationContext private val context: Context) : AgentRepository {
    private val definitionsKey = stringPreferencesKey("definitions")
    private val initializedKey = booleanPreferencesKey("initialized")
    private val json = Json { ignoreUnknownKeys = true }

    override val agents: Flow<List<AgentDefinition>> = context.agentDataStore.data.map { prefs ->
        if (prefs[initializedKey] != true) defaultAgents() else decode(prefs[definitionsKey])
    }

    override suspend fun get(id: String): AgentDefinition? =
        agents.first().firstOrNull { it.id == id }

    override suspend fun upsert(agent: AgentDefinition) {
        context.agentDataStore.edit { prefs ->
            val current = if (prefs[initializedKey] == true) decode(prefs[definitionsKey]) else defaultAgents()
            prefs[definitionsKey] = encode(current.filterNot { it.id == agent.id } + agent)
            prefs[initializedKey] = true
        }
    }

    override suspend fun delete(id: String) {
        context.agentDataStore.edit { prefs ->
            val current = if (prefs[initializedKey] == true) decode(prefs[definitionsKey]) else defaultAgents()
            prefs[definitionsKey] = encode(current.filterNot { it.id == id })
            prefs[initializedKey] = true
        }
    }

    override suspend fun resetToDefaults() {
        context.agentDataStore.edit { prefs ->
            prefs[definitionsKey] = encode(defaultAgents())
            prefs[initializedKey] = true
        }
    }

    private fun encode(value: List<AgentDefinition>): String =
        json.encodeToString(ListSerializer(AgentDefinition.serializer()), value)

    private fun decode(value: String?): List<AgentDefinition> =
        value?.let {
            runCatching {
                json.decodeFromString(ListSerializer(AgentDefinition.serializer()), it)
            }.getOrDefault(emptyList())
        } ?: emptyList()
}
