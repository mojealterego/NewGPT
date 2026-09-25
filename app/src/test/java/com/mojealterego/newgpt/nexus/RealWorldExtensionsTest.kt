package com.mojealterego.newgpt.nexus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealWorldExtensionsTest {
    @Test
    fun constitutionalGovernanceRejectsForbiddenMutation() {
        val governance = ConstitutionalGovernance.create(
            1L,
            listOf(
                ConstitutionalRule(
                    "human",
                    "human control",
                    prohibitedActions = setOf("autonomous-production-deploy"),
                    requiredControls = setOf("human-approval")
                )
            )
        )
        val result = governance.verifyMutation(
            MutationCandidate(
                "m1",
                changedComponents = setOf("router"),
                requestedActions = setOf("autonomous-production-deploy"),
                expectedBenefits = setOf("human-approval")
            )
        )
        assertFalse(result.accepted)
        assertTrue(result.violations.any { it.contains("prohibited") })
    }

    @Test
    fun epistemicControllerRequiresEvidenceAndReversibility() {
        val controller = EpistemicHumilityController(0.8)
        assertFalse(controller.inspect(0.79, 3, emptyList(), true).actionAllowed)
        assertFalse(controller.inspect(0.95, 3, listOf("contradiction"), true).actionAllowed)
        assertTrue(controller.inspect(0.95, 3, emptyList(), true).actionAllowed)
    }

    @Test
    fun substrateDesignUsesRealDeviceTelemetryInputs() {
        val runtime = PhysicalSubstrateRuntime("https://lab.example.invalid/fabricate", "lab.example.invalid")
        val snapshot = DeviceComputeSnapshot(
            sdk = 36,
            abis = listOf("arm64-v8a"),
            cpuThreads = 8,
            totalRamBytes = 12L * 1024 * 1024 * 1024,
            availableRamBytes = 6L * 1024 * 1024 * 1024,
            lowRamDevice = false,
            batteryPercent = 80,
            batteryCurrentMicroAmps = -1000,
            batteryTemperatureC = 32.0,
            thermalStatus = 0,
            powerSaveMode = false
        )
        val design = runtime.designFromTelemetry(snapshot, 0.2, 6.0)
        assertTrue(design.materialFamily.isNotBlank())
        assertTrue(design.targetFrequencyGHz > 0.0)
    }
}
