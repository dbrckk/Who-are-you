package com.whoareyou.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateListOf
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

private val Ink = Color(0xFF090A0F)
private val Panel = Color(0xFF14151D)
private val PanelSoft = Color(0xFF1B1D27)
private val Violet = Color(0xFF9C7BFF)
private val Cyan = Color(0xFF6EE7F9)
private val Muted = Color(0xFFA4A7B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = Ink,
                    surface = Panel,
                    primary = Violet,
                    secondary = Cyan
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    WhoAreYouApp()
                }
            }
        }
    }
}

data class Answer(val text: String, val score: Int)
data class Question(val text: String, val answers: List<Answer>)
data class Quiz(
    val id: String,
    val title: String,
    val hook: String,
    val time: String,
    val accent: String,
    val lowTitle: String,
    val midTitle: String,
    val highTitle: String,
    val lowDescription: String,
    val midDescription: String,
    val highDescription: String,
    val metricLow: String,
    val metricHigh: String,
    val questions: List<Question>
)

private enum class Screen { DISCOVER, QUIZ, RESULT }

@Composable
private fun WhoAreYouApp() {
    var screen by remember { mutableStateOf(Screen.DISCOVER) }
    var selectedQuiz by remember { mutableStateOf(quizzes.first()) }
    var finalScore by remember { mutableIntStateOf(0) }
    val completed = remember { mutableStateListOf<String>() }

    AnimatedContent(targetState = screen, label = "screen") { destination ->
        when (destination) {
            Screen.DISCOVER -> DiscoverScreen(completed) {
                selectedQuiz = it
                screen = Screen.QUIZ
            }
            Screen.QUIZ -> QuizScreen(
                quiz = selectedQuiz,
                onBack = { screen = Screen.DISCOVER },
                onFinished = { score ->
                    finalScore = score
                    if (selectedQuiz.id !in completed) completed.add(selectedQuiz.id)
                    screen = Screen.RESULT
                }
            )
            Screen.RESULT -> ResultScreen(
                quiz = selectedQuiz,
                score = finalScore,
                completedCount = completed.size,
                onDone = { screen = Screen.DISCOVER },
                onRetry = { screen = Screen.QUIZ }
            )
        }
    }
}

