package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitEvolutionEngineTest {
    @Test
    fun `first measurement is classified as new evidence not movement`() {
        val catalog = listOf(quiz("a", QuizTraitWeight("curiosity", 1.0)))

        val result = TraitEvolutionEngine.build(
            catalog = catalog,
            latestScores = mapOf("a" to 80),
            scoreHistory = mapOf("a" to listOf(80))
        )

        val trait = result.traits.single()
        assertEquals(TraitEvolutionKind.NEW_EVIDENCE, trait.kind)
        assertEquals(null, trait.previousScore)
        assertEquals(80, trait.currentScore)
    }

    @Test
    fun `meaningful retake movement is separated from new information`() {
        val catalog = listOf(quiz("a", QuizTraitWeight("curiosity", 1.0)))

        val result = TraitEvolutionEngine.build(
            catalog = catalog,
            latestScores = mapOf("a" to 82),
            scoreHistory = mapOf("a" to listOf(55, 82))
        )

        val trait = result.traits.single()
        assertEquals(TraitEvolutionKind.MOVED, trait.kind)
        assertEquals(55, trait.previousScore)
        assertEquals(27, trait.delta)
        assertTrue(result.meaningfulChanges.any { it.traitId == "curiosity" })
    }

    @Test
    fun `small retake movement remains stable`() {
        val catalog = listOf(quiz("a", QuizTraitWeight("curiosity", 1.0)))

        val result = TraitEvolutionEngine.build(
            catalog = catalog,
            latestScores = mapOf("a" to 74),
            scoreHistory = mapOf("a" to listOf(70, 74))
        )

        assertEquals(TraitEvolutionKind.STABLE, result.traits.single().kind)
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
