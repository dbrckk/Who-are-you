package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizScreen(
    quiz: Quiz,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit
) {
    var questionIndex by remember(quiz.id) { mutableIntStateOf(0) }
    var score by remember(quiz.id) { mutableIntStateOf(0) }
    val question = quiz.questions[questionIndex]
    val progress = (questionIndex + 1f) / quiz.questions.size

    Column(
        Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(V2Spacing.Screen)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.back),
                color = V2Colors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Text(
                stringResource(R.string.question_progress, questionIndex + 1, quiz.questions.size),
                color = V2Colors.TextSecondary,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = V2Colors.AccentViolet,
            trackColor = V2Colors.Hairline
        )
        Spacer(Modifier.height(20.dp))
        QuizArtwork(quiz, compact = true)
        Spacer(Modifier.height(24.dp))
        Text(quiz.title.uppercase(), color = V2Colors.AccentViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(question.text, color = V2Colors.TextPrimary, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.answers.forEach { answer ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val newScore = score + answer.score
                        if (questionIndex == quiz.questions.lastIndex) {
                            onFinished(Scoring.quizPercent(newScore, quiz.questions.size))
                        } else {
                            score = newScore
                            questionIndex++
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        answer.text,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        color = V2Colors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.no_right_answers),
            color = V2Colors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(10.dp))
    }
}
