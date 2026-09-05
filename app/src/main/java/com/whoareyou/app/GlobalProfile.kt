package com.whoareyou.app

data class ProfileDimension(
    val quizId: String,
    val title: String,
    val score: Int,
    val resultTitle: String,
    val metricLabel: String
)

data class GlobalProfileSummary(
    val dominantArchetype: String,
    val completionPercent: Int,
    val completedCount: Int,
    val totalCount: Int,
    val dimensions: List<ProfileDimension>,
    val signature: SignatureProfileMatch? = null
)

object GlobalProfileEngine {
    fun build(catalog: List<Quiz>, latestScores: Map<String, Int>): GlobalProfileSummary {
        val dimensions = catalog.mapNotNull { quiz ->
            latestScores[quiz.id]?.let { rawScore ->
                val score = rawScore.coerceIn(0, 100)
                ProfileDimension(
                    quizId = quiz.id,
                    title = quiz.title,
                    score = score,
                    resultTitle = quiz.resultTitleFor(score),
                    metricLabel = if (score >= 50) quiz.metricHigh else quiz.metricLow
                )
            }
        }

        val dominant = dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
        val completion = if (catalog.isEmpty()) 0 else ((dimensions.size * 100f) / catalog.size).toInt().coerceIn(0, 100)
        val stableScores = dimensions.associate { it.quizId to it.score }

        return GlobalProfileSummary(
            dominantArchetype = dominant?.resultTitle ?: "Profile undiscovered",
            completionPercent = completion,
            completedCount = dimensions.size,
            totalCount = catalog.size,
            dimensions = dimensions,
            signature = SignatureProfiles.primary(stableScores)
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
