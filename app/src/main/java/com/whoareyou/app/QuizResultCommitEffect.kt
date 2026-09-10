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
    onCommitted: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(screen, quiz.id, attemptId, pendingScore) {
        val score = pendingScore
        if (screen != AppScreen.QUIZ || score == null) return@LaunchedEffect
        val committed = runCatching {
            ProfileStore.saveQuizResult(context, quiz.id, score, attemptId)
        }.isSuccess
        if (!committed) {
            onCommitFailed()
            return@LaunchedEffect
        }
        runCatching { AppEvents.testComplete(quiz.id, score) }
        runCatching { AppEvents.resultView(quiz.id, score) }
        onCommitted()
    }
}
