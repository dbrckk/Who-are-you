package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecommendationAttributionTest {
    @Test
    fun `start preserves quiz and guided mode`() {
        val attempt = RecommendationAttribution.start("values", signatureGuided = true)

        assertEquals("values", attempt.quizId)
        assertEquals(RecommendationMode.SIGNATURE_GUIDED, attempt.mode)
    }

    @Test
    fun `start uses generic mode for non guided recommendation`() {
        val attempt = RecommendationAttribution.start("values", signatureGuided = false)

        assertEquals(RecommendationMode.GENERIC, attempt.mode)
    }

    @Test
    fun `completion returns matching recommendation attempt`() {
        val attempt = RecommendationAttempt("values", RecommendationMode.SIGNATURE_GUIDED)

        assertEquals(attempt, RecommendationAttribution.completion(attempt, "values"))
    }

    @Test
    fun `completion ignores different quiz`() {
        val attempt = RecommendationAttempt("values", RecommendationMode.SIGNATURE_GUIDED)

        assertNull(RecommendationAttribution.completion(attempt, "social"))
    }

    @Test
    fun `completion ignores missing attribution`() {
        assertNull(RecommendationAttribution.completion(null, "values"))
    }
}
