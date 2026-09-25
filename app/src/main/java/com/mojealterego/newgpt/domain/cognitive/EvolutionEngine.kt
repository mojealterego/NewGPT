package com.mojealterego.newgpt.domain.cognitive

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvolutionEngine @Inject constructor() {
    fun proposeMutation(
        parentId: String,
        genotype: Map<String, String>,
        hypothesis: String
    ): MutationCandidate = MutationCandidate(
        id = UUID.randomUUID().toString(),
        parentId = parentId,
        genotype = genotype.toMap(),
        hypothesis = hypothesis.trim().take(2000)
    )

    fun evaluate(candidate: MutationCandidate, testsPassed: Boolean, regressionFree: Boolean): MutationCandidate =
        candidate.copy(
            status = if (testsPassed && regressionFree) MutationStatus.ACCEPTED else MutationStatus.REJECTED
        )
}
