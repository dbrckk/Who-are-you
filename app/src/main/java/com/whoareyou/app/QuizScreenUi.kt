package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(
    quiz: Quiz,
    questionIndex: Int,
    score: Int,
    onProgress: (questionIndex: Int, score: Int) -> Unit,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit
) {
    val safeQuestionIndex = questionIndex.coerceIn(0, quiz.questions.lastIndex)
    val question = quiz.questions[safeQuestionIndex]
    val progress = (safeQuestionIndex + 1f) / quiz.questions.size
    val scrollState = rememberScrollState()
    val accent = QuizVisuals.accentFor(quiz)
    val companion = QuizVisuals.companionAccentFor(quiz)

    LaunchedEffect(quiz.id, safeQuestionIndex) {
        scrollState.scrollTo(0)
    }

    Column(
        Modifier
            .fillMaxSize()
            .testTag("quiz_question_${safeQuestionIndex + 1}")
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.08f),
                        V2Colors.InkSoft,
                        V2Colors.Ink,
                        companion.copy(alpha = 0.05f),
                        V2Colors.Ink
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(V2Spacing.Screen)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccessibleBackAction(onClick = onBack)
            Text(
                stringResource(R.string.question_progress, safeQuestionIndex + 1, quiz.questions.size),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = accent,
            trackColor = companion.copy(alpha = 0.14f)
        )
        Spacer(Modifier.height(20.dp))
        QuizArtwork(quiz, compact = true)
        Spacer(Modifier.height(24.dp))
        Text(
            quiz.title.uppercase(),
            color = accent,
            style = V2Type.Eyebrow
        )
        Spacer(Modifier.height(12.dp))
        Text(
            question.text,
            color = V2Colors.TextPrimary,
            style = V2Type.Question
        )
        Spacer(Modifier.height(26.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.answers.forEachIndexed { answerIndex, answer ->
                V2PressableSurface(
                    onClick = {
                        val newScore = score + answer.score
                        if (safeQuestionIndex == quiz.questions.lastIndex) {
                            onFinished(Scoring.quizPercent(newScore, quiz.questions.size))
                        } else {
                            onProgress(safeQuestionIndex + 1, newScore)
                        }
                    },
                    modifier = Modifier.testTag("quiz_answer_$answerIndex")
                ) {
                    Text(
                        answer.text,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        color = V2Colors.TextPrimary,
                        style = V2Type.BodyStrong
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.no_right_answers),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
    }
}
