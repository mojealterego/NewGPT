package com.mojealterego.newgpt.nexus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetaArchitectRuntimeTest {
    @Test fun decisionMatrixScoresOptions() {
        val runtime=MetaArchitectRuntime()
        val scores=runtime.decisions.score(listOf(
            DecisionOption("a",mapOf("quality" to .9,"latency" to 100.0,"cost" to .5,"privacy" to .8,"reliability" to .9)),
            DecisionOption("b",mapOf("quality" to .7,"latency" to 500.0,"cost" to .9,"privacy" to .8,"reliability" to .9))
        ))
        assertEquals(2,scores.size)
        assertTrue(scores.all{it.evidenceCoverage>0.9})
    }

    @Test fun contextBudgetAndSchemaAreStrict() {
        val runtime=MetaArchitectRuntime()
        val compiled=runtime.context.compile(
            listOf(ContextItem("a","important",4,1.0,1.0),ContextItem("b","large",20,.1,.5)),
            ContextBudget(8)
        )
        assertEquals(1,compiled.items.size)
        val schema=StrictSchema(listOf(SchemaField("name",true){it is String && it.isNotBlank()}))
        assertTrue(schema.validate(emptyMap()).contains("missing:name"))
        assertTrue(schema.validate(mapOf("name" to 7)).contains("invalid:name"))
    }

    @Test fun securityAndGovernanceRequireHumanForConsequentialChanges() {
        val runtime=MetaArchitectRuntime()
        assertTrue(runtime.zeroTrust.check(Risk.WRITE,false,false).requiresHuman)
        val result=runtime.governance.check("m1")
        assertTrue(result.inputValid)
        assertTrue(result.humanApprovalRequired)
        assertTrue(result.rollbackRequired)
    }


    @Test fun gracefulDegradationAndClarificationWork() {
        val runtime=MetaArchitectRuntime()
        assertEquals("BASIC",runtime.loadShedder.mode(true,false))
        val clarification=runtime.clarifier.resolve("raport",listOf("raport sprzedaż","serwer","hr"))
        assertTrue(clarification.needed)
    }
}
