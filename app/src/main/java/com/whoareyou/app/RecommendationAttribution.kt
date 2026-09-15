package com.whoareyou.app

data class RecommendationAttempt(
    val quizId: String,
    val mode: RecommendationMode
)

data class RecommendationAttributionSession(
    val attempt: RecommendationAttempt? = null,
    val awaitingTestStart: Boolean = false
) {
    init {
        require(!awaitingTestStart || attempt != null) {
            "A pending recommendation handoff requires an attribution attempt"
        }
    }

    companion object {
        val Empty = RecommendationAttributionSession()
    }
}

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

    fun recommendationStarted(
        session: RecommendationAttributionSession,
        quizId: String,
        signatureGuided: Boolean
    ): RecommendationAttributionSession =
        if (session.awaitingTestStart &&
            session.attempt?.quizId == quizId &&
            session.attempt.mode == RecommendationTelemetry.mode(signatureGuided)
        ) {
            session
        } else {
            recommendationStarted(quizId, signatureGuided)
        }

    fun cancelPending(
        session: RecommendationAttributionSession,
        quizId: String
    ): RecommendationAttributionSession = if (
        session.awaitingTestStart && session.attempt?.quizId == quizId
    ) {
        RecommendationAttributionSession.Empty
    } else {
        session
    }

    fun testStarted(
        session: RecommendationAttributionSession,
        quizId: String
    ): RecommendationAttributionSession = if (
        session.awaitingTestStart && session.attempt?.quizId == quizId
    ) {
        session.copy(awaitingTestStart = false)
    } else {
        RecommendationAttributionSession.Empty
    }

    fun completion(
        attempt: RecommendationAttempt?,
        completedQuizId: String
    ): RecommendationAttempt? = attempt?.takeIf { it.quizId == completedQuizId }

    fun testCompleted(
        session: RecommendationAttributionSession,
        quizId: String
    ): Pair<RecommendationAttempt?, RecommendationAttributionSession> {
        val completedAttempt = if (session.awaitingTestStart) {
            null
        } else {
            completion(session.attempt, quizId)
        }
        return completedAttempt to RecommendationAttributionSession.Empty
    }
}
