package com.mojealterego.newgpt.domain.cognitive

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class AgentGenotype(
    val agentId: String,
    val version: Int,
    val traits: Map<String, String>,
    val parentHash: String? = null
)

@Singleton
class DigitalGenotypeEngine @Inject constructor() {
    fun fingerprint(genotype: AgentGenotype): String {
        val canonical = buildString {
            append(genotype.agentId).append('|')
            append(genotype.version).append('|')
            genotype.traits.toSortedMap().forEach { (key, value) ->
                append(key).append('=').append(value).append(';')
            }
            append(genotype.parentHash.orEmpty())
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun mutate(
        parent: AgentGenotype,
        changes: Map<String, String>
    ): AgentGenotype = parent.copy(
        version = parent.version + 1,
        traits = parent.traits + changes,
        parentHash = fingerprint(parent)
    )
}
