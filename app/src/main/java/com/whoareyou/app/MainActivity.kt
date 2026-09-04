package com.whoareyou.app

import android.app.Activity
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
            MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(background = Ink, surface = Panel, primary = Violet, secondary = Cyan)) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) { WhoAreYouApp() }
            }
        }
    }
}

private enum class Screen { DISCOVER, PROFILE, QUIZ, RESULT }

@Composable
private fun WhoAreYouApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val quizCatalog = remember(context) { QuizRepository.load(context) }
    val storedProfile by ProfileStore.observe(context).collectAsState(initial = StoredProfile())
    val globalProfile = remember(quizCatalog, storedProfile.latestScores) { GlobalProfileEngine.build(quizCatalog, storedProfile.latestScores) }

    var premiumOverride by remember { mutableStateOf(false) }
    val adsRemoved = storedProfile.adsRemoved || premiumOverride
    val billingManager = remember(context) { BillingManager(context) { premiumOverride = it } }
    val adManager = remember(context) { AdManager(context) }

    DisposableEffect(billingManager, adManager) {
        billingManager.start()
        adManager.start()
        onDispose { billingManager.close() }
    }

    var screen by remember { mutableStateOf(Screen.DISCOVER) }
    var selectedQuiz by remember(quizCatalog) { mutableStateOf(quizCatalog.first()) }
    var finalScore by remember { mutableIntStateOf(0) }

    AnimatedContent(targetState = screen, label = "screen") { destination ->
        when (destination) {
            Screen.DISCOVER -> DiscoverScreen(
                quizzes = quizCatalog,
                profile = globalProfile,
                completed = storedProfile.completedQuizIds,
                adsRemoved = adsRemoved,
                onOpenProfile = { screen = Screen.PROFILE },
                onQuizSelected = {
                    selectedQuiz = it
                    AppEvents.testStart(it.id)
                    screen = Screen.QUIZ
                },
                onRemoveAds = { if (!adsRemoved && activity != null) billingManager.launchPurchase(activity) }
            )

            Screen.PROFILE -> GlobalProfileScreen(
                summary = globalProfile,
                catalog = quizCatalog,
                onBack = { screen = Screen.DISCOVER }
            )

            Screen.QUIZ -> QuizScreen(
                quiz = selectedQuiz,
                onBack = { screen = Screen.DISCOVER },
                onFinished = { score ->
                    finalScore = score
                    AppEvents.testComplete(selectedQuiz.id, score)
                    scope.launch { ProfileStore.saveQuizResult(context, selectedQuiz.id, score) }
                    screen = Screen.RESULT
                }
            )

            Screen.RESULT -> ResultScreen(
                quiz = selectedQuiz,
                score = finalScore,
                completedCount = (storedProfile.completedQuizIds + selectedQuiz.id).size,
                totalQuizCount = quizCatalog.size,
                onDone = { adManager.onResultFinished(activity, adsRemoved) { screen = Screen.DISCOVER } },
                onRetry = {
                    AppEvents.testStart(selectedQuiz.id)
                    screen = Screen.QUIZ
                }
            )
        }
    }
}

