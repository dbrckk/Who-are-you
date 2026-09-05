package com.whoareyou.app

data class ProfileEvolutionSnapshot(
    val trackedCount: Int,
    val totalDimensions: Int,
    val mostChanged: ProfileDimension
) {
    val coveragePercent: Int
        get() = if (totalDimensions <= 0) 0 else ((trackedCount * 100f) / totalDimensions).toInt().coerceIn(0, 100)
}

object ProfileEvolutionSummary {
    fun derive(dimensions: List<ProfileDimension>): ProfileEvolutionSnapshot? {
        val tracked = dimensions.filter { it.scoreChange != null }
        if (tracked.isEmpty()) return null

        val mostChanged = tracked.maxWithOrNull(
            compareBy<ProfileDimension> { it.scoreChange?.absoluteDelta ?: -1 }
                .thenBy { -dimensions.indexOf(it) }
        ) ?: return null

        return ProfileEvolutionSnapshot(
            trackedCount = tracked.size,
            totalDimensions = dimensions.size,
            mostChanged = mostChanged
        )
    }
}
