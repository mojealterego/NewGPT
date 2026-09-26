package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.cognitive.EvidenceLedger
import com.mojealterego.newgpt.domain.cognitive.EvidenceType
import com.mojealterego.newgpt.domain.cognitive.Supervisor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupervisorTest {
    @Test
    fun acceptsNonEmptyOutput() {
        val supervisor = Supervisor(EvidenceLedger())
        assertTrue(supervisor.verify("goal", "result").accepted)
    }

    @Test
    fun rejectsEmptyOutput() {
        val supervisor = Supervisor(EvidenceLedger())
        assertFalse(supervisor.verify("goal", "").accepted)
    }

    @Test
    fun countsRequiredEvidence() {
        val ledger = EvidenceLedger()
        ledger.record(EvidenceType.DOCUMENT, "source")
        val supervisor = Supervisor(ledger)
        assertTrue(supervisor.verify("goal", "result", requiredEvidence = 1).accepted)
    }
}
