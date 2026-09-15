package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class RecommendationAttributionTest {
    @Test
    fun `session rejects pending handoff without attempt`() {
        assertThrows(IllegalArgumentException::class.java) {
            RecommendationAttributionSession(attempt = null, awaitingTestStart = true)
        }
    }

    @Test
    fun `empty session singleton is valid`() {
        assertNull(RecommendationAttributionSession.Empty.attempt)
        assertFalse(RecommendationAttributionSession.Empty.awaitingTestStart)
    }

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
    fun `duplicate pending recommendation start is idempotent`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val duplicate = RecommendationAttribution.recommendationStarted(
            started,
            "values",
            signatureGuided = true
        )

        assertEquals(started, duplicate)
    }

    @Test
    fun `different recommendation replaces pending handoff`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val replacement = RecommendationAttribution.recommendationStarted(
            started,
            "social",
            signatureGuided = false
        )

        assertEquals("social", replacement.attempt?.quizId)
        assertEquals(RecommendationMode.GENERIC, replacement.attempt?.mode)
        assertTrue(replacement.awaitingTestStart)
    }

    @Test
    fun `new recommendation start cannot overwrite active attribution`() {
        val pending = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(pending, "values")
        val staleStart = RecommendationAttribution.recommendationStarted(
            active,
            "social",
            signatureGuided = false
        )

        assertEquals(active, staleStart)
        assertEquals("values", staleStart.attempt?.quizId)
        assertFalse(staleStart.awaitingTestStart)
    }

    @Test
    fun `cancel pending clears matching handoff`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val cancelled = RecommendationAttribution.cancelPending(started, "values")

        assertNull(cancelled.attempt)
        assertFalse(cancelled.awaitingTestStart)
    }

    @Test
    fun `cancel pending ignores different quiz`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val unchanged = RecommendationAttribution.cancelPending(started, "social")

        assertEquals(started, unchanged)
    }

    @Test
    fun `cancel pending does not erase active attribution after test start`() {
        val started = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val active = RecommendationAttribution.testStarted(started, "values")
        val unchanged = RecommendationAttribution.cancelPending(active, "values")

        assertEquals(active, unchanged)
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
    fun `completion before matching test start is not attributed`() {
        val pending = RecommendationAttribution.recommendationStarted("values", signatureGuided = true)
        val (completion, cleared) = RecommendationAttribution.testCompleted(pending, "values")

        assertNull(completion)
        assertEquals(RecommendationAttributionSession.Empty, cleared)
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
