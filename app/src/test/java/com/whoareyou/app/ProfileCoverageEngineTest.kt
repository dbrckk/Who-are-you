package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileCoverageEngineTest {
    @Test
    fun `empty evidence reports zero known traits but preserves taxonomy size`() {
        val catalog = listOf(
            quiz("a", QuizTraitWeight("assertiveness", 1.0)),
            quiz("b", QuizTraitWeight("curiosity", 1.0))
        )

        val coverage = ProfileCoverageEngine.build(catalog, TraitGraph(emptyList(), 0))

        assertEquals(0, coverage.knownTraitCount)
        assertEquals(2, coverage.totalTraitCount)
        assertEquals(0, coverage.coveragePercent)
        assertEquals(2, coverage.uncertainTraitCount)
    }

    @Test
    fun `unknown trait is preferred over already strong trait`() {
        val catalog = listOf(
            quiz("known", QuizTraitWeight("assertiveness", 1.0)),
            quiz("unknown", QuizTraitWeight("curiosity", 1.0))
        )
        val graph = TraitGraph(
            traits = listOf(
                ProfileTrait(
                    id = "assertiveness",
                    score = 85,
                    confidence = 90,
                    evidence = listOf(
                        TraitEvidence(
                            quizId = "known",
                            quizTitle = "known",
                            sourceScore = 85,
                            weight = 1.0,
                            contribution = 85,
                            signalStrength = 0.9
                        ),
                        TraitEvidence(
                            quizId = "known2",
                            quizTitle = "known2",
                            sourceScore = 80,
                            weight = 1.0,
                            contribution = 80,
                            signalStrength = 0.8
                        )
                    ),
                    contradictoryEvidenceCount = 0
                )
            ),
            evidenceCount = 2
        )
        val coverage = ProfileCoverageEngine.build(catalog, graph)

        val recommendation = CoverageRecommendationEngine.recommend(
            catalog = catalog,
            completedQuizIds = emptySet(),
            coverage = coverage
        )

        assertNotNull(recommendation)
        assertEquals("unknown", recommendation?.quizId)
        assertTrue("curiosity" in recommendation!!.targetedTraitIds)
    }

    @Test
    fun `contradictory trait remains uncertain`() {
        val catalog = listOf(quiz("a", QuizTraitWeight("adaptability", 1.0)))
        val graph = TraitGraph(
            traits = listOf(
                ProfileTrait(
                    id = "adaptability",
                    score = 52,
                    confidence = 65,
                    evidence = listOf(
                        TraitEvidence("a", "a", 80, 1.0, 80, 0.6),
                        TraitEvidence("b", "b", 25, 1.0, 25, 0.5)
                    ),
                    contradictoryEvidenceCount = 1
                )
            ),
            evidenceCount = 2
        )

        val coverage = ProfileCoverageEngine.build(catalog, graph)
        val trait = coverage.traits.single()

        assertTrue(trait.contradictoryEvidenceCount > 0)
        assertTrue(coverage.uncertainTraitCount > 0)
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
