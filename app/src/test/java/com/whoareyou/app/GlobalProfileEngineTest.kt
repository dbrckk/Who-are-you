package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalProfileEngineTest {
    private fun quiz(id: String, title: String = id): Quiz = Quiz(
        id = id,
        title = title,
        hook = "hook",
        time = "60s",
        accent = "•",
        lowTitle = "LOW-$id",
        midTitle = "MID-$id",
        highTitle = "HIGH-$id",
        lowDescription = "low",
        midDescription = "mid",
        highDescription = "high",
        metricLow = "LOW METRIC",
        metricHigh = "HIGH METRIC",
        questions = listOf(
            Question("Q", listOf(
                Answer("A", 0), Answer("B", 1), Answer("C", 2), Answer("D", 3)
            ))
        )
    )

    @Test
    fun resultBuckets_useExpectedBoundaries() {
        val quiz = quiz("logic")
        assertEquals("LOW-logic", quiz.resultTitleFor(34))
        assertEquals("MID-logic", quiz.resultTitleFor(35))
        assertEquals("MID-logic", quiz.resultTitleFor(69))
        assertEquals("HIGH-logic", quiz.resultTitleFor(70))
    }

    @Test
    fun build_reportsCompletionAndClampsScores() {
        val catalog = listOf(quiz("a"), quiz("b"), quiz("c"), quiz("d"))
        val summary = GlobalProfileEngine.build(catalog, mapOf("a" to 120, "b" to -10))

        assertEquals(50, summary.completionPercent)
        assertEquals(2, summary.completedCount)
        assertEquals(4, summary.totalCount)
        assertEquals(100, summary.dimensions.first { it.quizId == "a" }.score)
        assertEquals(0, summary.dimensions.first { it.quizId == "b" }.score)
        assertNull(summary.signature)
    }

    @Test
    fun build_selectsMostDistinctDimensionAsDominant() {
        val catalog = listOf(quiz("near"), quiz("far"), quiz("middle"))
        val summary = GlobalProfileEngine.build(catalog, mapOf("near" to 55, "far" to 92, "middle" to 40))

        assertEquals("HIGH-far", summary.dominantArchetype)
        assertTrue(summary.dimensions.any { it.quizId == "far" && it.metricLabel == "HIGH METRIC" })
    }

    @Test
    fun build_exposesPreviousToLatestEvolutionWithoutChangingLatestScore() {
        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz("planning_style"), quiz("adaptability")),
            latestScores = mapOf("planning_style" to 74, "adaptability" to 82),
            previousScores = mapOf("planning_style" to 61)
        )

        val planning = summary.dimensions.first { it.quizId == "planning_style" }
        val adaptability = summary.dimensions.first { it.quizId == "adaptability" }
        assertEquals(74, planning.score)
        assertEquals(61, planning.change?.previousScore)
        assertEquals(13, planning.change?.delta)
        assertEquals(ScoreChangeDirection.HIGHER, planning.change?.direction)
        assertNull(adaptability.change)
    }

    @Test
    fun build_attachesSignatureFromStableQuizIds() {
        val ids = listOf(
            "learning_drive",
            "novelty_seeker",
            "independence",
            "communication_style",
            "planning_style",
            "self_discipline",
            "patience",
            "stress_response"
        )
        val catalog = ids.map(::quiz)
        val summary = GlobalProfileEngine.build(
            catalog,
            mapOf(
                "learning_drive" to 82,
                "novelty_seeker" to 78,
                "independence" to 88,
                "communication_style" to 50,
                "planning_style" to 50,
                "self_discipline" to 50,
                "patience" to 50,
                "stress_response" to 50
            )
        )

        assertEquals(SignatureProfileKey.INDEPENDENT_EXPLORER, summary.signature?.key)
        assertTrue((summary.signature?.confidence ?: 0) >= 65)
    }

    @Test
    fun emptyCatalog_isSafe() {
        val summary = GlobalProfileEngine.build(emptyList(), emptyMap())
        assertEquals(0, summary.completionPercent)
        assertEquals(0, summary.completedCount)
        assertEquals(0, summary.totalCount)
        assertTrue(summary.dimensions.isEmpty())
        assertNull(summary.signature)
    }
}
