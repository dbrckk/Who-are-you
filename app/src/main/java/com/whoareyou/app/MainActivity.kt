package com.whoareyou.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.health.connect.client.PermissionController
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(localizedAppContext(newBase)) }
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
    val quizCatalogState by produceState<List<Quiz>?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) { runCatching { QuizRepository.load(context.applicationContext) }.getOrDefault(emptyList()) }
    }
    val storedProfileFlow = remember(context) { ProfileStore.observe(context) }
    val storedProfileState by storedProfileFlow.collectAsState(initial = null)
    val behaviorFlow = remember(context) { BehaviorRepository.observe(context.applicationContext) }
    val behaviorSnapshot by behaviorFlow.collectAsState(
        initial = BehaviorSnapshot(null, emptyList(), emptyList(), BehaviorSource.entries.associateWith { BehaviorSourceState.DISABLED }, emptyList())
    )
    val behaviorRefreshCoordinator = remember(context) { createAndroidBehaviorRefreshCoordinator(context.applicationContext) }
    fun refreshBehavior() { scope.launch(Dispatchers.IO) { runCatching { behaviorRefreshCoordinator.refresh(Instant.now()) } } }
    val activityPermissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            val allowed = HealthConnectActivityDataSource.READ_STEPS_PERMISSION in granted
            BehaviorRepository.setSourceEnabled(context, BehaviorSource.ACTIVITY, true)
            BehaviorRepository.setSourceState(
                context,
                BehaviorSource.ACTIVITY,
                if (allowed) BehaviorSourceState.AVAILABLE else BehaviorSourceState.PERMISSION_REQUIRED
            )
            if (BehaviorIntegrationPolicy.shouldRefreshAfterActivityPermission(allowed)) refreshBehavior()
        }
    }
    val usageAccessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        scope.launch {
            val state = UsageAccess.state(context)
            BehaviorRepository.setSourceEnabled(context, BehaviorSource.APP_USAGE, true)
            BehaviorRepository.setSourceState(context, BehaviorSource.APP_USAGE, state)
            if (BehaviorIntegrationPolicy.shouldRefreshAfterUsageAccessReturn()) refreshBehavior()
        }
    }
    val storedProfile = storedProfileState
    val quizCatalog = quizCatalogState
    if (storedProfile == null || quizCatalog == null) { BrandLoadingScreen(tag = "startup_loading"); return }
    LaunchedEffect(activity, storedProfile.onboardingComplete, quizCatalog.size) { runCatching { activity?.reportFullyDrawn() } }

    val globalProfile = remember(quizCatalog, storedProfile.latestScores, storedProfile.previousScores, storedProfile.scoreHistory, storedProfile.timedScoreHistory) {
        GlobalProfileEngine.build(quizCatalog, storedProfile.latestScores, storedProfile.previousScores, storedProfile.scoreHistory, storedProfile.timedScoreHistory)
    }
    if (!storedProfile.onboardingComplete) {
        LaunchedEffect(Unit) { runCatching { AppEvents.onboardingView() } }
        OnboardingScreen { runCatching { AppEvents.onboardingComplete() }; scope.launch { ProfileStore.setOnboardingComplete(context) } }
        return
    }
    if (!AppNavigation.hasUsableCatalog(quizCatalog.size)) {
        LaunchedEffect(quizCatalog.size) { runCatching { AppEvents.catalogUnavailable() } }
        CatalogUnavailableScreen(); return
    }

    var premiumOverride by remember { mutableStateOf(false) }
    var privacyOptionsRequired by remember { mutableStateOf(false) }
    val adsRemoved = storedProfile.adsRemoved || premiumOverride
    val billingManager = remember(context) { if (BuildConfig.EXTERNAL_SERVICES_ENABLED) runCatching { BillingPriceState.markLoading(); BillingManager(context, { premiumOverride = it }, BillingPriceState::update) }.getOrNull() else null }
    val adManager = remember(context, adsRemoved) { if (BuildConfig.EXTERNAL_SERVICES_ENABLED && !adsRemoved) runCatching { AdManager(context) { required -> privacyOptionsRequired = required } }.getOrNull() else null }
    LaunchedEffect(billingManager, adManager, activity) { withFrameNanos { }; runCatching { billingManager?.start() }; runCatching { adManager?.start(activity) } }
    DisposableEffect(billingManager) { onDispose { runCatching { billingManager?.close() } } }
    DisposableEffect(adManager) { onDispose { runCatching { adManager?.close() } } }

    var screenName by rememberSaveable { mutableStateOf(AppScreen.DISCOVER.name) }
    val screen = runCatching { AppScreen.valueOf(screenName) }.getOrDefault(AppScreen.DISCOVER)
    var selectedQuizId by rememberSaveable { mutableStateOf(quizCatalog.first().id) }
    val selectedQuiz = quizCatalog.firstOrNull { it.id == selectedQuizId }
    var quizAttemptId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    val quizAttemptEvidence = remember { QuizAttemptEvidence() }
    var quizQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    var quizRawScore by rememberSaveable { mutableIntStateOf(0) }
    var pendingFinalScore by rememberSaveable { mutableStateOf<Int?>(null) }
    var commitFailed by rememberSaveable { mutableStateOf(false) }
    var finalScore by rememberSaveable { mutableIntStateOf(0) }
    var previousScoreForAttempt by rememberSaveable { mutableStateOf<Int?>(null) }
    val quizFinishing = pendingFinalScore != null
    if (selectedQuiz == null) {
        LaunchedEffect(selectedQuizId, quizCatalog) { selectedQuizId = quizCatalog.first().id; quizAttemptId = UUID.randomUUID().toString(); quizQuestionIndex = 0; quizRawScore = 0; pendingFinalScore = null; commitFailed = false; finalScore = 0; previousScoreForAttempt = null; screenName = AppScreen.DISCOVER.name }
        BrandLoadingScreen(tag = "quiz_session_recovering"); return
    }

    fun navigate(destination: AppScreen) { screenName = destination.name }
    fun resetQuizAttempt() { quizAttemptId = UUID.randomUUID().toString(); quizQuestionIndex = 0; quizRawScore = 0; quizAttemptEvidence.clear(); pendingFinalScore = null; commitFailed = false }
    QuizResultCommitEffect(screen, selectedQuiz, quizAttemptId, pendingFinalScore, onCommitFailed = { pendingFinalScore = null; commitFailed = true }, onCommitted = { finalScore = it; navigate(AppScreen.RESULT) })
    LaunchedEffect(screen) { runCatching { AppEvents.screenView(screen) }; if (screen == AppScreen.HABITS) refreshBehavior() }
    BackHandler(enabled = screen != AppScreen.DISCOVER) { if (screen == AppScreen.QUIZ && quizFinishing) return@BackHandler; if (screen == AppScreen.QUIZ) runCatching { AppEvents.testAbandon(selectedQuiz.id, "system_back") }; navigate(AppNavigation.backDestination(screen) ?: AppScreen.DISCOVER) }

    val reduceMotion = reducedMotionEnabled()
    val screenPaneTitle = when (screen) { AppScreen.DISCOVER -> stringResource(R.string.discover_headline); AppScreen.PROFILE -> stringResource(R.string.your_profile); AppScreen.HABITS -> stringResource(R.string.habits_title); AppScreen.QUIZ, AppScreen.RESULT -> selectedQuiz.title }
    Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
        AnimatedContent(targetState = screen, transitionSpec = { premiumScreenTransition(initialState, targetState, reduceMotion) }, label = "screen", modifier = Modifier.fillMaxSize().testTag("app_screen_${screen.name.lowercase()}").semantics { paneTitle = screenPaneTitle }) { destination ->
            when (destination) {
                AppScreen.DISCOVER -> DiscoverHub(quizCatalog, globalProfile, storedProfile, storedProfile.completedQuizIds, adsRemoved, privacyOptionsRequired && !adsRemoved, { navigate(AppScreen.PROFILE) }, { quiz -> previousScoreForAttempt = storedProfile.latestScores[quiz.id]; selectedQuizId = quiz.id; resetQuizAttempt(); runCatching { AppEvents.testStart(quiz.id) }; navigate(AppScreen.QUIZ) }, { if (!adsRemoved && activity != null) runCatching { billingManager?.launchPurchase(activity) } }, { runCatching { adManager?.showPrivacyOptions(activity) } })
                AppScreen.PROFILE -> ProfileScreen(globalProfile, quizCatalog, { quiz -> previousScoreForAttempt = storedProfile.latestScores[quiz.id]; selectedQuizId = quiz.id; resetQuizAttempt(); runCatching { AppEvents.testStart(quiz.id) }; navigate(AppScreen.QUIZ) }, { navigate(AppScreen.DISCOVER) }, { scope.launch { ProfileStore.clearLocalProfile(context); navigate(AppScreen.DISCOVER) } })
                AppScreen.HABITS -> BehaviorScreen(
                    model = BehaviorUiModelFactory.build(behaviorSnapshot),
                    onBack = { navigate(AppScreen.DISCOVER) },
                    onSourceAction = { source, action ->
                        when (BehaviorIntegrationPolicy.command(source, action)) {
                            BehaviorIntegrationCommand.DISABLE_SOURCE -> scope.launch { BehaviorRepository.clearSource(context, source) }
                            BehaviorIntegrationCommand.REQUEST_ACTIVITY_PERMISSION -> scope.launch {
                                BehaviorRepository.setSourceEnabled(context, source, true)
                                activityPermissionLauncher.launch(setOf(HealthConnectActivityDataSource.READ_STEPS_PERMISSION))
                            }
                            BehaviorIntegrationCommand.OPEN_USAGE_ACCESS -> scope.launch {
                                BehaviorRepository.setSourceEnabled(context, source, true)
                                usageAccessLauncher.launch(UsageAccess.settingsIntent())
                            }
                            BehaviorIntegrationCommand.NONE -> Unit
                        }
                    },
                    onDeleteAll = { scope.launch { BehaviorRepository.clearAll(context) } }
                )
                AppScreen.QUIZ -> QuizScreen(selectedQuiz, quizQuestionIndex, quizRawScore, quizFinishing, commitFailed, { q, s -> if (!quizFinishing) { quizQuestionIndex = q; quizRawScore = s } }, { q, a, s -> if (!quizFinishing) quizAttemptEvidence.record(q, a, s) }, { if (!quizFinishing) { runCatching { AppEvents.testAbandon(selectedQuiz.id, "screen_back") }; navigate(AppScreen.DISCOVER) } }, { score -> if (!quizFinishing) { commitFailed = false; pendingFinalScore = score } })
                AppScreen.RESULT -> ResultScreen(selectedQuiz, finalScore, previousScoreForAttempt, (storedProfile.completedQuizIds + selectedQuiz.id).size, quizCatalog.size, quizCatalog, storedProfile.completedQuizIds + selectedQuiz.id, ResultEvidenceEngine.derive(selectedQuiz.questions, quizAttemptEvidence.snapshot()), globalProfile.traitGraph, globalProfile.coverage, { quiz -> previousScoreForAttempt = storedProfile.latestScores[quiz.id]; selectedQuizId = quiz.id; resetQuizAttempt(); runCatching { AppEvents.testStart(quiz.id) }; navigate(AppScreen.QUIZ) }, { pendingFinalScore = null; val manager = adManager; if (manager == null) navigate(AppScreen.DISCOVER) else runCatching { manager.onResultFinished(activity, adsRemoved) { navigate(AppScreen.DISCOVER) } }.onFailure { navigate(AppScreen.DISCOVER) } }, { previousScoreForAttempt = finalScore; resetQuizAttempt(); runCatching { AppEvents.testStart(selectedQuiz.id) }; navigate(AppScreen.QUIZ) })
            }
        }
        AppShellNavigation.tabFor(screen)?.let { PremiumAppShellBar(it, { tab -> navigate(AppShellNavigation.destination(tab)) }, Modifier.align(Alignment.BottomCenter)) }
    }
}
