package com.mojealterego.newgpt.runtime

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AbsoluteMaximumRuntimeTest {
    @Test fun backoffIsBounded() {
        val b = ExponentialBackoff(baseMs = 100, maxMs = 800)
        repeat(8) { assertTrue(b.delayMs(it) <= 800) }
    }
    @Test fun privateModeBlocksExternalTools() {
        val d = ZeroTrustPolicy().evaluate(ToolRequest("web", Risk.EXTERNAL, "x"), true)
        assertFalse(d.allowed)
    }
    @Test fun driftDetectorFindsOrthogonalVectors() {
        val detector = ConceptDriftDetector(0.75)
        assertTrue(detector.detect(detector.cosineDistance(listOf(1.0,0.0), listOf(0.0,1.0))).detected)
    }
    @Test fun alignmentRejectsPolicyHashMutation() {
        val policy = AlignmentPolicy("abc", setOf("human-control"), setOf("disable-audit"))
        assertFalse(ProvableAlignmentGate().verify(policy, MutationProposal("m1", setOf("refactor"), "def")).allowed)
    }
    @Test fun humilityRequestsHumanForHighImpactUncertainDecision() {
        assertTrue(EpistemicHumilityLoop().check(0.70, emptyList(), listOf("assumption"), true).humanReviewRequired)
    }
    @Test fun substratePlannerRanksCandidates() {
        val result = PhysicalSubstratePlanner().rank(listOf(
            SubstrateCandidate("silicon","baseline",1.0,1.0,0.95),
            SubstrateCandidate("graphene","experimental",2.0,0.5,0.4)
        ))
        assertEquals("graphene", result.first().material)
    }
    @Test fun quantumFallbackIsDeterministic() = runBlocking {
        assertEquals(listOf("a","b"), QuantumHybridCoordinator().optimize("search", mapOf("b" to 2.0,"a" to 1.0)).keys.toList())
    }
}
