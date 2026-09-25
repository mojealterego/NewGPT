package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.cognitive.DigitalGenotypeEngine
import com.mojealterego.newgpt.domain.cognitive.EvolutionEngine
import com.mojealterego.newgpt.domain.cognitive.MutationStatus
import com.mojealterego.newgpt.domain.cognitive.AgentGenotype
import org.junit.Assert.*
import org.junit.Test

class EvolutionEngineTest {
    @Test fun acceptedMutationRequiresTestsAndNoRegression() {
        val engine = EvolutionEngine()
        val candidate = engine.proposeMutation(
            "agent",
            mapOf("prompt" to "v2"),
            "improve verification"
        )
        assertEquals(MutationStatus.ACCEPTED, engine.evaluate(candidate, true, true).status)
        assertEquals(MutationStatus.REJECTED, engine.evaluate(candidate, false, true).status)
        assertEquals(MutationStatus.REJECTED, engine.evaluate(candidate, true, false).status)
    }

    @Test fun genotypeFingerprintChangesAcrossMutation() {
        val engine = DigitalGenotypeEngine()
        val first = AgentGenotype("coder", 1, mapOf("mode" to "safe"))
        val second = engine.mutate(first, mapOf("mode" to "deep"))
        assertNotEquals(engine.fingerprint(first), engine.fingerprint(second))
        assertEquals(2, second.version)
    }
}
