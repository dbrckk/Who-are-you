package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedJourneysTest {
    @Test
    fun `journey membership stays stable as quizzes are completed`() {
        val quizzes = sampleQuizzes()
        val before = GuidedJourneyEngine.build(quizzes, emptySet())
        val after = GuidedJourneyEngine.build(quizzes, setOf("identity-a", "mind-a"))

        assertEquals(before.map { it.quizIds }, after.map { it.quizIds })
    }

    @Test
    fun `next quiz advances to first unfinished step`() {
        val journeys = GuidedJourneyEngine.build(sampleQuizzes(), setOf("identity-a"))
        val personality = journeys.first { it.definition.id == GuidedJourneyId.PERSONALITY }

        assertEquals("mind-a", personality.nextQuizId)
        assertEquals(1, personality.completedCount)
        assertFalse(personality.isComplete)
    }

    @Test
    fun `completed journey remains actionable as a retake path`() {
        val quizzes = sampleQuizzes()
        val personalityIds = GuidedJourneyEngine.build(quizzes, emptySet())
            .first { it.definition.id == GuidedJourneyId.PERSONALITY }
            .quizIds
        val personality = GuidedJourneyEngine.build(quizzes, personalityIds.toSet())
            .first { it.definition.id == GuidedJourneyId.PERSONALITY }

        assertTrue(personality.isComplete)
        assertEquals(personality.quizIds.first(), personality.nextQuizId)
        assertEquals(100, personality.progressPercent)
    }

    @Test
    fun `journey selection represents every available theme before repeating one`() {
        val quizzes = listOf(
            quiz("identity-a", "Identity portrait"),
            quiz("identity-b", "Identity style"),
            quiz("identity-c", "Identity self"),
            quiz("identity-d", "Identity core"),
            quiz("mind-a", "Logic mind"),
            quiz("lifestyle-a", "Lifestyle routine")
        )

        val personality = GuidedJourneyEngine.build(quizzes, emptySet(), maxQuizzesPerJourney = 4)
            .first { it.definition.id == GuidedJourneyId.PERSONALITY }

        assertEquals(
            listOf("identity-a", "mind-a", "lifestyle-a", "identity-b"),
            personality.quizIds
        )
    }

    @Test
    fun `journey fills remaining slots when one theme has fewer quizzes`() {
        val quizzes = listOf(
            quiz("emotion-a", "Love attachment"),
            quiz("social-a", "Social confidence"),
            quiz("social-b", "Social group"),
            quiz("social-c", "Social conversation")
        )

        val relationships = GuidedJourneyEngine.build(quizzes, emptySet(), maxQuizzesPerJourney = 4)
            .first { it.definition.id == GuidedJourneyId.RELATIONSHIPS }

        assertEquals(
            listOf("emotion-a", "social-a", "social-b", "social-c"),
            relationships.quizIds
        )
    }

    private fun sampleQuizzes(): List<Quiz> = listOf(
        quiz("identity-a", "Identity portrait"),
        quiz("mind-a", "Logic mind"),
        quiz("lifestyle-a", "Lifestyle routine"),
        quiz("emotion-a", "Love attachment"),
        quiz("social-a", "Social confidence"),
        quiz("values-a", "Values purpose"),
        quiz("growth-a", "Growth ambition"),
        quiz("control-a", "Control planning"),
        quiz("energy-a", "Energy battery")
    )

    private fun quiz(id: String, title: String) = Quiz(
        id = id,
        title = title,
        hook = title,
        time = "2 min",
        accent = "•",
        lowTitle = "Low",
        midTitle = "Mid",
        highTitle = "High",
        lowDescription = "Low",
        midDescription = "Mid",
        highDescription = "High",
        metricLow = "Low",
        metricHigh = "High",
        questions = emptyList()
    )
}