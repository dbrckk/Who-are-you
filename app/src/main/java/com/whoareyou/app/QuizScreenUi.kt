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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(
    quiz: Quiz,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit
) {
    var questionIndex by rememberSaveable(quiz.id) { mutableIntStateOf(0) }
    var score by rememberSaveable(quiz.id) { mutableIntStateOf(0) }
    val question = quiz.questions[questionIndex]
    val progress = (questionIndex + 1f) / quiz.questions.size
    val scrollState = rememberScrollState()

    LaunchedEffect(quiz.id, questionIndex) {
        scrollState.scrollTo(0)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
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
                stringResource(R.string.question_progress, questionIndex + 1, quiz.questions.size),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("quiz_question_${questionIndex + 1}")
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = V2Colors.AccentViolet,
            trackColor = V2Colors.Hairline
        )
        Spacer(Modifier.height(20.dp))
        QuizArtwork(quiz, compact = true)
        Spacer(Modifier.height(24.dp))
        Text(
            quiz.title.uppercase(),
            color = V2Colors.AccentViolet,
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
                        if (questionIndex == quiz.questions.lastIndex) {
                            onFinished(Scoring.quizPercent(newScore, quiz.questions.size))
                        } else {
                            score = newScore
                            questionIndex++
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
