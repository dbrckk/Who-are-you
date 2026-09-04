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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val ChallengeInk = Color(0xFF090A0F)
private val ChallengePanel = Color(0xFF14151D)
private val ChallengePanelSoft = Color(0xFF1B1D27)
private val ChallengeViolet = Color(0xFF9C7BFF)
private val ChallengeCyan = Color(0xFF6EE7F9)
private val ChallengeMuted = Color(0xFFA4A7B5)

class ChallengeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = ChallengeShare.parse(intent?.data)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = ChallengeInk,
                    surface = ChallengePanel,
                    primary = ChallengeViolet,
                    secondary = ChallengeCyan
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = ChallengeInk) {
                    if (incoming == null) {
                        InvalidChallengeScreen { finish() }
                    } else {
                        val quiz = challengeQuizzes[incoming.quizId]
                        if (quiz == null) InvalidChallengeScreen { finish() }
                        else ChallengeFlow(quiz, incoming.inviterScore) { finish() }
                    }
                }
            }
        }
    }
}

private data class ChallengeQuiz(
    val id: String,
    val title: String,
    val metricLow: String,
    val metricHigh: String,
    val questions: List<Question>
)

@Composable
private fun ChallengeFlow(quiz: ChallengeQuiz, inviterScore: Int, onClose: () -> Unit) {
    var questionIndex by remember { mutableIntStateOf(0) }
    var rawScore by remember { mutableIntStateOf(0) }
    var myScore by remember { mutableStateOf<Int?>(null) }

    val result = myScore
    if (result == null) {
        val question = quiz.questions[questionIndex]
        val progress = (questionIndex + 1f) / quiz.questions.size
        Column(Modifier.fillMaxSize().background(ChallengeInk).padding(20.dp)) {
            Text("FRIEND CHALLENGE", color = ChallengeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(quiz.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Text("Take the same test. Your compatibility appears instantly.", color = ChallengeMuted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = ChallengeViolet,
                trackColor = ChallengePanelSoft
            )
            Spacer(Modifier.height(32.dp))
            Text(question.text, color = Color.White, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                question.answers.forEach { answer ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val updated = rawScore + answer.score
                            if (questionIndex == quiz.questions.lastIndex) {
                                myScore = ((updated.toFloat() / (quiz.questions.size * 3)) * 100).toInt()
                            } else {
                                rawScore = updated
                                questionIndex++
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = ChallengePanel),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(answer.text, modifier = Modifier.padding(18.dp), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("For entertainment and self-reflection only.", color = ChallengeMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    } else {
        CompatibilityScreen(quiz, inviterScore, result, onClose)
    }
}

@Composable
private fun CompatibilityScreen(quiz: ChallengeQuiz, inviterScore: Int, myScore: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    val compatibility = (100 - abs(inviterScore - myScore)).coerceIn(0, 100)
    val label = when {
        compatibility >= 90 -> "ALMOST IDENTICAL"
        compatibility >= 75 -> "STRONG MATCH"
        compatibility >= 55 -> "MIXED MATCH"
        else -> "OPPOSITE ENERGY"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(ChallengeInk).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(34.dp))
        Text("YOUR MATCH", color = ChallengeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("$compatibility%", color = ChallengeViolet, fontSize = 76.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(quiz.title, color = ChallengeMuted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))

        Card(colors = CardDefaults.cardColors(containerColor = ChallengePanel), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                ScoreRow("FRIEND", inviterScore, quiz.metricLow, quiz.metricHigh)
                Spacer(Modifier.height(22.dp))
                ScoreRow("YOU", myScore, quiz.metricLow, quiz.metricHigh)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            when {
                compatibility >= 90 -> "You landed in nearly the same place on this dimension."
                compatibility >= 75 -> "Your answers differ, but your overall pattern is strongly aligned."
                compatibility >= 55 -> "You share some tendencies while diverging on others."
                else -> "You approach this dimension very differently — which can make the comparison more interesting."
            },
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { ChallengeShare.share(context, quiz.id, quiz.title, myScore) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ChallengeViolet),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("CHALLENGE ANOTHER FRIEND  ↗", fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ChallengePanelSoft),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("CLOSE", fontWeight = FontWeight.Bold)
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
    LinearProgressIndicator(
        progress = { score / 100f },
        modifier = Modifier.fillMaxWidth().height(9.dp),
        color = ChallengeViolet,
        trackColor = ChallengePanelSoft
    )
}

@Composable
private fun InvalidChallengeScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(ChallengeInk).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("INVALID CHALLENGE", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Text("This challenge link is incomplete or no longer supported.", color = ChallengeMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onClose) { Text("CLOSE") }
    }
}

private fun cq(text: String, a: String, b: String, c: String, d: String) = Question(
    text,
    listOf(Answer(a, 0), Answer(b, 1), Answer(c, 2), Answer(d, 3))
)

private val challengeQuizzes = mapOf(
    "social_battery" to ChallengeQuiz("social_battery", "Social Battery", "SOLITUDE", "SOCIAL ENERGY", listOf(
        cq("After a full day around people, what do you want most?", "Total silence", "One close person", "A relaxed group", "More plans"),
        cq("A free Saturday appears. Your first instinct?", "Stay home alone", "Keep it low-key", "Meet a few people", "Fill the day with plans"),
        cq("At a party where you know almost nobody...", "I look for an exit", "I stay near one person", "I warm up eventually", "I start conversations"),
        cq("How do group chats usually feel?", "Exhausting", "Easy to ignore", "Fine in moderation", "I keep them alive"),
        cq("After cancelling social plans, you usually feel...", "Relieved", "Mostly fine", "A little disappointed", "Like I missed out")
    )),
    "logic_emotion" to ChallengeQuiz("logic_emotion", "Logic vs Emotion", "LOGIC", "EMOTION", listOf(
        cq("Two choices are equally practical. What breaks the tie?", "The numbers", "Long-term logic", "My instinct", "What feels right"),
        cq("A friend makes an irrational decision. You first...", "Point out the flaw", "Ask for the reasoning", "Try to understand", "Focus on how they feel"),
        cq("When buying something expensive, you trust...", "Comparison data", "Research plus instinct", "My overall impression", "The feeling it gives me"),
        cq("In an argument, what bothers you most?", "Bad logic", "Contradictions", "Being misunderstood", "Emotional coldness"),
        cq("When a plan fails unexpectedly...", "Diagnose the cause", "Recalculate", "Follow my gut", "Check how everyone feels")
    )),
    "overthinker" to ChallengeQuiz("overthinker", "Overthinker", "LET GO", "OVERTHINK", listOf(
        cq("After sending an important message, you...", "Forget about it", "Check once", "Reread it", "Analyze every possible interpretation"),
        cq("Before a simple decision, how many scenarios appear?", "One", "A couple", "Several", "Basically a decision tree"),
        cq("An awkward moment from years ago appears in your mind...", "Almost never", "Rarely", "Sometimes", "Far too easily"),
        cq("Someone replies with just 'ok'. You think...", "Nothing", "They're busy", "Maybe something is off", "What exactly did that 'ok' mean?"),
        cq("At night, your brain is usually...", "Quiet", "Slowing down", "Reviewing the day", "Running twelve tabs at once")
    )),
    "chaos_control" to ChallengeQuiz("chaos_control", "Chaos vs Control", "CONTROL", "CHAOS", listOf(
        cq("A trip is next week. Your itinerary is...", "Already detailed", "Mostly planned", "A few anchors", "What itinerary?"),
        cq("Your workspace usually looks...", "Precisely organized", "Mostly tidy", "Functional chaos", "Like a side quest exploded"),
        cq("When plans suddenly change...", "I hate it", "I need a moment", "I adapt", "That makes it interesting"),
        cq("Deadlines make you...", "Finish early", "Stay on schedule", "Sprint near the end", "Become incredibly powerful at 2 AM"),
        cq("Choose a weekend style.", "Scheduled", "Planned loosely", "Decide that morning", "Follow whatever happens")
    )),
    "risk_taker" to ChallengeQuiz("risk_taker", "Risk Taker", "SECURITY", "RISK", listOf(
        cq("A new opportunity has a big upside but no guarantee. You...", "Pass", "Research for a long time", "Take a measured shot", "Jump in"),
        cq("Trying something with a real chance of public failure feels...", "Not worth it", "Uncomfortable", "Acceptable", "Exciting"),
        cq("With spare money, you prefer...", "Protect it", "Mostly safe options", "A balanced mix", "High-upside bets"),
        cq("You have 70% of the information needed. Do you act?", "No", "Usually wait", "Often yes", "Definitely"),
        cq("Which sounds more painful?", "Losing what I have", "Making a bad call", "Missing a chance", "Never finding out")
    ))
)
