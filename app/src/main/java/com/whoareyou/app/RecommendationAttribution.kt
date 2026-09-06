package com.whoareyou.app

data class RecommendationAttempt(
    val quizId: String,
    val mode: RecommendationMode
)

object RecommendationAttribution {
    fun start(quizId: String, signatureGuided: Boolean): RecommendationAttempt =
        RecommendationAttempt(
            quizId = quizId,
            mode = RecommendationTelemetry.mode(signatureGuided)
        )

    fun completion(
        attempt: RecommendationAttempt?,
        completedQuizId: String
    ): RecommendationAttempt? = attempt?.takeIf { it.quizId == completedQuizId }
}
