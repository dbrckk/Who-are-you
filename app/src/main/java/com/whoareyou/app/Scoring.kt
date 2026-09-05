package com.whoareyou.app

import kotlin.math.abs

object Scoring {
    fun quizPercent(rawScore: Int, questionCount: Int): Int {
        if (questionCount <= 0) return 0
        val maxScore = questionCount * 3
        return ((rawScore.coerceIn(0, maxScore).toFloat() / maxScore) * 100).toInt().coerceIn(0, 100)
    }

    fun compatibility(firstScore: Int, secondScore: Int): Int {
        val first = firstScore.coerceIn(0, 100)
        val second = secondScore.coerceIn(0, 100)
        return (100 - abs(first - second)).coerceIn(0, 100)
    }
}
