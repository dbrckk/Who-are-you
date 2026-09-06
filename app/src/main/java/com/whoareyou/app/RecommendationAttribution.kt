package com.whoareyou.app

data class RecommendationAttempt(
    val quizId: String,
    val mode: RecommendationMode
)

data class RecommendationAttributionSession(
    val attempt: RecommendationAttempt? = null,
    val awaitingTestStart: Boolean = false
)

object RecommendationAttribution {
    fun start(quizId: String, signatureGuided: Boolean): RecommendationAttempt =
        RecommendationAttempt(
            quizId = quizId,
            mode = RecommendationTelemetry.mode(signatureGuided)
        )

    fun recommendationStarted(
        quizId: String,
        signatureGuided: Boolean
    ): RecommendationAttributionSession = RecommendationAttributionSession(
        attempt = start(quizId, signatureGuided),
        awaitingTestStart = true
    )

    fun testStarted(
        session: RecommendationAttributionSession,
        quizId: String
    ): RecommendationAttributionSession = if (
        session.awaitingTestStart && session.attempt?.quizId == quizId
    ) {
        session.copy(awaitingTestStart = false)
    } else {
        RecommendationAttributionSession()
    }

    fun completion(
        attempt: RecommendationAttempt?,
        completedQuizId: String
    ): RecommendationAttempt? = attempt?.takeIf { it.quizId == completedQuizId }

    fun testCompleted(
        session: RecommendationAttributionSession,
        quizId: String
    ): Pair<RecommendationAttempt?, RecommendationAttributionSession> =
        completion(session.attempt, quizId) to RecommendationAttributionSession()
}
