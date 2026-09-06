package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `recommended launch survives its immediate matching test start`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(started, "values")

        assertEquals("values", active.attempt?.quizId)
        assertFalse(active.awaitingTestStart)
    }

    @Test
    fun `later normal start clears abandoned recommendation even for same quiz`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(started, "values")
        val cleared = RecommendationAttribution.testStarted(active, "values")

        assertNull(cleared.attempt)
        assertFalse(cleared.awaitingTestStart)
    }

    @Test
    fun `different test start clears recommendation attribution`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val cleared = RecommendationAttribution.testStarted(started, "social")

        assertNull(cleared.attempt)
    }

    @Test
    fun `matching completion emits attempt and consumes session`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(started, "values")
        val (completion, cleared) = RecommendationAttribution.testCompleted(active, "values")

        assertEquals(active.attempt, completion)
        assertNull(cleared.attempt)
        assertFalse(cleared.awaitingTestStart)
    }

    @Test
    fun `mismatched completion does not emit and still consumes session`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(started, "values")
        val (completion, cleared) = RecommendationAttribution.testCompleted(active, "social")

        assertNull(completion)
        assertNull(cleared.attempt)
    }

    @Test
    fun `recommendation started marks pending handoff`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = false)

        assertTrue(started.awaitingTestStart)
        assertEquals(RecommendationMode.GENERIC, started.attempt?.mode)
    }
}
