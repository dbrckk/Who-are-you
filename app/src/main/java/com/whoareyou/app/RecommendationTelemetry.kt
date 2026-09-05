package com.whoareyou.app

enum class RecommendationMode(val wireValue: String) {
    GENERIC("generic"),
    SIGNATURE_GUIDED("signature_guided")
}

object RecommendationTelemetry {
    fun mode(signatureGuided: Boolean): RecommendationMode =
        if (signatureGuided) RecommendationMode.SIGNATURE_GUIDED else RecommendationMode.GENERIC

    fun params(quizId: String, signatureGuided: Boolean): Map<String, Any?> = mapOf(
        "quiz_id" to quizId,
        "mode" to mode(signatureGuided).wireValue
    )
}
