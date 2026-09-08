package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChallengeFlow(quiz: Quiz, inviterScore: Int, onClose: () -> Unit) {
    var questionIndex by remember(quiz.id) { mutableIntStateOf(0) }
    var rawScore by remember(quiz.id) { mutableIntStateOf(0) }
    var myScore by remember(quiz.id) { mutableStateOf<Int?>(null) }
    val result = myScore

    if (result == null) {
        ChallengeQuizScreen(
            quiz = quiz,
            questionIndex = questionIndex,
            onAnswer = { answerScore ->
                val updated = rawScore + answerScore
                if (questionIndex == quiz.questions.lastIndex) {
                    myScore = Scoring.quizPercent(updated, quiz.questions.size)
                } else {
                    rawScore = updated
                    questionIndex++
                }
            }
        )
    } else {
        CompatibilityScreen(quiz, inviterScore, result, onClose)
    }
}

@Composable
private fun ChallengeQuizScreen(
    quiz: Quiz,
    questionIndex: Int,
    onAnswer: (Int) -> Unit
) {
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
        Text(stringResource(R.string.challenge_header), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
        Spacer(Modifier.height(8.dp))
        Text(quiz.title, color = V2Colors.TextPrimary, style = V2Type.Question)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.challenge_intro), color = V2Colors.TextSecondary, style = V2Type.Supporting)
        Spacer(Modifier.height(22.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = V2Colors.AccentViolet,
            trackColor = V2Colors.Hairline
        )
        Spacer(Modifier.height(32.dp))
        Text(question.text, color = V2Colors.TextPrimary, style = V2Type.Question)
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.answers.forEach { answer ->
                V2PressableSurface(onClick = { onAnswer(answer.score) }) {
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
            stringResource(R.string.challenge_disclaimer),
            color = V2Colors.TextSecondary,
            style = V2Type.Caption,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun CompatibilityScreen(
    quiz: Quiz,
    inviterScore: Int,
    myScore: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val compatibility = Scoring.compatibility(inviterScore, myScore)
    val label = when {
        compatibility >= 90 -> stringResource(R.string.challenge_match_identical)
        compatibility >= 75 -> stringResource(R.string.challenge_match_strong)
        compatibility >= 55 -> stringResource(R.string.challenge_match_mixed)
        else -> stringResource(R.string.challenge_match_opposite)
    }
    val explanation = when {
        compatibility >= 90 -> stringResource(R.string.challenge_match_identical_copy)
        compatibility >= 75 -> stringResource(R.string.challenge_match_strong_copy)
        compatibility >= 55 -> stringResource(R.string.challenge_match_mixed_copy)
        else -> stringResource(R.string.challenge_match_opposite_copy)
    }

    LaunchedEffect(quiz.id, inviterScore, myScore) {
        ProfileStore.saveMatchResult(context, compatibility)
        AppEvents.challengeComplete(quiz.id, compatibility)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .verticalScroll(rememberScrollState())
            .padding(V2Spacing.Screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.challenge_match_header), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
        Spacer(Modifier.height(14.dp))
        Text("$compatibility%", color = V2Colors.AccentViolet, fontSize = 72.sp, lineHeight = 76.sp, fontWeight = FontWeight.Black)
        Text(label, color = V2Colors.TextPrimary, style = V2Type.SectionTitle, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(quiz.title, color = V2Colors.TextSecondary, style = V2Type.Supporting, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
            shape = RoundedCornerShape(V2Radius.Card),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp)) {
                ScoreRow(stringResource(R.string.challenge_friend), inviterScore, quiz.metricLow, quiz.metricHigh)
                Spacer(Modifier.height(22.dp))
                ScoreRow(stringResource(R.string.challenge_you), myScore, quiz.metricLow, quiz.metricHigh)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(explanation, color = V2Colors.TextPrimary, style = V2Type.Body, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { CompatibilityShare.share(context, quiz.id, quiz.title, inviterScore, myScore, compatibility) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentCyan, contentColor = V2Colors.Ink),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.compatibility_share_button), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { ChallengeShare.share(context, quiz.id, quiz.title, myScore) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentViolet),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.challenge_another_friend), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ScoreRow(label: String, score: Int, low: String, high: String) {
    Text(label, color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
    Spacer(Modifier.height(6.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$score%", color = V2Colors.TextPrimary, style = V2Type.Metric)
        Text(
            if (score >= 50) high else low,
            color = V2Colors.TextSecondary,
            style = V2Type.Caption,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
    Spacer(Modifier.height(7.dp))
    LinearProgressIndicator(
        progress = { score / 100f },
        modifier = Modifier.fillMaxWidth().height(7.dp),
        color = V2Colors.AccentViolet,
        trackColor = V2Colors.Hairline
    )
}

@Composable
fun InvalidChallengeScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.invalid_challenge),
            color = V2Colors.TextPrimary,
            style = V2Type.Question,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.invalid_challenge_copy),
            color = V2Colors.TextSecondary,
            style = V2Type.Body,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
        }
    }
}
