package com.mojealterego.newgpt.domain.agent

object AgentGraphValidator {
    data class Result(val valid: Boolean, val errors: List<String>)

    fun validate(agent: AgentDefinition, all: List<AgentDefinition>): Result {
        val errors = buildList {
            if (agent.id.isBlank()) add("ID agenta nie może być pusty.")
            if (agent.name.isBlank()) add("Nazwa agenta nie może być pusta.")
            if (agent.systemPrompt.isBlank()) add("System prompt nie może być pusty.")
            if (agent.handoffs.any { it == agent.id }) add("Agent nie może przekazywać zadania sam do siebie.")
            val ids = all.map { it.id }.toSet()
            agent.handoffs.filterNot { it in ids }.forEach { add("Nieznany handoff: $it") }
            if (all.count { it.id == agent.id } > 1) add("ID agenta musi być unikalne.")
        }
        return Result(errors.isEmpty(), errors)
    }

    fun validateGraph(all: List<AgentDefinition>): Result {
        val errors = mutableListOf<String>()
        val ids = all.map { it.id }
        if (ids.size != ids.toSet().size) errors += "Registry zawiera zduplikowane ID."

        val byId = all.associateBy { it.id }
        all.forEach { agent ->
            agent.handoffs.filterNot { it in byId }.forEach {
                errors += "${agent.name}: nieznany handoff '$it'."
            }
        }

        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        fun visit(id: String) {
            if (id in visiting) {
                errors += "Wykryto cykl handoffów przy '$id'."
                return
            }
            if (id in visited) return
            visiting += id
            byId[id]?.handoffs?.forEach(::visit)
            visiting -= id
            visited += id
        }

        all.forEach { visit(it.id) }
        return Result(errors.isEmpty(), errors.distinct())
    }
}
