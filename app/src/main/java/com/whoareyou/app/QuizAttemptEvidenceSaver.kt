package com.whoareyou.app

import androidx.compose.runtime.saveable.listSaver

/**
 * Saves the selected answers as Android Bundle-safe integers, without persisting
 * additional profile data. Each triple contains a question, answer and 0..3 score.
 */
internal val quizAttemptEvidenceSaver = listSaver<QuizAttemptEvidence, Int>(
    save = { evidence ->
        evidence.snapshot().flatMap { answer ->
            listOf(answer.questionIndex, answer.answerIndex, (answer.contribution + 3) / 2)
        }
    },
    restore = { saved ->
        QuizAttemptEvidence().apply {
            saved.chunked(3).forEach { entry ->
                if (entry.size == 3 && entry[0] >= 0 && entry[1] >= 0 && entry[2] in 0..3) {
                    record(entry[0], entry[1], entry[2])
                }
            }
        }
    }
)
