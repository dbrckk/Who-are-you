package com.whoareyou.app

import kotlin.math.abs

enum class ResultSignalStrength {
    BALANCED,
    CLEAR,
    STRONG
}

enum class ResultDirection {
    LOW,
    HIGH
}

data class ResultInterpretationSummary(
    val score: Int,
    val direction: ResultDirection,
    val strength: ResultSignalStrength,
    val distanceFromNeutral: Int,
    val nuancePercent: Int
)

object ResultInterpretationEngine {
    fun derive(score: Int): ResultInterpretationSummary {
        val safeScore = score.coerceIn(0, 100)
        val distance = abs(safeScore - 50)
        val strength = when {
            distance >= 30 -> ResultSignalStrength.STRONG
            distance >= 15 -> ResultSignalStrength.CLEAR
            else -> ResultSignalStrength.BALANCED
        }
        return ResultInterpretationSummary(
            score = safeScore,
            direction = if (safeScore < 50) ResultDirection.LOW else ResultDirection.HIGH,
            strength = strength,
            distanceFromNeutral = distance,
            nuancePercent = (100 - distance * 2).coerceIn(0, 100)
        )
    }
}
