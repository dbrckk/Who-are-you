package com.whoareyou.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first

@Composable
fun QuizResultCommitEffect(
    screen: AppScreen,
    quiz: Quiz,
    attemptId: String,
    pendingScore: Int?,
    onCommitFailed: () -> Unit,
    onCommitted: (Int) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(screen, quiz.id, attemptId, pendingScore) {
        val score = pendingScore
        if (screen != AppScreen.QUIZ || score == null) return@LaunchedEffect
        val commitResult = runCatching {
            ProfileStore.saveQuizResult(context, quiz.id, score, attemptId)
        }
        if (commitResult.isFailure) {
            onCommitFailed()
            return@LaunchedEffect
        }
        val changed = commitResult.getOrDefault(false)
        if (changed) {
            runCatching { AppEvents.testComplete(quiz.id, score) }
            runCatching { AppEvents.resultView(quiz.id, score) }
        }
        val persistedScore = runCatching {
            ProfileStore.observe(context).first().latestScores[quiz.id]
        }.getOrNull() ?: score
        onCommitted(persistedScore)
    }
}
