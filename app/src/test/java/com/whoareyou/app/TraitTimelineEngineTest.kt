package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitTimelineEngineTest {
    @Test
    fun `timeline merges multiple contributing quizzes into one trait`() {
        val catalog = listOf(
            quiz("a", QuizTraitWeight("assertiveness", 1.0)),
            quiz("b", QuizTraitWeight("assertiveness", 0.5))
        )
        val history = mapOf(
            "a" to listOf(TimedScore(60, 0), TimedScore(72, 10)),
            "b" to listOf(TimedScore(80, 20))
        )

        val timeline = TraitTimelineEngine.build(catalog, history)
            .single { it.traitId == "assertiveness" }

        assertEquals(2, timeline.points.size)
        assertEquals(listOf("a"), timeline.points[0].retakeQuizIds)
        assertTrue(timeline.points[0].newEvidenceQuizIds.isEmpty())
        assertEquals(listOf("b"), timeline.points[1].newEvidenceQuizIds)
        assertTrue(timeline.points[1].evidenceCount >= 2)
    }

    @Test
    fun `new quiz source is not mislabeled as retake`() {
        val catalog = listOf(
            quiz("a", QuizTraitWeight("curiosity", 1.0)),
            quiz("b", QuizTraitWeight("curiosity", 0.4))
        )
        val history = mapOf(
            "a" to listOf(TimedScore(70, 0)),
            "b" to listOf(TimedScore(85, 15))
        )

        val point = TraitTimelineEngine.build(catalog, history)
            .single { it.traitId == "curiosity" }
            .points
            .single()

        assertEquals(listOf("b"), point.newEvidenceQuizIds)
        assertTrue(point.retakeQuizIds.isEmpty())
    }

    @Test
    fun `period comparison tracks score and confidence movement`() {
        val catalog = listOf(
            quiz("a", QuizTraitWeight("assertiveness", 1.0)),
            quiz("b", QuizTraitWeight("assertiveness", 0.5))
        )
        val history = mapOf(
            "a" to listOf(
                TimedScore(55, 0),
                TimedScore(60, 10),
                TimedScore(75, 30)
            ),
            "b" to listOf(
                TimedScore(80, 20),
                TimedScore(82, 40)
            )
        )

        val timeline = TraitTimelineEngine.build(catalog, history)
            .single { it.traitId == "assertiveness" }
        val comparison = timeline.periodComparison!!

        assertTrue(comparison.recentScoreAverage >= comparison.earlierScoreAverage)
        assertTrue(comparison.recentConfidenceAverage >= 0)
        assertEquals(
            comparison.recentScoreAverage - comparison.earlierScoreAverage,
            comparison.scoreDelta
        )
    }

    @Test
    fun `unknown-date migration points seed baseline without creating dated point`() {
        val catalog = listOf(quiz("a", QuizTraitWeight("optimism", 1.0)))
        val history = mapOf(
            "a" to listOf(
                TimedScore(55, 0),
                TimedScore(65, 0),
                TimedScore(75, 30)
            )
        )

        val timeline = TraitTimelineEngine.build(catalog, history).single()

        assertEquals(1, timeline.points.size)
        assertEquals(30, timeline.points.single().epochDay)
        assertEquals(listOf("a"), timeline.points.single().retakeQuizIds)
    }

    private fun quiz(id: String, vararg traits: QuizTraitWeight) = Quiz(
        id = id,
        title = id,
        hook = id,
        time = "1 MIN",
        accent = "•",
        lowTitle = "low",
        midTitle = "mid",
        highTitle = "high",
        lowDescription = "low",
        midDescription = "mid",
        highDescription = "high",
        metricLow = "LOW",
        metricHigh = "HIGH",
        questions = listOf(
            Question(
                text = "q",
                answers = listOf(
                    Answer("a", 0),
                    Answer("b", 1),
                    Answer("c", 2),
                    Answer("d", 3)
                )
            )
        ),
        traits = traits.toList()
    )
}
