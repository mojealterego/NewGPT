package com.mojealterego.newgpt

import com.mojealterego.newgpt.domain.cognitive.InferenceRequest
import com.mojealterego.newgpt.domain.cognitive.ModelRouter
import com.mojealterego.newgpt.domain.cognitive.ModelTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelRouterTest {
    @Test fun offlineSimpleTaskUsesLocalFast() {
        val plan = ModelRouter().route(
            InferenceRequest(500, 0.2f, true, true, false, false)
        )
        assertEquals(ModelTier.LOCAL_FAST, plan.tier)
        assertTrue(plan.maxContextTokens >= 1012)
    }

    @Test fun complexToolTaskUsesDeepCloudWhenOnline() {
        val plan = ModelRouter().route(
            InferenceRequest(4000, 0.9f, false, false, true, true)
        )
        assertEquals(ModelTier.CLOUD_DEEP, plan.tier)
        assertTrue(plan.verification)
        assertTrue(plan.maxContextTokens >= 4000)
    }
}
