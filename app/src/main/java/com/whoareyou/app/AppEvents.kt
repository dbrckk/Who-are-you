package com.whoareyou.app

import android.util.Log

object AppEvents {
    private const val tag = "WhoAreYouEvents"

    fun log(name: String, params: Map<String, Any?> = emptyMap()) {
        val payload = params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.d(tag, if (payload.isBlank()) name else "$name | $payload")
    }

    fun testStart(quizId: String) = log("test_start", mapOf("quiz_id" to quizId))
    fun testComplete(quizId: String, score: Int) = log("test_complete", mapOf("quiz_id" to quizId, "score" to score))
    fun resultShare(quizId: String, score: Int? = null) = log(
        "result_share",
        buildMap {
            put("quiz_id", quizId)
            if (score != null) put("score", score)
        }
    )
    fun challengeCreate(quizId: String, score: Int? = null) = log(
        "challenge_create",
        buildMap {
            put("quiz_id", quizId)
            if (score != null) put("score", score)
        }
    )
    fun challengeOpen(quizId: String, source: String) = log(
        "challenge_open",
        mapOf("quiz_id" to quizId, "source" to source)
    )
    fun appLinkOpen(path: String) = log("app_link_open", mapOf("path" to path))
    fun profileShare(archetype: String, completedCount: Int) = log(
        "profile_share",
        mapOf("archetype" to archetype, "completed_count" to completedCount)
    )
    fun profileChallenge(quizId: String, score: Int) = log(
        "profile_challenge",
        mapOf("quiz_id" to quizId, "score" to score)
    )
    fun premiumView() = log("premium_view")
    fun purchaseSuccess(productId: String) = log("purchase_success", mapOf("product_id" to productId))
    fun adImpression(placement: String) = log("ad_impression", mapOf("placement" to placement))
}
