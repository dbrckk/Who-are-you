package com.whoareyou.app

class QuizAttemptEvidence {
    private val contributions = linkedMapOf<Int, AnswerContribution>()

    fun record(questionIndex: Int, answerIndex: Int, score: Int) {
        contributions[questionIndex] = AnswerContribution(
            questionIndex = questionIndex,
            answerIndex = answerIndex,
            contribution = score
        )
    }

    fun snapshot(): List<AnswerContribution> =
        contributions.values.sortedBy { it.questionIndex }

    fun clear() {
        contributions.clear()
    }
}
