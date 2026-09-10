package com.whoareyou.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WhoAreYouTheme { WhoAreYouApp() } }
    }
}

@Composable
private fun WhoAreYouApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val quizCatalog = remember(context) { QuizRepository.load(context) }
    val storedProfileFlow = remember(context) { ProfileStore.observe(context).map<StoredProfile, StoredProfile?> { it } }
    val storedProfileState by storedProfileFlow.collectAsState(initial = null)
    val storedProfile = storedProfileState ?: return
    val globalProfile = remember(quizCatalog, storedProfile.latestScores, storedProfile.previousScores) {
        GlobalProfileEngine.build(quizCatalog, storedProfile.latestScores, storedProfile.previousScores)
    }
    var premiumOverride by remember { mutableStateOf(false) }
    var privacyOptionsRequired by remember { mutableStateOf(false) }
    val adsRemoved = storedProfile.adsRemoved || premiumOverride
    val billingManager = remember(context) {
        if (BuildConfig.EXTERNAL_SERVICES_ENABLED) runCatching {
            BillingManager(context, { premiumOverride = it }, BillingPriceState::update)
        }.getOrNull() else null
    }
    val adManager = remember(context) {
        if (BuildConfig.EXTERNAL_SERVICES_ENABLED) runCatching {
            AdManager(context) { required -> privacyOptionsRequired = required }
        }.getOrNull() else null
    }

    DisposableEffect(billingManager, adManager, activity) {
        runCatching { billingManager?.start() }
        runCatching { adManager?.start(activity) }
        onDispose { runCatching { billingManager?.close() } }
    }
    if (!storedProfile.onboardingComplete) {
        LaunchedEffect(Unit) { runCatching { AppEvents.onboardingView() } }
        OnboardingScreen {
            runCatching { AppEvents.onboardingComplete() }
            scope.launch { ProfileStore.setOnboardingComplete(context) }
        }
        return
    }
    if (!AppNavigation.hasUsableCatalog(quizCatalog.size)) {
        LaunchedEffect(quizCatalog.size) { runCatching { AppEvents.catalogUnavailable() } }
        CatalogUnavailableScreen()
        return
    }

    var screenName by rememberSaveable { mutableStateOf(AppScreen.DISCOVER.name) }
    val screen = runCatching { AppScreen.valueOf(screenName) }.getOrDefault(AppScreen.DISCOVER)
    var selectedQuizId by rememberSaveable { mutableStateOf(quizCatalog.first().id) }
    val selectedQuiz = quizCatalog.firstOrNull { it.id == selectedQuizId } ?: quizCatalog.first()
    var quizAttemptId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var quizQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    var quizRawScore by rememberSaveable { mutableIntStateOf(0) }
    var pendingFinalScore by rememberSaveable { mutableStateOf<Int?>(null) }
    var finalScore by rememberSaveable { mutableIntStateOf(0) }
    var previousScoreForAttempt by rememberSaveable { mutableStateOf<Int?>(null) }
    val quizFinishing = pendingFinalScore != null

    fun navigate(destination: AppScreen) { screenName = destination.name }
    fun resetQuizAttempt() {
        quizAttemptId = UUID.randomUUID().toString()
        quizQuestionIndex = 0
        quizRawScore = 0
        pendingFinalScore = null
    }

    QuizResultCommitEffect(
        screen, selectedQuiz, quizAttemptId, pendingFinalScore,
        onCommitFailed = { pendingFinalScore = null },
        onCommitted = { navigate(AppScreen.RESULT) }
    )
    LaunchedEffect(screen) { runCatching { AppEvents.screenView(screen) } }
    BackHandler(enabled = screen != AppScreen.DISCOVER) {
        if (screen == AppScreen.QUIZ && quizFinishing) return@BackHandler
        if (screen == AppScreen.QUIZ) runCatching { AppEvents.testAbandon(selectedQuiz.id, "system_back") }
        navigate(AppNavigation.backDestination(screen) ?: AppScreen.DISCOVER)
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { premiumScreenTransition() },
            label = "screen",
            modifier = Modifier.fillMaxSize().testTag("app_screen_${screen.name.lowercase()}")
        ) { destination ->
            when (destination) {
                AppScreen.DISCOVER -> DiscoverHub(
                    quizzes = quizCatalog,
                    profile = globalProfile,
                    storedProfile = storedProfile,
                    completed = storedProfile.completedQuizIds,
                    adsRemoved = adsRemoved,
                    privacyOptionsRequired = privacyOptionsRequired,
                    onOpenProfile = { navigate(AppScreen.PROFILE) },
                    onQuizSelected = { quiz ->
                        previousScoreForAttempt = storedProfile.latestScores[quiz.id]
                        selectedQuizId = quiz.id
                        resetQuizAttempt()
                        runCatching { AppEvents.testStart(quiz.id) }
                        navigate(AppScreen.QUIZ)
                    },
                    onRemoveAds = {
                        if (!adsRemoved && activity != null) runCatching { billingManager?.launchPurchase(activity) }
                    },
                    onPrivacyOptions = { runCatching { adManager?.showPrivacyOptions(activity) } }
                )
                AppScreen.PROFILE -> ProfileScreen(globalProfile, quizCatalog) { navigate(AppScreen.DISCOVER) }
                AppScreen.QUIZ -> QuizScreen(
                    quiz = selectedQuiz,
                    questionIndex = quizQuestionIndex,
                    score = quizRawScore,
                    onProgress = { questionIndex, score ->
                        if (!quizFinishing) {
                            quizQuestionIndex = questionIndex
                            quizRawScore = score
                        }
                    },
                    onBack = {
                        if (!quizFinishing) {
                            runCatching { AppEvents.testAbandon(selectedQuiz.id, "screen_back") }
                            navigate(AppScreen.DISCOVER)
                        }
                    },
                    onFinished = { score ->
                        if (!quizFinishing) {
                            finalScore = score
                            pendingFinalScore = score
                        }
                    }
                )
                AppScreen.RESULT -> ResultScreen(
                    quiz = selectedQuiz,
                    score = finalScore,
                    previousScore = previousScoreForAttempt,
                    completedCount = (storedProfile.completedQuizIds + selectedQuiz.id).size,
                    totalQuizCount = quizCatalog.size,
                    onDone = {
                        pendingFinalScore = null
                        val manager = adManager
                        if (manager == null) navigate(AppScreen.DISCOVER) else runCatching {
                            manager.onResultFinished(activity, adsRemoved) { navigate(AppScreen.DISCOVER) }
                        }.onFailure { navigate(AppScreen.DISCOVER) }
                    },
                    onRetry = {
                        previousScoreForAttempt = finalScore
                        resetQuizAttempt()
                        runCatching { AppEvents.testStart(selectedQuiz.id) }
                        navigate(AppScreen.QUIZ)
                    }
                )
            }
        }
        AppShellNavigation.tabFor(screen)?.let { selectedTab ->
            PremiumAppShellBar(
                selectedTab,
                { tab -> navigate(AppShellNavigation.destination(tab)) },
                Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
