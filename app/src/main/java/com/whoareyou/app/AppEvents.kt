package com.whoareyou.app

object AppEvents {
    @Volatile
    private var sink: EventSink = LogEventSink

    @Volatile
    private var recommendationSession = RecommendationAttributionSession()

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

    fun appOpen() = log("app_open")
    fun screenView(screen: AppScreen) = log("screen_view", mapOf("screen" to screen.name.lowercase()))
    fun onboardingView() = log("onboarding_view")
    fun onboardingComplete() = log("onboarding_complete")
    fun catalogUnavailable() = log("catalog_unavailable")

    fun testStart(quizId: String) {
        recommendationSession = RecommendationAttribution.testStarted(recommendationSession, quizId)
        log("test_start", mapOf("quiz_id" to quizId))
    }

    fun testAbandon(quizId: String, reason: String) = log(
        "test_abandon",
        mapOf("quiz_id" to quizId, "reason" to reason)
    )

    fun testComplete(quizId: String, score: Int) {
        log("test_complete", mapOf("quiz_id" to quizId, "score_bucket" to scoreBucket(score)))
        val (completion, clearedSession) = RecommendationAttribution.testCompleted(recommendationSession, quizId)
        recommendationSession = clearedSession
        completion?.let(::recommendationComplete)
    }

    fun resultView(quizId: String, score: Int) = log(
        "result_view",
        mapOf("quiz_id" to quizId, "score_bucket" to scoreBucket(score))
    )

    fun recommendationView(quizId: String, signatureGuided: Boolean) = log(
        "recommendation_view",
        RecommendationTelemetry.params(quizId, signatureGuided)
    )

    fun recommendationStart(quizId: String, signatureGuided: Boolean) {
        recommendationSession = RecommendationAttribution.recommendationStarted(quizId, signatureGuided)
        log(
            "recommendation_start",
            RecommendationTelemetry.params(quizId, signatureGuided)
        )
    }

    fun recommendationComplete(attempt: RecommendationAttempt) = log(
        "recommendation_complete",
        mapOf(
            "quiz_id" to attempt.quizId,
            "mode" to attempt.mode.wireValue
        )
    )

    fun resultShare(quizId: String, score: Int? = null) = log(
        "result_share",
        buildMap {
            put("quiz_id", quizId)
            score?.let { put("score_bucket", scoreBucket(it)) }
        }
    )

    fun challengeCreate(quizId: String, score: Int? = null) = log(
        "challenge_create",
        buildMap {
            put("quiz_id", quizId)
            score?.let { put("score_bucket", scoreBucket(it)) }
        }
    )

    fun challengeOpen(quizId: String, source: String) = log(
        "challenge_open",
        mapOf("quiz_id" to quizId, "source" to source)
    )

    fun challengeComplete(quizId: String, compatibility: Int) = log(
        "challenge_complete",
        mapOf("quiz_id" to quizId, "compatibility_bucket" to scoreBucket(compatibility))
    )

    fun compatibilityShare(quizId: String, compatibility: Int) = log(
        "compatibility_share",
        mapOf("quiz_id" to quizId, "compatibility_bucket" to scoreBucket(compatibility))
    )

    fun appLinkOpen(path: String) = log(
        "app_link_open",
        mapOf("route" to path.substringBefore('?').take(80))
    )

    fun profileShare(archetype: String, completedCount: Int) = log(
        "profile_share",
        mapOf("archetype" to archetype.take(48), "completed_count" to completedCount.coerceAtLeast(0))
    )

    fun profileChallenge(quizId: String, score: Int) = log(
        "profile_challenge",
        mapOf("quiz_id" to quizId, "score_bucket" to scoreBucket(score))
    )

    fun signatureUnlock(signature: SignatureProfileMatch) = log(
        "signature_unlock",
        mapOf(
            "signature_key" to signature.key.name.lowercase(),
            "confidence_bucket" to scoreBucket(signature.confidence),
            "evidence_count" to signature.supportingQuizIds.size
        )
    )

    fun signatureShare(signature: SignatureProfileMatch) = log(
        "signature_share",
        mapOf(
            "signature_key" to signature.key.name.lowercase(),
            "confidence_bucket" to scoreBucket(signature.confidence)
        )
    )

    fun dailyQuestionView(questionId: String) = log("daily_question_view", mapOf("question_id" to questionId))
    fun dailyQuestionVote(questionId: String, option: Int) = log("daily_question_vote", mapOf("question_id" to questionId, "option" to option.coerceIn(0, 1)))
    fun streakContinue(streak: Int) = log("streak_continue", mapOf("streak" to streak.coerceAtLeast(0)))
    fun achievementUnlock(achievementId: String) = log("achievement_unlock", mapOf("achievement_id" to achievementId))

    fun premiumView() = log("premium_view")
    fun purchaseStart(productId: String) = log("purchase_start", mapOf("product_id" to productId))
    fun purchaseCancel(productId: String) = log("purchase_cancel", mapOf("product_id" to productId))
    fun purchaseSuccess(productId: String) = log("purchase_success", mapOf("product_id" to productId))

    fun adImpression(placement: String) = log("ad_impression", mapOf("placement" to placement))

    private fun scoreBucket(value: Int): String = when (value.coerceIn(0, 100)) {
        in 0..19 -> "00_19"
        in 20..39 -> "20_39"
        in 40..59 -> "40_59"
        in 60..79 -> "60_79"
        else -> "80_100"
    }
}
