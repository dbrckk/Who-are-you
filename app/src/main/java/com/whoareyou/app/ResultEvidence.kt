package com.whoareyou.app

import kotlin.math.abs

data class AnswerContribution(
    val questionIndex: Int,
    val answerIndex: Int,
    val contribution: Int
)

data class ResultEvidence(
    val questionIndex: Int,
    val questionText: String,
    val answerText: String,
    val contribution: Int
)

object ResultEvidenceEngine {
    fun derive(
        questions: List<Question>,
        contributions: List<AnswerContribution>
    ): List<ResultEvidence> = contributions
        .mapNotNull { item ->
            val question = questions.getOrNull(item.questionIndex) ?: return@mapNotNull null
            val answer = question.answers.getOrNull(item.answerIndex) ?: return@mapNotNull null
            ResultEvidence(
                questionIndex = item.questionIndex,
                questionText = question.text,
                answerText = answer.text,
                contribution = item.contribution
            )
        }
        .sortedWith(
            compareByDescending<ResultEvidence> { abs(it.contribution) }
                .thenBy { it.questionIndex }
        )
        .take(3)
}
