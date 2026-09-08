package com.whoareyou.app

import kotlin.math.abs

data class ProfileStrengthNuanceSummary(
    val strongest: List<ProfileDimension>,
    val mostNuanced: ProfileDimension?,
    val contrastPair: Pair<ProfileDimension, ProfileDimension>?
)

object ProfileStrengthNuanceEngine {
    fun build(dimensions: List<ProfileDimension>): ProfileStrengthNuanceSummary {
        val ranked = dimensions.sortedWith(
            compareByDescending<ProfileDimension> { abs(it.score - 50) }
                .thenBy { it.quizId }
        )
        val strongest = ranked.take(2)
        val mostNuanced = dimensions.minWithOrNull(
            compareBy<ProfileDimension> { abs(it.score - 50) }
                .thenBy { it.quizId }
        )
        val low = dimensions.minWithOrNull(compareBy<ProfileDimension> { it.score }.thenBy { it.quizId })
        val high = dimensions.maxWithOrNull(compareBy<ProfileDimension> { it.score }.thenByDescending { it.quizId })
        val contrastPair = if (low != null && high != null && low.quizId != high.quizId) low to high else null

        return ProfileStrengthNuanceSummary(
            strongest = strongest,
            mostNuanced = mostNuanced,
            contrastPair = contrastPair
        )
    }
}
