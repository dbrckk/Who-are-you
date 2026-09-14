package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileNarrativeEngineTest {
    @Test
    fun `repeated movement requires several points and meaningful period delta`() {
        val graph = graph("curiosity", score = 76, confidence = 72, evidenceCount = 2)
        val timeline = timeline(
            "curiosity",
            scores = listOf(48, 54, 72, 78),
            confidences = listOf(55, 58, 70, 72)
        )

        val insight = ProfileNarrativeEngine.build(graph, listOf(timeline)).insights.single()

        assertEquals(ProfileNarrativeKind.REPEATED_MOVEMENT, insight.kind)
        assertTrue(insight.scoreDelta >= 8)
    }

    @Test
    fun `stable score with stronger confidence is described separately`() {
        val graph = graph("planning", score = 62, confidence = 74, evidenceCount = 3)
        val timeline = timeline(
            "planning",
            scores = listOf(60, 61, 62, 63),
            confidences = listOf(40, 45, 70, 78)
        )

        val insight = ProfileNarrativeEngine.build(graph, listOf(timeline)).insights.single()

        assertEquals(ProfileNarrativeKind.STABLE_WITH_MORE_EVIDENCE, insight.kind)
        assertTrue(insight.confidenceDelta >= 12)
    }

    @Test
    fun `contradictory evidence outranks directional storytelling`() {
        val graph = graph(
            "assertiveness",
            score = 70,
            confidence = 70,
            evidenceCount = 3,
            contradictions = 1
        )
        val timeline = timeline(
            "assertiveness",
            scores = listOf(45, 55, 68, 72),
            confidences = listOf(55, 60, 68, 70)
        )

        val insight = ProfileNarrativeEngine.build(graph, listOf(timeline)).insights.single()

        assertEquals(ProfileNarrativeKind.CONTRADICTORY, insight.kind)
    }

    private fun graph(
        traitId: String,
        score: Int,
        confidence: Int,
        evidenceCount: Int,
        contradictions: Int = 0
    ): TraitGraph {
        val evidence = (1..evidenceCount).map { index ->
            TraitEvidence(
                quizId = "q$index",
                quizTitle = "Quiz $index",
                sourceScore = score,
                weight = 1.0,
                contribution = score,
                signalStrength = confidence / 100.0
            )
        }
        return TraitGraph(
            traits = listOf(
                ProfileTrait(
                    id = traitId,
                    score = score,
                    confidence = confidence,
                    evidence = evidence,
                    contradictoryEvidenceCount = contradictions
                )
            ),
            evidenceCount = evidenceCount
        )
    }

    private fun timeline(
        traitId: String,
        scores: List<Int>,
        confidences: List<Int>
    ): TraitTimeline {
        val points = scores.mapIndexed { index, score ->
            TimedTraitSnapshot(
                epochDay = (index + 1).toLong(),
                score = score,
                confidence = confidences[index],
                evidenceCount = 2,
                contradictoryEvidenceCount = 0,
                changedQuizIds = listOf("q1"),
                newEvidenceQuizIds = if (index == 0) listOf("q1") else emptyList(),
                retakeQuizIds = if (index > 0) listOf("q1") else emptyList()
            )
        }
        val split = points.size / 2
        val earlier = points.take(split)
        val recent = points.drop(split)
        val earlierScore = earlier.map { it.score }.average().toInt()
        val recentScore = recent.map { it.score }.average().toInt()
        val earlierConfidence = earlier.map { it.confidence }.average().toInt()
        val recentConfidence = recent.map { it.confidence }.average().toInt()

        return TraitTimeline(
            traitId = traitId,
            points = points,
            trend = LongitudinalTrendEngine.build(
                "trait:$traitId",
                points.map { TimedScore(it.score, it.epochDay) }
            ),
            periodComparison = TraitTimelinePeriodComparison(
                earlierScoreAverage = earlierScore,
                recentScoreAverage = recentScore,
                scoreDelta = recentScore - earlierScore,
                earlierConfidenceAverage = earlierConfidence,
                recentConfidenceAverage = recentConfidence,
                confidenceDelta = recentConfidence - earlierConfidence
            )
        )
    }
}
