package com.whoareyou.app

object QuizAnswerTrace {
    fun append(previous: List<Int>, answerIndex: Int): ArrayList<Int> =
        ArrayList<Int>(previous.size + 1).apply {
            addAll(previous)
            add(answerIndex)
        }

    fun complete(
        previous: List<Int>,
        finalAnswerIndex: Int,
        expectedQuestionCount: Int
    ): ArrayList<Int> {
        val precedingAnswerCount = (expectedQuestionCount - 1).coerceAtLeast(0)
        return ArrayList<Int>(expectedQuestionCount.coerceAtLeast(1)).apply {
            addAll(previous.take(precedingAnswerCount))
            add(finalAnswerIndex)
        }
    }
}