@Composable
private fun DiscoverScreen(completed: List<String>, onQuizSelected: (Quiz) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(28.dp))
            Text("WHO ARE YOU?", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Discover what\nmakes you, you.", color = Color.White, fontSize = 38.sp, lineHeight = 41.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text("Fast personality tests. Visual results. Compare with friends.", color = Muted, fontSize = 16.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(22.dp))
            ProfileProgress(completed.size)
            Spacer(Modifier.height(10.dp))
            Text("TRENDING TESTS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(quizzes) { quiz -> QuizCard(quiz, quiz.id in completed) { onQuizSelected(quiz) } }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text("DAILY QUESTION", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("Would you rather know when you die or how you die?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Global votes + friend sharing coming next", color = Muted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileProgress(completedCount: Int) {
    val progress by animateFloatAsState((completedCount / quizzes.size.toFloat()).coerceIn(0f, 1f), label = "profileProgress")
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("YOUR PROFILE", color = Color.White, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}%", color = Violet, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
            Spacer(Modifier.height(8.dp))
            Text("$completedCount/${quizzes.size} dimensions discovered", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun QuizCard(quiz: Quiz, completed: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(quiz.accent, fontSize = 26.sp)
                Text(if (completed) "DONE" else quiz.time, color = if (completed) Cyan else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text(quiz.title.uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text(quiz.hook, color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Text(if (completed) "TAKE AGAIN  →" else "START  →", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuizScreen(quiz: Quiz, onBack: () -> Unit, onFinished: (Int) -> Unit) {
    var questionIndex by remember(quiz.id) { mutableIntStateOf(0) }
    var score by remember(quiz.id) { mutableIntStateOf(0) }
    val question = quiz.questions[questionIndex]
    val progress = (questionIndex + 1f) / quiz.questions.size

    Column(Modifier.fillMaxSize().background(Ink).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("← BACK", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            Text("${questionIndex + 1} / ${quiz.questions.size}", color = Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
        Spacer(Modifier.height(38.dp))
        Text(quiz.title.uppercase(), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(question.text, color = Color.White, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.answers.forEach { answer ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val newScore = score + answer.score
                        if (questionIndex == quiz.questions.lastIndex) {
                            onFinished(((newScore.toFloat() / (quiz.questions.size * 3)) * 100).toInt())
                        } else {
                            score = newScore
                            questionIndex++
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(answer.text, modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text("No right answers. Pick what feels most like you.", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ResultScreen(quiz: Quiz, score: Int, completedCount: Int, onDone: () -> Unit, onRetry: () -> Unit) {
    val context = LocalContext.current
    val resultTitle = when {
        score < 35 -> quiz.lowTitle
        score < 70 -> quiz.midTitle
        else -> quiz.highTitle
    }
    val description = when {
        score < 35 -> quiz.lowDescription
        score < 70 -> quiz.midDescription
        else -> quiz.highDescription
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Text("YOUR RESULT", color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Text(quiz.accent, fontSize = 44.sp)
            Spacer(Modifier.height(12.dp))
            Text(resultTitle.uppercase(), color = Color.White, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(quiz.title, color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(26.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score%", color = Violet, fontSize = 54.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(quiz.metricLow, color = Muted, fontSize = 12.sp)
                        Text(quiz.metricHigh, color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().height(10.dp), color = Violet, trackColor = PanelSoft)
                    Spacer(Modifier.height(20.dp))
                    Text(description, color = Color.White, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = PanelSoft), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("PROFILE PROGRESS", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("$completedCount/${quizzes.size} dimensions discovered", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { ResultShare.share(context, quiz.title, resultTitle, score, quiz.metricLow, quiz.metricHigh, description) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                shape = RoundedCornerShape(18.dp)
            ) { Text("SHARE MY RESULT  ↗", fontWeight = FontWeight.Black) }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { ChallengeShare.share(context, quiz.id, quiz.title, score) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PanelSoft),
                shape = RoundedCornerShape(18.dp)
            ) { Text("COMPARE WITH A FRIEND  →", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            Text("Your friend takes the same test and gets an instant match score", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRetry, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PanelSoft)) { Text("RETRY") }
                Button(onClick = onDone, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink)) { Text("DONE", fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(30.dp))
            Text("For entertainment and self-reflection only — not a psychological diagnosis.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
        }
    }
}

private fun q(text: String, a: String, b: String, c: String, d: String) = Question(text, listOf(Answer(a, 0), Answer(b, 1), Answer(c, 2), Answer(d, 3)))

private val quizzes = listOf(
    Quiz(
        "social_battery", "Social Battery", "How much people can you actually handle?", "45 SEC", "◉",
        "The Quiet Core", "The Selective Social", "The Human Charger",
        "Your energy tends to recover in quiet spaces. You can enjoy people, but too much social input drains you quickly.",
        "You enjoy connection when the context feels right. Your social energy depends heavily on the people, place and mood.",
        "Interaction often gives you momentum. You tend to recharge through people, activity and shared experiences.",
        "SOLITUDE", "SOCIAL ENERGY",
        listOf(
            q("After a full day around people, what do you want most?", "Total silence", "One close person", "A relaxed group", "More plans"),
            q("A free Saturday appears. Your first instinct?", "Stay home alone", "Keep it low-key", "Meet a few people", "Fill the day with plans"),
            q("At a party where you know almost nobody...", "I look for an exit", "I stay near one person", "I warm up eventually", "I start conversations"),
            q("How do group chats usually feel?", "Exhausting", "Easy to ignore", "Fine in moderation", "I keep them alive"),
            q("After cancelling social plans, you usually feel...", "Relieved", "Mostly fine", "A little disappointed", "Like I missed out")
        )
    ),
    Quiz(
        "logic_emotion", "Logic vs Emotion", "What really drives your decisions?", "50 SEC", "◇",
        "The Rational Mind", "The Integrator", "The Intuitive Heart",
        "You tend to trust evidence, consistency and structure before feelings. Emotions matter, but they rarely get the final vote.",
        "You naturally combine analysis and feeling. You can switch between evidence and intuition depending on what is at stake.",
        "Your internal sense of what feels right strongly shapes your choices. Human impact often matters more than perfect logic.",
        "LOGIC", "EMOTION",
        listOf(
            q("Two choices are equally practical. What breaks the tie?", "The numbers", "Long-term logic", "My instinct", "What feels right"),
            q("A friend makes an irrational decision. You first...", "Point out the flaw", "Ask for the reasoning", "Try to understand", "Focus on how they feel"),
            q("When buying something expensive, you trust...", "Comparison data", "Research plus instinct", "My overall impression", "The feeling it gives me"),
            q("In an argument, what bothers you most?", "Bad logic", "Contradictions", "Being misunderstood", "Emotional coldness"),
            q("When a plan fails unexpectedly...", "Diagnose the cause", "Recalculate", "Follow my gut", "Check how everyone feels")
        )
    ),
    Quiz(
        "overthinker", "Overthinker", "Does your brain ever actually switch off?", "40 SEC", "∞",
        "The Clear Decider", "The Analyzer", "The Infinite Loop",
        "You usually process what matters and move forward. Uncertainty may bother you, but it rarely keeps your mind trapped for long.",
        "You think deeply and often replay important situations. Analysis helps you, though it can occasionally become mental noise.",
        "Your mind generates branches, alternatives and second-order consequences almost automatically. Switching off can be harder than deciding.",
        "LET GO", "OVERTHINK",
        listOf(
            q("After sending an important message, you...", "Forget about it", "Check once", "Reread it", "Analyze every possible interpretation"),
            q("Before a simple decision, how many scenarios appear?", "One", "A couple", "Several", "Basically a decision tree"),
            q("An awkward moment from years ago appears in your mind...", "Almost never", "Rarely", "Sometimes", "Far too easily"),
            q("Someone replies with just 'ok'. You think...", "Nothing", "They're busy", "Maybe something is off", "What exactly did that 'ok' mean?"),
            q("At night, your brain is usually...", "Quiet", "Slowing down", "Reviewing the day", "Running twelve tabs at once")
        )
    ),
    Quiz(
        "chaos_control", "Chaos vs Control", "Planner, improviser, or beautifully unpredictable?", "45 SEC", "✦",
        "The Architect", "The Adaptive Planner", "The Chaos Surfer",
        "Structure gives you freedom. You prefer knowing what comes next and reducing avoidable surprises before they happen.",
        "You like having a framework without becoming trapped by it. A plan is useful, but you can abandon it when reality changes.",
        "You are comfortable moving without a complete map. Improvisation, novelty and last-minute decisions can feel more alive than rigid plans.",
        "CONTROL", "CHAOS",
        listOf(
            q("A trip is next week. Your itinerary is...", "Already detailed", "Mostly planned", "A few anchors", "What itinerary?"),
            q("Your workspace usually looks...", "Precisely organized", "Mostly tidy", "Functional chaos", "Like a side quest exploded"),
            q("When plans suddenly change...", "I hate it", "I need a moment", "I adapt", "That makes it interesting"),
            q("Deadlines make you...", "Finish early", "Stay on schedule", "Sprint near the end", "Become incredibly powerful at 2 AM"),
            q("Choose a weekend style.", "Scheduled", "Planned loosely", "Decide that morning", "Follow whatever happens")
        )
    ),
    Quiz(
        "risk_taker", "Risk Taker", "How far outside certainty will you go?", "45 SEC", "△",
        "The Strategist", "The Calculated Risk", "The Edge Seeker",
        "You prefer asymmetric bets: protect the downside first, then move. Security and predictability carry real value for you.",
        "You will take meaningful risks when the upside is justified. You are neither reckless nor automatically conservative.",
        "Uncertainty can feel energizing rather than threatening. When something matters, you are often willing to move before certainty arrives.",
        "SECURITY", "RISK",
        listOf(
            q("A new opportunity has a big upside but no guarantee. You...", "Pass", "Research for a long time", "Take a measured shot", "Jump in"),
            q("Trying something with a real chance of public failure feels...", "Not worth it", "Uncomfortable", "Acceptable", "Exciting"),
            q("With spare money, you prefer...", "Protect it", "Mostly safe options", "A balanced mix", "High-upside bets"),
            q("You have 70% of the information needed. Do you act?", "No", "Usually wait", "Often yes", "Definitely"),
            q("Which sounds more painful?", "Losing what I have", "Making a bad call", "Missing a chance", "Never finding out")
        )
    )
)
