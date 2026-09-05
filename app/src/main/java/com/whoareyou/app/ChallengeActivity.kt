package com.whoareyou.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ChallengeInk = Color(0xFF090A0F)
private val ChallengePanel = Color(0xFF14151D)
private val ChallengePanelSoft = Color(0xFF1B1D27)
private val ChallengeViolet = Color(0xFF9C7BFF)
private val ChallengeCyan = Color(0xFF6EE7F9)
private val ChallengeMuted = Color(0xFFA4A7B5)

class ChallengeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incomingUri = intent?.data
        val incoming = ChallengeShare.parse(incomingUri)
        val quiz = incoming?.let { QuizRepository.find(this, it.quizId) }

        if (incomingUri?.scheme == "https") AppEvents.appLinkOpen(incomingUri.path.orEmpty())
        if (incoming != null) {
            val source = if (incomingUri?.scheme == "https") "https" else "legacy_scheme"
            AppEvents.challengeOpen(incoming.quizId, source)
        }

        setContent {
            MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(background = ChallengeInk, surface = ChallengePanel, primary = ChallengeViolet, secondary = ChallengeCyan)) {
                Surface(modifier = Modifier.fillMaxSize(), color = ChallengeInk) {
                    if (incoming == null || quiz == null) InvalidChallengeScreen { finish() }
                    else ChallengeFlow(quiz, incoming.inviterScore) { finish() }
                }
            }
        }
    }
}

@Composable
private fun ChallengeFlow(quiz: Quiz, inviterScore: Int, onClose: () -> Unit) {
    var questionIndex by remember { mutableIntStateOf(0) }
    var rawScore by remember { mutableIntStateOf(0) }
    var myScore by remember { mutableStateOf<Int?>(null) }
    val result = myScore
    if (result == null) {
        val question = quiz.questions[questionIndex]
        val progress = (questionIndex + 1f) / quiz.questions.size
        Column(Modifier.fillMaxSize().background(ChallengeInk).padding(20.dp)) {
            Text(stringResource(R.string.challenge_header), color = ChallengeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(quiz.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.challenge_intro), color = ChallengeMuted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = ChallengeViolet, trackColor = ChallengePanelSoft)
            Spacer(Modifier.height(32.dp))
            Text(question.text, color = Color.White, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                question.answers.forEach { answer ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        val updated = rawScore + answer.score
                        if (questionIndex == quiz.questions.lastIndex) myScore = Scoring.quizPercent(updated, quiz.questions.size)
                        else { rawScore = updated; questionIndex++ }
                    }, colors = CardDefaults.cardColors(containerColor = ChallengePanel), shape = RoundedCornerShape(18.dp)) {
                        Text(answer.text, modifier = Modifier.padding(18.dp), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.challenge_disclaimer), color = ChallengeMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    } else CompatibilityScreen(quiz, inviterScore, result, onClose)
}

@Composable
private fun CompatibilityScreen(quiz: Quiz, inviterScore: Int, myScore: Int, onClose: () -> Unit) {
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

    Column(modifier = Modifier.fillMaxSize().background(ChallengeInk).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(34.dp))
        Text(stringResource(R.string.challenge_match_header), color = ChallengeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("$compatibility%", color = ChallengeViolet, fontSize = 76.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(quiz.title, color = ChallengeMuted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        Card(colors = CardDefaults.cardColors(containerColor = ChallengePanel), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                ScoreRow(stringResource(R.string.challenge_friend), inviterScore, quiz.metricLow, quiz.metricHigh)
                Spacer(Modifier.height(22.dp))
                ScoreRow(stringResource(R.string.challenge_you), myScore, quiz.metricLow, quiz.metricHigh)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(explanation, color = Color.White, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Button(onClick = { CompatibilityShare.share(context, quiz.id, quiz.title, inviterScore, myScore, compatibility) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = ChallengeCyan), shape = RoundedCornerShape(18.dp)) {
            Text(stringResource(R.string.compatibility_share_button), color = ChallengeInk, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { ChallengeShare.share(context, quiz.id, quiz.title, myScore) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = ChallengeViolet), shape = RoundedCornerShape(18.dp)) {
            Text(stringResource(R.string.challenge_another_friend), fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = ChallengePanelSoft), shape = RoundedCornerShape(18.dp)) {
            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ScoreRow(label: String, score: Int, low: String, high: String) {
    Text(label, color = ChallengeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$score%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(if (score >= 50) high else low, color = ChallengeMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(7.dp))
    LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().height(9.dp), color = ChallengeViolet, trackColor = ChallengePanelSoft)
}

@Composable
private fun InvalidChallengeScreen(onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(ChallengeInk).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.invalid_challenge), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.invalid_challenge_copy), color = ChallengeMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onClose) { Text(stringResource(R.string.close)) }
    }
}