package com.whoareyou.app

object AppEvents {
    @Volatile
    private var sink: EventSink = LogEventSink

    fun configure() {
        sink = BuildConfig.TELEMETRY_ENDPOINT
            .takeIf { it.startsWith("https://") }
            ?.let(::HttpEventSink)
            ?: LogEventSink
    }

    fun log(name: String, params: Map<String, Any?> = emptyMap()) {
        sink.send(name, params)
    }

    fun recordError(throwable: Throwable, context: Map<String, Any?> = emptyMap()) {
        sink.recordError(throwable, context)
    }

    fun testStart(quizId: String) = log("test_start", mapOf("quiz_id" to quizId))
    fun testComplete(quizId: String, score: Int) = log("test_complete", mapOf("quiz_id" to quizId, "score" to score))
    fun recommendationView(quizId: String, signatureGuided: Boolean) = log(
        "recommendation_view",
        RecommendationTelemetry.params(quizId, signatureGuided)
    )
    fun recommendationStart(quizId: String, signatureGuided: Boolean) = log(
        "recommendation_start",
        RecommendationTelemetry.params(quizId, signatureGuided)
    )
    fun recommendationComplete(attempt: RecommendationAttempt) = log(
        "recommendation_complete",
        mapOf(
            "quiz_id" to attempt.quizId,
            "mode" to attempt.mode.wireValue
        )
    )
    fun resultShare(quizId: String, score: Int? = null) = log("result_share", buildMap { put("quiz_id", quizId); if (score != null) put("score", score) })
    fun challengeCreate(quizId: String, score: Int? = null) = log("challenge_create", buildMap { put("quiz_id", quizId); if (score != null) put("score", score) })
    fun challengeOpen(quizId: String, source: String) = log("challenge_open", mapOf("quiz_id" to quizId, "source" to source))
    fun challengeComplete(quizId: String, compatibility: Int) = log("challenge_complete", mapOf("quiz_id" to quizId, "compatibility" to compatibility.coerceIn(0, 100)))
    fun compatibilityShare(quizId: String, compatibility: Int) = log("compatibility_share", mapOf("quiz_id" to quizId, "compatibility" to compatibility.coerceIn(0, 100)))
    fun appLinkOpen(path: String) = log("app_link_open", mapOf("path" to path))
    fun profileShare(archetype: String, completedCount: Int) = log("profile_share", mapOf("archetype" to archetype, "completed_count" to completedCount))
    fun profileChallenge(quizId: String, score: Int) = log("profile_challenge", mapOf("quiz_id" to quizId, "score" to score))
    fun signatureUnlock(signature: SignatureProfileMatch) = log(
        "signature_unlock",
        mapOf(
            "signature_key" to signature.key.name.lowercase(),
            "confidence" to signature.confidence.coerceIn(0, 100),
            "evidence_count" to signature.supportingQuizIds.size
        )
    )
    fun signatureShare(signature: SignatureProfileMatch) = log(
        "signature_share",
        mapOf(
            "signature_key" to signature.key.name.lowercase(),
            "confidence" to signature.confidence.coerceIn(0, 100)
        )
    )
    fun dailyQuestionView(questionId: String) = log("daily_question_view", mapOf("question_id" to questionId))
    fun dailyQuestionVote(questionId: String, option: Int) = log("daily_question_vote", mapOf("question_id" to questionId, "option" to option.coerceIn(0, 1)))
    fun streakContinue(streak: Int) = log("streak_continue", mapOf("streak" to streak))
    fun achievementUnlock(achievementId: String) = log("achievement_unlock", mapOf("achievement_id" to achievementId))
    fun premiumView() = log("premium_view")
    fun purchaseSuccess(productId: String) = log("purchase_success", mapOf("product_id" to productId))
    fun adImpression(placement: String) = log("ad_impression", mapOf("placement" to placement))
}
