package com.whoareyou.app

enum class ProfileInsightKey {
    STRONG_SIGNATURE,
    BALANCED_CORE,
    BOLD_CONTRAST
}

object ProfileInsights {
    fun derive(dimensions: List<ProfileDimension>, limit: Int = 3): List<ProfileInsightKey> {
        if (dimensions.size < 5 || limit <= 0) return emptyList()

        val scores = dimensions.map { it.score.coerceIn(0, 100) }
        val insights = buildList {
            if (scores.count { kotlin.math.abs(it - 50) >= 30 } >= 2) {
                add(ProfileInsightKey.STRONG_SIGNATURE)
            }
            if (scores.count { it in 40..60 } >= 3) {
                add(ProfileInsightKey.BALANCED_CORE)
            }
            if ((scores.maxOrNull() ?: 50) >= 75 && (scores.minOrNull() ?: 50) <= 25) {
                add(ProfileInsightKey.BOLD_CONTRAST)
            }
        }

        return insights.take(limit.coerceAtLeast(0))
    }
}
