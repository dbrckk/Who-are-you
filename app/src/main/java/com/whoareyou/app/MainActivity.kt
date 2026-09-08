package com.whoareyou.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.map
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
    val storedProfileFlow = remember(context) {
        ProfileStore.observe(context).map<StoredProfile, StoredProfile?> { it }
    }
    val storedProfileState by storedProfileFlow.collectAsState(initial = null)
    val storedProfile = storedProfileState ?: return
    val globalProfile = remember(quizCatalog, storedProfile.latestScores, storedProfile.previousScores) {
        GlobalProfileEngine.build(quizCatalog, storedProfile.latestScores, storedProfile.previousScores)
    }

    var premiumOverride by remember { mutableStateOf(false) }
    val adsRemoved = storedProfile.adsRemoved || premiumOverride
    val billingManager = remember(context) {
        BillingManager(
            context = context,
            onPremiumChanged = { premiumOverride = it },
            onPriceChanged = BillingPriceState::update
        )
    }
    val adManager = remember(context) { AdManager(context) }

    DisposableEffect(billingManager, adManager) {
        billingManager.start()
        adManager.start()
        onDispose { billingManager.close() }
    }

    if (!storedProfile.onboardingComplete) {
        OnboardingScreen(
            onStart = { scope.launch { ProfileStore.setOnboardingComplete(context) } }
        )
        return
    }

    if (!AppNavigation.hasUsableCatalog(quizCatalog.size)) {
        CatalogUnavailableScreen()
        return
    }

    var screen by remember { mutableStateOf(Screen.DISCOVER) }
    var selectedQuiz by remember(quizCatalog) { mutableStateOf(quizCatalog.first()) }
    var finalScore by remember { mutableIntStateOf(0) }
    var previousScoreForAttempt by remember { mutableStateOf<Int?>(null) }

    BackHandler(enabled = screen != Screen.DISCOVER) {
        screen = Screen.DISCOVER
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            label = "screen",
            modifier = Modifier.fillMaxSize()
        ) { destination ->
            when (destination) {
                Screen.DISCOVER -> DiscoverScreen(
                    quizzes = quizCatalog,
                    profile = globalProfile,
                    storedProfile = storedProfile,
                    completed = storedProfile.completedQuizIds,
                    adsRemoved = adsRemoved,
                    onOpenProfile = { screen = Screen.PROFILE },
                    onQuizSelected = {
                        previousScoreForAttempt = storedProfile.latestScores[it.id]
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
                    previousScore = previousScoreForAttempt,
                    completedCount = (storedProfile.completedQuizIds + selectedQuiz.id).size,
                    totalQuizCount = quizCatalog.size,
                    onDone = { adManager.onResultFinished(activity, adsRemoved) { screen = Screen.DISCOVER } },
                    onRetry = {
                        previousScoreForAttempt = finalScore
                        AppEvents.testStart(selectedQuiz.id)
                        screen = Screen.QUIZ
                    }
                )
            }
        }

        if (screen == Screen.DISCOVER || screen == Screen.PROFILE) {
            PremiumAppShellBar(
                selected = if (screen == Screen.PROFILE) AppShellTab.PROFILE else AppShellTab.DISCOVER,
                onSelect = { tab ->
                    screen = when (tab) {
                        AppShellTab.DISCOVER -> Screen.DISCOVER
                        AppShellTab.PROFILE -> Screen.PROFILE
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CatalogUnavailableScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.catalog_unavailable_title), color = Color.White, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.catalog_unavailable_body), color = Muted, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OnboardingScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.onboarding_title), color = Color.White, fontSize = 58.sp, lineHeight = 56.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.onboarding_subtitle), color = Violet, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.onboarding_body), color = Muted, fontSize = 16.sp, lineHeight = 24.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.onboarding_start), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.onboarding_no_account), color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DiscoverScreen(
    quizzes: List<Quiz>,
    profile: GlobalProfileSummary,
    storedProfile: StoredProfile,
    completed: Set<String>,
    adsRemoved: Boolean,
    onOpenProfile: () -> Unit,
    onQuizSelected: (Quiz) -> Unit,
    onRemoveAds: () -> Unit
) {
    val premiumPrice = BillingPriceState.displayPrice
    val allCompleted = quizzes.isNotEmpty() && completed.containsAll(quizzes.map { it.id })
    val unfinishedIds = remember(quizzes, completed) { quizzes.asSequence().map { it.id }.filterNot { it in completed }.toSet() }
    val signatureRecommendation = remember(storedProfile.latestScores, unfinishedIds) {
        SignatureProfiles.recommendNext(storedProfile.latestScores, unfinishedIds)
    }
    val retakeRecommendation = remember(quizzes, allCompleted, storedProfile.latestScores, storedProfile.previousScores) {
        if (allCompleted) {
            RetakeRecommendationEngine.recommend(
                quizIds = quizzes.map { it.id },
                latestScores = storedProfile.latestScores,
                previousScores = storedProfile.previousScores
            )
        } else null
    }
    val fallbackQuiz = if (allCompleted) {
        retakeRecommendation?.let { recommendation -> quizzes.firstOrNull { it.id == recommendation.quizId } }
            ?: quizzes.firstOrNull()
    } else {
        quizzes.firstOrNull { it.id !in completed } ?: quizzes.firstOrNull()
    }
    val recommendedQuiz = if (allCompleted) {
        fallbackQuiz
    } else {
        signatureRecommendation
            ?.let { recommendation -> quizzes.firstOrNull { it.id == recommendation.quizId && it.id !in completed } }
            ?: fallbackQuiz
    }
    val signatureGuided = !allCompleted && recommendedQuiz != null && signatureRecommendation?.quizId == recommendedQuiz.id
    val retakeReason = retakeRecommendation
        ?.takeIf { recommendation -> allCompleted && recommendedQuiz?.id == recommendation.quizId }
        ?.reason
    val signaturePathProgress = remember(storedProfile.latestScores) {
        SignatureProfiles.pathProgress(storedProfile.latestScores)
    }
    val orderedQuizzes = remember(quizzes, completed) { quizzes.sortedBy { it.id in completed } }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.app_name), color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.discover_headline), color = Color.White, fontSize = 38.sp, lineHeight = 41.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.discover_subtitle), color = Muted, fontSize = 16.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(22.dp))
            ProfileProgress(profile, onOpenProfile)
            if (signatureGuided && signaturePathProgress != null && !allCompleted) {
                Spacer(Modifier.height(14.dp))
                SignaturePathProgressCard(signaturePathProgress)
            }
            if (recommendedQuiz != null) {
                Spacer(Modifier.height(14.dp))
                RecommendedQuizCard(recommendedQuiz, allCompleted, signatureGuided, retakeReason) { onQuizSelected(recommendedQuiz) }
            }
            Spacer(Modifier.height(14.dp))
            RetentionSection()
            Spacer(Modifier.height(14.dp))
            SocialStatsCard(storedProfile)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.trending_tests), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(orderedQuizzes, key = { it.id }) { quiz -> QuizCard(quiz, quiz.id in completed) { onQuizSelected(quiz) } }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text(stringResource(if (adsRemoved) R.string.lifetime_upgrade_active else R.string.remove_ads_forever), color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(9.dp))
                    Text(if (adsRemoved) stringResource(R.string.no_ads_ever) else stringResource(R.string.premium_once_no_subscription, premiumPrice), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    Text(stringResource(if (adsRemoved) R.string.premium_restore_copy else R.string.premium_copy), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    if (!adsRemoved) {
                        Spacer(Modifier.height(15.dp))
                        Button(onClick = onRemoveAds, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.remove_ads_button, premiumPrice), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SignaturePathProgressCard(progress: SignaturePathProgress) {
    val animatedProgress by animateFloatAsState(progress.percent / 100f, label = "signaturePathProgress")
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.profile_path), color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${progress.percent}%", color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(9.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = Violet,
                trackColor = PanelSoft
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.profile_path_progress, progress.completedRequirements, progress.totalRequirements),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Text(stringResource(R.string.profile_path_reason), color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun RecommendedQuizCard(
    quiz: Quiz,
    allCompleted: Boolean,
    signatureGuided: Boolean,
    retakeReason: RetakeRecommendationReason?,
    onClick: () -> Unit
) {
    DisposableEffect(quiz.id, signatureGuided) {
        AppEvents.recommendationView(quiz.id, signatureGuided)
        onDispose { }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            AppEvents.recommendationStart(quiz.id, signatureGuided)
            onClick()
        },
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            QuizArtwork(quiz)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(
                        when {
                            retakeReason != null -> R.string.recommended_retake_label
                            signatureGuided -> R.string.recommended_signature_label
                            else -> R.string.recommended_for_you
                        }
                    ),
                    color = Cyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(quiz.accent, fontSize = 22.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(quiz.title.uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(
                    when {
                        retakeReason == RetakeRecommendationReason.START_TRACKING -> R.string.recommended_retake_start_reason
                        retakeReason == RetakeRecommendationReason.RECHECK_CHANGE -> R.string.recommended_retake_change_reason
                        allCompleted -> R.string.recommended_all_done
                        signatureGuided -> R.string.recommended_signature_reason
                        else -> R.string.recommended_reason
                    }
                ),
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.continue_discovering), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SocialStatsCard(profile: StoredProfile) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.your_social_stats), color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (profile.matchCount == 0) {
                Text(stringResource(R.string.social_no_matches), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SocialStat(stringResource(R.string.social_comparisons), profile.matchCount.toString(), Modifier.weight(1f))
                    SocialStat(stringResource(R.string.social_best_match), "${profile.bestMatchPercent ?: 0}%", Modifier.weight(1f))
                    SocialStat(stringResource(R.string.social_most_different), "${profile.lowestMatchPercent ?: 0}%", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SocialStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, color = Violet, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun ProfileProgress(summary: GlobalProfileSummary, onOpenProfile: () -> Unit) {
    val progress by animateFloatAsState(summary.completionPercent / 100f, label = "profileProgress")
    val snapshot = summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }.take(3)

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.your_profile), color = Color.White, fontWeight = FontWeight.Bold)
                Text("${summary.completionPercent}%", color = Violet, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            Text(summary.dominantArchetype.uppercase(), color = if (summary.dimensions.isEmpty()) Muted else Cyan, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.profile_dimensions_discovered, summary.completedCount, summary.totalCount), color = Muted, fontSize = 12.sp)

            if (snapshot.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.profile_snapshot), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Text(stringResource(R.string.open_full_profile), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Text(stringResource(R.string.back), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.height(26.dp))
            Text(stringResource(R.string.your_profile), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(summary.dominantArchetype.uppercase(), color = Color.White, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("${summary.completedCount}/${summary.totalCount} dimensions • ${summary.completionPercent}%", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                AppEvents.profileShare(summary.dominantArchetype, summary.completedCount)
                GlobalProfileShare.share(context, summary)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(18.dp)) {
                Text(stringResource(R.string.share_my_profile), fontWeight = FontWeight.Black)
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
                Text(stringResource(R.string.compare_profile_friend), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (strongestDimension == null) stringResource(R.string.complete_test_unlock_compare)
                else stringResource(R.string.starts_with_dimension, strongestDimension.title),
                color = Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (summary.dimensions.size >= 5) {
                Spacer(Modifier.height(18.dp))
                ProfileInsightCards(summary)
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.all_dimensions), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (summary.dimensions.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(22.dp)) {
                        Text(stringResource(R.string.profile_undiscovered), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Text(stringResource(R.string.complete_first_test_profile), color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
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
                        dimension.scoreChange?.let { change ->
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.profile_evolution), color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.profile_evolution_values, change.previousScore, change.currentScore),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                when (change.direction) {
                                    ScoreChangeDirection.HIGHER -> stringResource(R.string.profile_evolution_higher, change.absoluteDelta)
                                    ScoreChangeDirection.LOWER -> stringResource(R.string.profile_evolution_lower, change.absoluteDelta)
                                    ScoreChangeDirection.SAME -> stringResource(R.string.profile_evolution_same)
                                },
                                color = Muted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.disclaimer), color = Muted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun QuizCard(quiz: Quiz, completed: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp)) {
        Column {
            QuizArtwork(quiz)
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(quiz.accent, fontSize = 26.sp)
                    Text(if (completed) stringResource(R.string.done) else quiz.time, color = if (completed) Cyan else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                Text(quiz.title.uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Text(quiz.hook, color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(if (completed) R.string.take_again else R.string.start), color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
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
            Text(stringResource(R.string.back), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            Text(stringResource(R.string.question_progress, questionIndex + 1, quiz.questions.size), color = Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = PanelSoft)
        Spacer(Modifier.height(20.dp))
        QuizArtwork(quiz, compact = true)
        Spacer(Modifier.height(24.dp))
        Text(quiz.title.uppercase(), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(question.text, color = Color.White, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.answers.forEach { answer ->
                Card(modifier = Modifier.fillMaxWidth().clickable {
                    val newScore = score + answer.score
                    if (questionIndex == quiz.questions.lastIndex) onFinished(Scoring.quizPercent(newScore, quiz.questions.size))
                    else { score = newScore; questionIndex++ }
                }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                    Text(answer.text, modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.no_right_answers), color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ResultScreen(
    quiz: Quiz,
    score: Int,
    previousScore: Int?,
    completedCount: Int,
    totalQuizCount: Int,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val resultTitle = quiz.resultTitleFor(score)
    val description = quiz.resultDescriptionFor(score)
    val scoreChange = remember(previousScore, score) { ScoreChangeEngine.compare(previousScore, score) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.your_result), color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            QuizArtwork(quiz)
            Spacer(Modifier.height(20.dp))
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

            if (scoreChange != null) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = PanelSoft), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.retake_change_title), color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(7.dp))
                        Text(
                            stringResource(R.string.retake_change_values, scoreChange.previousScore, scoreChange.currentScore),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when (scoreChange.direction) {
                                ScoreChangeDirection.HIGHER -> stringResource(R.string.retake_change_higher, scoreChange.absoluteDelta)
                                ScoreChangeDirection.LOWER -> stringResource(R.string.retake_change_lower, scoreChange.absoluteDelta)
                                ScoreChangeDirection.SAME -> stringResource(R.string.retake_change_same)
                            },
                            color = Muted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = PanelSoft), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text(stringResource(R.string.profile_progress), color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.profile_dimensions_discovered, completedCount, totalQuizCount), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(onClick = {
                AppEvents.resultShare(quiz.id, score)
                ResultShare.share(context, quiz.title, resultTitle, score, quiz.metricLow, quiz.metricHigh, description)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet), shape = RoundedCornerShape(18.dp)) {
                Text(stringResource(R.string.share_my_result), fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                AppEvents.challengeCreate(quiz.id, score)
                ChallengeShare.share(context, quiz.id, quiz.title, score)
            }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = PanelSoft), shape = RoundedCornerShape(18.dp)) {
                Text(stringResource(R.string.compare_with_friend), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.friend_match_explainer), color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRetry, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PanelSoft)) { Text(stringResource(R.string.retry)) }
                Button(onClick = onDone, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink)) { Text(stringResource(R.string.done_button), fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(30.dp))
            Text(stringResource(R.string.disclaimer), color = Muted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
        }
    }
}