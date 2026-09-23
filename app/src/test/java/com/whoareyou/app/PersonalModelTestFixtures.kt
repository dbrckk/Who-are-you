package com.whoareyou.app

fun evidence(
    quizId: String,
    contribution: Int,
    strength: Double = 0.8,
    weight: Double = 1.0
): TraitEvidence = TraitEvidence(
    quizId = quizId,
    quizTitle = quizId,
    sourceScore = contribution,
    weight = weight,
    contribution = contribution,
    signalStrength = strength
)

fun profileTrait(
    id: String,
    score: Int,
    confidence: Int,
    evidenceCount: Int
): ProfileTrait = ProfileTrait(
    id = id,
    score = score,
    confidence = confidence,
    evidence = List(evidenceCount) { index ->
        evidence("q$index", contribution = score)
    },
    contradictoryEvidenceCount = 0
)

fun graphFor(
    id: String,
    score: Int,
    confidence: Int,
    evidenceCount: Int
): TraitGraph = TraitGraph(
    traits = listOf(profileTrait(id, score, confidence, evidenceCount)),
    evidenceCount = evidenceCount
)

fun coverageFor(
    id: String,
    evidenceCount: Int,
    confidence: Int,
    contradictoryEvidenceCount: Int = 0,
    status: CoverageStatus = CoverageStatus.STRONG
): ProfileCoverage = ProfileCoverage(
    knownTraitCount = 1,
    totalTraitCount = 1,
    coveragePercent = 100,
    averageConfidence = confidence,
    strongTraitCount = if (status == CoverageStatus.STRONG) 1 else 0,
    uncertainTraitCount = if (status == CoverageStatus.STRONG) 0 else 1,
    traits = listOf(
        TraitCoverage(
            traitId = id,
            evidenceCount = evidenceCount,
            confidence = confidence,
            contradictoryEvidenceCount = contradictoryEvidenceCount,
            status = status
        )
    )
)

fun timelineFor(
    traitId: String,
    kind: LongitudinalTrendKind,
    pointCount: Int
): TraitTimeline {
    val points = List(pointCount) { index ->
        TimedTraitSnapshot(
            epochDay = (index + 1).toLong(),
            score = 70 + index,
            confidence = 70,
            evidenceCount = 3,
            contradictoryEvidenceCount = 0,
            changedQuizIds = listOf("q$index"),
            newEvidenceQuizIds = if (index == 0) listOf("q$index") else emptyList(),
            retakeQuizIds = if (index == 0) emptyList() else listOf("q$index")
        )
    }
    return TraitTimeline(
        traitId = traitId,
        points = points,
        trend = LongitudinalTrend(
            quizId = "trait:$traitId",
            points = points.map { TimedScore(it.score, it.epochDay) },
            slopePerStep = when (kind) {
                LongitudinalTrendKind.RISING -> 3.0
                LongitudinalTrendKind.FALLING -> -3.0
                else -> 0.0
            },
            volatility = if (kind == LongitudinalTrendKind.VOLATILE) 20.0 else 2.0,
            netChange = when (kind) {
                LongitudinalTrendKind.RISING -> 12
                LongitudinalTrendKind.FALLING -> -12
                else -> 0
            },
            kind = kind
        ),
        periodComparison = null
    )
}

fun testQuiz(
    id: String,
    traits: List<QuizTraitWeight>
): Quiz = Quiz(
    id = id,
    title = id,
    hook = "Hook",
    time = "1 min",
    accent = "cyan",
    lowTitle = "Low",
    midTitle = "Mid",
    highTitle = "High",
    lowDescription = "Low",
    midDescription = "Mid",
    highDescription = "High",
    metricLow = "Low metric",
    metricHigh = "High metric",
    questions = listOf(
        Question(
            text = "Q",
            answers = listOf(
                Answer("A", 0),
                Answer("B", 1),
                Answer("C", 2),
                Answer("D", 3)
            )
        )
    ),
    traits = traits
)

fun graphWithTraits(order: List<String>): TraitGraph {
    val traits = order.mapIndexed { index, id ->
        profileTrait(
            id = id,
            score = if (index % 2 == 0) 78 else 72,
            confidence = 70,
            evidenceCount = 3
        )
    }
    return TraitGraph(traits = traits, evidenceCount = traits.sumOf { it.evidenceCount })
}

fun coverageWithTraits(order: List<String>): ProfileCoverage {
    val traits = order.map { id ->
        TraitCoverage(id, 3, 70, 0, CoverageStatus.STRONG)
    }
    return ProfileCoverage(
        knownTraitCount = traits.size,
        totalTraitCount = traits.size,
        coveragePercent = 100,
        averageConfidence = 70,
        strongTraitCount = traits.size,
        uncertainTraitCount = 0,
        traits = traits
    )
}
