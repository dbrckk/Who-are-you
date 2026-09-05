package com.whoareyou.app

import org.junit.Assert.assertEquals
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
    }

    @Test
    fun build_selectsMostDistinctDimensionAsDominant() {
        val catalog = listOf(quiz("near"), quiz("far"), quiz("middle"))
        val summary = GlobalProfileEngine.build(catalog, mapOf("near" to 55, "far" to 92, "middle" to 40))

        assertEquals("HIGH-far", summary.dominantArchetype)
        assertTrue(summary.dimensions.any { it.quizId == "far" && it.metricLabel == "HIGH METRIC" })
    }

    @Test
    fun emptyCatalog_isSafe() {
        val summary = GlobalProfileEngine.build(emptyList(), emptyMap())
        assertEquals(0, summary.completionPercent)
        assertEquals(0, summary.completedCount)
        assertEquals(0, summary.totalCount)
        assertTrue(summary.dimensions.isEmpty())
    }
}
