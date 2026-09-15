package com.whoareyou.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(
    quiz: Quiz,
    questionIndex: Int,
    score: Int,
    isFinishing: Boolean,
    commitFailed: Boolean,
    onProgress: (questionIndex: Int, score: Int) -> Unit,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit
) {
    val safeQuestionIndex = questionIndex.coerceIn(0, quiz.questions.lastIndex)
    val progress = (safeQuestionIndex + 1f) / quiz.questions.size
    val reduceMotion = reducedMotionEnabled()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (reduceMotion) snap() else tween(V2Motion.StandardMillis),
        label = "quizProgress"
    )
    val canNavigateBack = !isFinishing
    val scrollState = rememberScrollState()
    val accent = QuizVisuals.accentFor(quiz)
    val companion = QuizVisuals.companionAccentFor(quiz)
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val constrainedLayout = configuration.screenWidthDp <= 360 || fontScale >= 1.3f
    val artworkGap = if (constrainedLayout) 16.dp else 24.dp
    val answerGap = if (constrainedLayout) 10.dp else 12.dp
    val answerVerticalPadding = if (constrainedLayout) 14.dp else 16.dp

    BackHandler(enabled = isFinishing) { }

    LaunchedEffect(quiz.id, safeQuestionIndex) {
        scrollState.scrollTo(0)
    }

    Column(
        Modifier
            .fillMaxHeight()
            .readableContentWidth()
            .testTag("quiz_question_${safeQuestionIndex + 1}")
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.08f),
                        V2Colors.InkSoft,
                        V2Colors.Ink,
                        companion.copy(alpha = 0.05f),
                        V2Colors.Ink
                    )
                )
            )
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(V2Spacing.Screen)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccessibleBackAction(onClick = { if (canNavigateBack) onBack() })
            Text(
                stringResource(R.string.question_progress, safeQuestionIndex + 1, quiz.questions.size),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = accent,
            trackColor = companion.copy(alpha = 0.14f)
        )
        Spacer(Modifier.height(20.dp))
        QuizArtwork(quiz, compact = true)
        Spacer(Modifier.height(artworkGap))
        Text(
            quiz.title.uppercase(),
            color = accent,
            style = V2Type.Eyebrow
        )
        Spacer(Modifier.height(12.dp))
        AnimatedContent(
            targetState = safeQuestionIndex,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                } else {
                    (
                        fadeIn(tween(V2Motion.StandardMillis)) +
                            slideInHorizontally(
                                animationSpec = tween(V2Motion.StandardMillis),
                                initialOffsetX = { width -> width / 12 }
                            )
                        ).togetherWith(
                            fadeOut(tween(V2Motion.FastMillis)) +
                                slideOutHorizontally(
                                    animationSpec = tween(V2Motion.FastMillis),
                                    targetOffsetX = { width -> -width / 16 }
                                )
                        )
                }
            },
            label = "quizQuestion"
        ) { animatedIndex ->
            Text(
                quiz.questions[animatedIndex.coerceIn(0, quiz.questions.lastIndex)].text,
                color = V2Colors.TextPrimary,
                style = V2Type.Question
            )
        }
        if (isFinishing) {
            Spacer(Modifier.height(14.dp))
            V2StatusNotice(
                text = stringResource(R.string.quiz_result_saving),
                accent = accent,
                tag = "quiz_result_saving"
            )
        } else if (commitFailed) {
            Spacer(Modifier.height(14.dp))
            V2StatusNotice(
                text = stringResource(R.string.quiz_result_save_failed),
                accent = V2Colors.Peach,
                tag = "quiz_result_save_failed"
            )
        }
        Spacer(Modifier.height(if (constrainedLayout) 20.dp else 26.dp))

        AnimatedContent(
            targetState = safeQuestionIndex,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                } else {
                    (
                        fadeIn(tween(V2Motion.StandardMillis)) +
                            slideInHorizontally(
                                animationSpec = tween(V2Motion.StandardMillis),
                                initialOffsetX = { width -> width / 18 }
                            )
                        ).togetherWith(
                            fadeOut(tween(V2Motion.FastMillis)) +
                                slideOutHorizontally(
                                    animationSpec = tween(V2Motion.FastMillis),
                                    targetOffsetX = { width -> -width / 24 }
                                )
                        )
                }
            },
            label = "quizAnswers"
        ) { animatedIndex ->
            val animatedQuestion = quiz.questions[animatedIndex.coerceIn(0, quiz.questions.lastIndex)]
            Column(verticalArrangement = Arrangement.spacedBy(answerGap)) {
                animatedQuestion.answers.forEachIndexed { answerIndex, answer ->
                    V2PressableSurface(
                        onClick = {
                            if (isFinishing || animatedIndex != safeQuestionIndex) return@V2PressableSurface
                            val newScore = score + answer.score
                            if (safeQuestionIndex == quiz.questions.lastIndex) {
                                onFinished(Scoring.quizPercent(newScore, quiz.questions.size))
                            } else {
                                onProgress(safeQuestionIndex + 1, newScore)
                            }
                        },
                        enabled = !isFinishing && animatedIndex == safeQuestionIndex,
                        modifier = if (animatedIndex == safeQuestionIndex) {
                            Modifier.testTag("quiz_answer_$answerIndex")
                        } else {
                            Modifier
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            accent.copy(alpha = 0.08f),
                                            V2Colors.Surface,
                                            companion.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = answerVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (answerIndex % 2 == 0) accent.copy(alpha = 0.18f) else companion.copy(alpha = 0.18f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + answerIndex).toChar().toString(),
                                    color = if (answerIndex % 2 == 0) accent else companion,
                                    style = V2Type.Caption,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                answer.text,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                                color = V2Colors.TextPrimary,
                                style = V2Type.BodyStrong
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.no_right_answers),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
    }
}


@Composable
private fun V2StatusNotice(
    text: String,
    accent: androidx.compose.ui.graphics.Color,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.16f),
                        V2Colors.SurfaceElevated,
                        accent.copy(alpha = 0.07f)
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Compact)
            )
            .testTag(tag)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = V2Colors.TextPrimary,
            style = V2Type.Supporting,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