@Composable
private fun DiscoverScreen(
    quizzes: List<Quiz>,
    profile: GlobalProfileSummary,
    completed: Set<String>,
    adsRemoved: Boolean,
    onOpenProfile: () -> Unit,
    onQuizSelected: (Quiz) -> Unit,
    onRemoveAds: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(28.dp))
            Text("WHO ARE YOU?", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Discover what\nmakes you, you.", color = Color.White, fontSize = 38.sp, lineHeight = 41.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text("Fast personality tests. Visual results. Compare with friends.", color = Muted, fontSize = 16.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(22.dp))
            ProfileProgress(profile, onOpenProfile)
            Spacer(Modifier.height(14.dp))
            RetentionSection()
            Spacer(Modifier.height(10.dp))
            Text("TRENDING TESTS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(quizzes, key = { it.id }) { quiz -> QuizCard(quiz, quiz.id in completed) { onQuizSelected(quiz) } }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text(if (adsRemoved) "LIFETIME UPGRADE ACTIVE" else "REMOVE ADS FOREVER", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(9.dp))
                    Text(if (adsRemoved) "No ads. Ever." else "€1.99 once. No subscription.", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    Text(if (adsRemoved) "Your purchase is stored and restored automatically." else "Keep every test, result, share and friend challenge. Only the ads disappear.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    if (!adsRemoved) {
                        Spacer(Modifier.height(15.dp))
                        Button(onClick = onRemoveAds, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(16.dp)) {
                            Text("REMOVE ADS — €1.99", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileProgress(summary: GlobalProfileSummary, onOpenProfile: () -> Unit) {
    val progress by animateFloatAsState(summary.completionPercent / 100f, label = "profileProgress")
    val snapshot = summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }.take(3)

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("YOUR PROFILE", color = Color.White, fontWeight = FontWeight.Bold)
                Text("${summary.completionPercent}%", color = Violet, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            Text(summary.dominantArchetype.uppercase(), color = if (summary.dimensions.isEmpty()) Muted else Cyan, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
            Spacer(Modifier.height(8.dp))
            Text("${summary.completedCount}/${summary.totalCount} dimensions discovered", color = Muted, fontSize = 12.sp)

            if (snapshot.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("PROFILE SNAPSHOT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                snapshot.forEach { dimension ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(dimension.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(dimension.metricLabel, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${dimension.score}%", color = Violet, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("OPEN FULL PROFILE  →", color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GlobalProfileScreen(summary: GlobalProfileSummary, catalog: List<Quiz>, onBack: () -> Unit) {
    val context = LocalContext.current
    val strongestDimension = summary.dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
    val strongestQuiz = strongestDimension?.let { dimension -> catalog.firstOrNull { it.id == dimension.quizId } }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("← BACK", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.height(26.dp))
            Text("YOUR PROFILE", color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(summary.dominantArchetype.uppercase(), color = Color.White, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("${summary.completedCount}/${summary.totalCount} dimensions • ${summary.completionPercent}% complete", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                AppEvents.profileShare(summary.dominantArchetype, summary.completedCount)
                GlobalProfileShare.share(context, summary)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(18.dp)) {
                Text("SHARE MY PROFILE  ↗", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                enabled = strongestDimension != null && strongestQuiz != null,
                onClick = {
                    val dimension = strongestDimension ?: return@Button
                    val quiz = strongestQuiz ?: return@Button
                    AppEvents.profileChallenge(quiz.id, dimension.score)
                    ChallengeShare.share(context, quiz.id, quiz.title, dimension.score)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PanelSoft),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("COMPARE PROFILE WITH A FRIEND  →", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (strongestDimension == null) "Complete one test to unlock profile comparison."
                else "Starts with your most distinctive dimension: ${strongestDimension.title}.",
                color = Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            Text("ALL DIMENSIONS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (summary.dimensions.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(22.dp)) {
                        Text("PROFILE UNDISCOVERED", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Text("Complete your first test to start building your profile.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        } else {
            items(summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }, key = { it.quizId }) { dimension ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(dimension.title.uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(3.dp))
                                Text(dimension.resultTitle, color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${dimension.score}%", color = Violet, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(progress = { dimension.score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
                        Spacer(Modifier.height(7.dp))
                        Text(dimension.metricLabel, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            Text("For entertainment and self-reflection only — not a psychological diagnosis.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun QuizCard(quiz: Quiz, completed: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp)) {
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
                Card(modifier = Modifier.fillMaxWidth().clickable {
                    val newScore = score + answer.score
                    if (questionIndex == quiz.questions.lastIndex) onFinished(((newScore.toFloat() / (quiz.questions.size * 3)) * 100).toInt())
                    else { score = newScore; questionIndex++ }
                }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
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
private fun ResultScreen(quiz: Quiz, score: Int, completedCount: Int, totalQuizCount: Int, onDone: () -> Unit, onRetry: () -> Unit) {
    val context = LocalContext.current
    val resultTitle = quiz.resultTitleFor(score)
    val description = quiz.resultDescriptionFor(score)

    LazyColumn(modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Text("$completedCount/$totalQuizCount dimensions discovered", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(onClick = {
                AppEvents.resultShare(quiz.id, score)
                ResultShare.share(context, quiz.title, resultTitle, score, quiz.metricLow, quiz.metricHigh, description)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(18.dp)) {
                Text("SHARE MY RESULT  ↗", fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                AppEvents.challengeCreate(quiz.id, score)
                ChallengeShare.share(context, quiz.id, quiz.title, score)
            }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = PanelSoft), shape = RoundedCornerShape(18.dp)) {
                Text("COMPARE WITH A FRIEND  →", fontWeight = FontWeight.Bold)
            }
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