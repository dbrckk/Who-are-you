package com.whoareyou.app

enum class RetakeRecommendationReason {
    START_TRACKING,
    RECHECK_CHANGE
}

data class RetakeRecommendation(
    val quizId: String,
    val reason: RetakeRecommendationReason
)

object RetakeRecommendationEngine {
    fun recommend(
        quizIds: List<String>,
        latestScores: Map<String, Int>,
        previousScores: Map<String, Int>
    ): RetakeRecommendation? {
        if (quizIds.isEmpty()) return null

        quizIds.firstOrNull { quizId ->
            quizId in latestScores && quizId !in previousScores
        }?.let { quizId ->
            return RetakeRecommendation(quizId, RetakeRecommendationReason.START_TRACKING)
        }

        return quizIds.mapIndexedNotNull { index, quizId ->
            val change = ScoreChangeEngine.compare(previousScores[quizId], latestScores[quizId] ?: return@mapIndexedNotNull null)
                ?: return@mapIndexedNotNull null
            Triple(quizId, change.absoluteDelta, index)
        }.maxWithOrNull(
            compareBy<Triple<String, Int, Int>> { it.second }
                .thenBy { -it.third }
        )?.let { (quizId, _, _) ->
            RetakeRecommendation(quizId, RetakeRecommendationReason.RECHECK_CHANGE)
        }
    }
}
