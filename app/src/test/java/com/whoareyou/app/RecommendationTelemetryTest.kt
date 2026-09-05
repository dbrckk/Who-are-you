package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RecommendationTelemetryTest {
    @Test
    fun genericRecommendationUsesGenericMode() {
        val params = RecommendationTelemetry.params("planning_style", signatureGuided = false)

        assertEquals("planning_style", params["quiz_id"])
        assertEquals("generic", params["mode"])
        assertEquals(2, params.size)
    }

    @Test
    fun guidedRecommendationUsesNeutralGuidedMode() {
        val params = RecommendationTelemetry.params("novelty_seeker", signatureGuided = true)

        assertEquals("novelty_seeker", params["quiz_id"])
        assertEquals("signature_guided", params["mode"])
        assertEquals(2, params.size)
        assertFalse(params.keys.any { it.contains("signature_key") || it.contains("target") })
    }
}
