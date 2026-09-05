package com.whoareyou.app

enum class ScoreChangeDirection {
    HIGHER,
    LOWER,
    SAME
}

data class ScoreChange(
    val previousScore: Int,
    val currentScore: Int,
    val delta: Int,
    val absoluteDelta: Int,
    val direction: ScoreChangeDirection
)

object ScoreChangeEngine {
    fun compare(previousScore: Int?, currentScore: Int): ScoreChange? {
        val previous = previousScore?.coerceIn(0, 100) ?: return null
        val current = currentScore.coerceIn(0, 100)
        val delta = current - previous
        val direction = when {
            delta > 0 -> ScoreChangeDirection.HIGHER
            delta < 0 -> ScoreChangeDirection.LOWER
            else -> ScoreChangeDirection.SAME
        }

        return ScoreChange(
            previousScore = previous,
            currentScore = current,
            delta = delta,
            absoluteDelta = kotlin.math.abs(delta),
            direction = direction
        )
    }
}
