package com.whoareyou.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = V2Colors.Ink,
                    surface = V2Colors.Surface,
                    primary = V2Colors.AccentViolet,
                    secondary = V2Colors.AccentCyan
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = V2Colors.Ink
                ) {
                    WhoAreYouApp()
                }
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
                Screen.DISCOVER -> DiscoverHub(
                    quizzes = quizCatalog,
                    profile = globalProfile,
                    storedProfile = storedProfile,
                    completed = storedProfile.completedQuizIds,
                    adsRemoved = adsRemoved,
                    onOpenProfile = { screen = Screen.PROFILE },
                    onQuizSelected = { quiz ->
                        previousScoreForAttempt = storedProfile.latestScores[quiz.id]
                        selectedQuiz = quiz
                        AppEvents.testStart(quiz.id)
                        screen = Screen.QUIZ
                    },
                    onRemoveAds = {
                        if (!adsRemoved && activity != null) {
                            billingManager.launchPurchase(activity)
                        }
                    }
                )

                Screen.PROFILE -> ProfileScreen(
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
                        scope.launch {
                            ProfileStore.saveQuizResult(context, selectedQuiz.id, score)
                        }
                        screen = Screen.RESULT
                    }
                )

                Screen.RESULT -> ResultScreen(
                    quiz = selectedQuiz,
                    score = finalScore,
                    previousScore = previousScoreForAttempt,
                    completedCount = (storedProfile.completedQuizIds + selectedQuiz.id).size,
                    totalQuizCount = quizCatalog.size,
                    onDone = {
                        adManager.onResultFinished(activity, adsRemoved) {
                            screen = Screen.DISCOVER
                        }
                    },
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
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.catalog_unavailable_title),
            color = V2Colors.TextPrimary,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.catalog_unavailable_body),
            color = V2Colors.TextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                stringResource(R.string.onboarding_title),
                color = V2Colors.TextPrimary,
                fontSize = 58.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.onboarding_subtitle),
                color = V2Colors.AccentViolet,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.onboarding_body),
                color = V2Colors.TextSecondary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentViolet),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.onboarding_start), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.onboarding_no_account),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
