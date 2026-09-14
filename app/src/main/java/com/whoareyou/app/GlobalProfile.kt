package com.whoareyou.app

data class ProfileDimension(
    val quizId: String,
    val title: String,
    val score: Int,
    val resultTitle: String,
    val metricLabel: String,
    val traitWeights: List<QuizTraitWeight> = emptyList(),
    val change: ScoreChange? = null
) {
    val scoreChange: ScoreChange?
        get() = change
}

data class GlobalProfileSummary(
    val dominantArchetype: String,
    val completionPercent: Int,
    val completedCount: Int,
    val totalCount: Int,
    val dimensions: List<ProfileDimension>,
    val signature: SignatureProfileMatch? = null,
    val traitGraph: TraitGraph = TraitGraph(emptyList(), 0),
    val coverage: ProfileCoverage = ProfileCoverage(
        knownTraitCount = 0,
        totalTraitCount = 0,
        coveragePercent = 0,
        averageConfidence = 0,
        strongTraitCount = 0,
        uncertainTraitCount = 0,
        traits = emptyList()
    ),
    val traitEvolution: TraitEvolutionSummary = TraitEvolutionSummary(
        traits = emptyList(),
        meaningfulChanges = emptyList(),
        newEvidence = emptyList()
    ),
    val longitudinalTrends: List<LongitudinalTrend> = emptyList()
)

object GlobalProfileEngine {
    fun build(
        catalog: List<Quiz>,
        latestScores: Map<String, Int>,
        previousScores: Map<String, Int> = emptyMap(),
        scoreHistory: Map<String, List<Int>> = emptyMap(),
        timedScoreHistory: Map<String, List<TimedScore>> = emptyMap()
    ): GlobalProfileSummary {
        val dimensions = catalog.mapNotNull { quiz ->
            latestScores[quiz.id]?.let { rawScore ->
                val score = rawScore.coerceIn(0, 100)
                ProfileDimension(
                    quizId = quiz.id,
                    title = quiz.title,
                    score = score,
                    resultTitle = quiz.resultTitleFor(score),
                    metricLabel = if (score >= 50) quiz.metricHigh else quiz.metricLow,
                    traitWeights = quiz.traits,
                    change = ScoreChangeEngine.compare(previousScores[quiz.id], score)
                )
            }
        }

        val dominant = dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
        val completion = if (catalog.isEmpty()) 0 else ((dimensions.size * 100f) / catalog.size).toInt().coerceIn(0, 100)
        val stableScores = dimensions.associate { it.quizId to it.score }

        val traitGraph = TraitGraphEngine.build(dimensions)
        return GlobalProfileSummary(
            dominantArchetype = dominant?.resultTitle ?: "Profile undiscovered",
            completionPercent = completion,
            completedCount = dimensions.size,
            totalCount = catalog.size,
            dimensions = dimensions,
            signature = SignatureProfiles.primary(stableScores),
            traitGraph = traitGraph,
            coverage = ProfileCoverageEngine.build(catalog, traitGraph),
            traitEvolution = TraitEvolutionEngine.build(
                catalog = catalog,
                latestScores = latestScores,
                scoreHistory = scoreHistory
            ),
            longitudinalTrends = timedScoreHistory
                .map { (quizId, points) -> LongitudinalTrendEngine.build(quizId, points) }
                .filter { it.kind != LongitudinalTrendKind.INSUFFICIENT }
                .sortedByDescending { kotlin.math.abs(it.netChange) + it.volatility }
        )
    }
}

fun Quiz.resultTitleFor(score: Int): String = when {
    score < 35 -> lowTitle
    score < 70 -> midTitle
    else -> highTitle
}

fun Quiz.resultDescriptionFor(score: Int): String = when {
    score < 35 -> lowDescription
    score < 70 -> midDescription
    else -> highDescription
}
