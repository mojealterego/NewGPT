package com.mojealterego.newgpt.domain.nexus

/**
 * Named adapter boundary for ImandraX-style formal verification.
 * The Android client does not embed a formal prover; configure a backend
 * implementation when a verified expression service is available.
 */
class ImandraXAdapter(
    private val delegate: FormalVerificationAdapter = UnconfiguredFormalVerificationAdapter()
) : FormalVerificationAdapter {
    override suspend fun verify(expression: String): VerificationResult =
        delegate.verify(expression)
}
