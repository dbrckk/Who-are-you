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
    fun resultShare(quizId: String, score: Int) = log("result_share", mapOf("quiz_id" to quizId, "score" to score))
    fun challengeCreate(quizId: String, score: Int) = log("challenge_create", mapOf("quiz_id" to quizId, "score" to score))
    fun premiumView() = log("premium_view")
    fun purchaseSuccess(productId: String) = log("purchase_success", mapOf("product_id" to productId))
    fun adImpression(placement: String) = log("ad_impression", mapOf("placement" to placement))
}
