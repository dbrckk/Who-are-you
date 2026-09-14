package com.whoareyou.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

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
            ProfileStore.commitQuizResult(context, quiz.id, score, attemptId)
        }
        if (commitResult.isFailure) {
            onCommitFailed()
            return@LaunchedEffect
        }
        val committed = commitResult.getOrNull()
        if (committed == null) {
            onCommitFailed()
            return@LaunchedEffect
        }
        if (committed.changed) {
            runCatching { AppEvents.testComplete(quiz.id, committed.persistedScore) }
            runCatching { AppEvents.resultView(quiz.id, committed.persistedScore) }
        }
        onCommitted(committed.persistedScore)
    }
}
