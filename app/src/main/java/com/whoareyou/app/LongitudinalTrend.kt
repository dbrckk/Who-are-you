package com.whoareyou.app

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class LongitudinalTrendKind {
    RISING,
    FALLING,
    STABLE,
    VOLATILE,
    OUTLIER,
    INSUFFICIENT
}

data class LongitudinalTrend(
    val quizId: String,
    val points: List<TimedScore>,
    val slopePerStep: Double,
    val volatility: Double,
    val netChange: Int,
    val kind: LongitudinalTrendKind
)

object LongitudinalTrendEngine {
    fun build(
        quizId: String,
        points: List<TimedScore>
    ): LongitudinalTrend {
        val series = points.takeLast(ScoreHistoryEngine.MAX_SCORES_PER_QUIZ)
        if (series.size < 2) {
            return LongitudinalTrend(
                quizId = quizId,
                points = series,
                slopePerStep = 0.0,
                volatility = 0.0,
                netChange = 0,
                kind = LongitudinalTrendKind.INSUFFICIENT
            )
        }

        val scores = series.map { it.score.toDouble() }
        val slope = linearSlope(scores)
        val volatility = standardDeviation(scores)
        val net = series.last().score - series.first().score
        val outlier = hasIsolatedLastOutlier(scores)

        val kind = when {
            outlier -> LongitudinalTrendKind.OUTLIER
            volatility >= 14.0 -> LongitudinalTrendKind.VOLATILE
            abs(slope) < 2.0 && abs(net) < 8 -> LongitudinalTrendKind.STABLE
            slope >= 2.0 && net >= 8 -> LongitudinalTrendKind.RISING
            slope <= -2.0 && net <= -8 -> LongitudinalTrendKind.FALLING
            else -> LongitudinalTrendKind.STABLE
        }

        return LongitudinalTrend(
            quizId = quizId,
            points = series,
            slopePerStep = slope,
            volatility = volatility,
            netChange = net,
            kind = kind
        )
    }

    private fun linearSlope(values: List<Double>): Double {
        val n = values.size.toDouble()
        val xs = values.indices.map(Int::toDouble)
        val meanX = xs.average()
        val meanY = values.average()
        val numerator = xs.indices.sumOf { index ->
            (xs[index] - meanX) * (values[index] - meanY)
        }
        val denominator = xs.sumOf { (it - meanX).pow(2) }
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    private fun standardDeviation(values: List<Double>): Double {
        val mean = values.average()
        return sqrt(values.sumOf { (it - mean).pow(2) } / values.size)
    }

    private fun hasIsolatedLastOutlier(values: List<Double>): Boolean {
        if (values.size < 4) return false
        val baseline = values.dropLast(1)
        val baselineMean = baseline.average()
        val baselineSd = standardDeviation(baseline)
        val baselineSlope = linearSlope(baseline)
        val last = values.last()

        // A final point is only an outlier when it breaks a relatively stable baseline.
        // If the baseline is already moving strongly, a distant final value may simply
        // continue the existing trend rather than represent isolated evidence.
        if (abs(baselineSlope) >= 3.0) return false

        return abs(last - baselineMean) >= maxOf(18.0, baselineSd * 2.5)
    }
}
