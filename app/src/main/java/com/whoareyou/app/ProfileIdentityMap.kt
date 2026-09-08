package com.whoareyou.app

import kotlin.math.abs

data class ProfileIdentityMapSummary(
    val axes: List<ProfileDimension>,
    val dominantSignalPercent: Int,
    val contrast: Int,
    val balancedAxes: Int
)

object ProfileIdentityMapEngine {
    fun build(dimensions: List<ProfileDimension>, maxAxes: Int = 6): ProfileIdentityMapSummary {
        val axes = dimensions
            .sortedByDescending { abs(it.score - 50) }
            .take(maxAxes.coerceAtLeast(1))

        val dominantSignal = axes.firstOrNull()
            ?.let { abs(it.score - 50) * 2 }
            ?.coerceIn(0, 100)
            ?: 0

        val contrast = if (axes.isEmpty()) 0 else axes.maxOf { it.score } - axes.minOf { it.score }
        val balanced = axes.count { abs(it.score - 50) <= 12 }

        return ProfileIdentityMapSummary(
            axes = axes,
            dominantSignalPercent = dominantSignal,
            contrast = contrast,
            balancedAxes = balanced
        )
    }
}
