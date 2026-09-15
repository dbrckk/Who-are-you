package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(
    quiz: Quiz,
    score: Int,
    previousScore: Int?,
    completedCount: Int,
    totalQuizCount: Int,
    catalog: List<Quiz>,
    completed: Set<String>,
    coverage: ProfileCoverage,
    onQuizSelected: (Quiz) -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val constrainedLayout = configuration.screenWidthDp <= 360 || fontScale >= 1.3f
    val sectionGap = if (constrainedLayout) 14.dp else 18.dp
    val cardPadding = if (constrainedLayout) 18.dp else 24.dp
    val heroScoreSize = if (constrainedLayout) 48.sp else 54.sp
    val heroScoreLineHeight = if (constrainedLayout) 54.sp else 60.sp
    val resultTitle = quiz.resultTitleFor(score)
    val description = quiz.resultDescriptionFor(score)
    val scoreChange = remember(previousScore, score) { ScoreChangeEngine.compare(previousScore, score) }
    val accent = QuizVisuals.accentFor(quiz)
    val secondaryAccent = QuizVisuals.companionAccentFor(quiz)
    val resultBackground = remember(accent, secondaryAccent) {
        Brush.verticalGradient(
            listOf(
                accent.copy(alpha = 0.13f),
                V2Colors.InkSoft,
                V2Colors.Ink,
                secondaryAccent.copy(alpha = 0.08f),
                V2Colors.Ink
            )
        )
    }
    val scoreCardBrush = remember(accent, secondaryAccent) {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.16f),
                Color.Transparent,
                secondaryAccent.copy(alpha = 0.10f)
            )
        )
    }
    val scoreAccessibility = stringResource(
        R.string.result_score_accessibility,
        score.coerceIn(0, 100),
        quiz.metricLow,
        quiz.metricHigh
    )
    val reduceMotion = reducedMotionEnabled()
    val animatedScore by animateFloatAsState(
        targetValue = score.coerceIn(0, 100) / 100f,
        animationSpec = if (reduceMotion) snap() else tween(V2Motion.EmphasizedMillis),
        label = "resultScoreProgress"
    )
    val scoreCounter = remember(score) { Animatable(if (reduceMotion) score.toFloat() else 0f) }
    LaunchedEffect(score, reduceMotion) {
        if (reduceMotion) {
            scoreCounter.snapTo(score.coerceIn(0, 100).toFloat())
        } else {
            scoreCounter.animateTo(
                targetValue = score.coerceIn(0, 100).toFloat(),
                animationSpec = tween(V2Motion.EmphasizedMillis)
            )
        }
    }
    val displayedScore = scoreCounter.value.roundToInt().coerceIn(0, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .readableContentWidth()
            .background(resultBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = V2Spacing.Screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(if (constrainedLayout) 22.dp else 32.dp))
            Text(stringResource(R.string.your_result), color = accent, style = V2Type.Eyebrow)
            Spacer(Modifier.height(if (constrainedLayout) 12.dp else 16.dp))
            BrandMascot(
                mood = BrandMascotMood.CELEBRATE,
                primary = accent,
                secondary = secondaryAccent
            )
            Spacer(Modifier.height(10.dp))
            QuizArtwork(quiz)
            Spacer(Modifier.height(sectionGap))
            Text(
                resultTitle.uppercase(),
                color = V2Colors.TextPrimary,
                style = V2Type.Hero,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("result_title")
                    .semantics {
                        heading()
                        contentDescription = resultTitle
                    }
            )
            Spacer(Modifier.height(8.dp))
            Text(quiz.title, color = V2Colors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(if (constrainedLayout) 18.dp else 26.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(V2Radius.Card),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_score_card")
            ) {
                Column(
                    Modifier
                        .background(scoreCardBrush)
                        .padding(cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$displayedScore%",
                        color = accent,
                        fontSize = heroScoreSize,
                        lineHeight = heroScoreLineHeight,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .testTag("result_score")
                            .semantics { contentDescription = scoreAccessibility }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(quiz.metricLow, color = V2Colors.TextSecondary, style = V2Type.Supporting, modifier = Modifier.weight(1f))
                        Text(quiz.metricHigh, color = V2Colors.TextSecondary, style = V2Type.Supporting, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clearAndSetSemantics { },
                        color = accent,
                        trackColor = secondaryAccent.copy(alpha = 0.18f)
                    )
                    Spacer(Modifier.height(if (constrainedLayout) 16.dp else 20.dp))
                    Text(
                        description,
                        color = V2Colors.TextPrimary,
                        style = V2Type.Body,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("result_description")
                    )
                }
            }

            Spacer(Modifier.height(sectionGap))
            ResultInterpretationPanel(quiz = quiz, score = score)

            if (scoreChange != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
                    shape = RoundedCornerShape(V2Radius.Compact),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.retake_change_title), color = secondaryAccent, style = V2Type.Eyebrow)
                        Spacer(Modifier.height(7.dp))
                        Text(
                            stringResource(R.string.retake_change_values, scoreChange.previousScore, scoreChange.currentScore),
                            color = V2Colors.TextPrimary,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when (scoreChange.direction) {
                                ScoreChangeDirection.HIGHER -> stringResource(R.string.retake_change_higher, scoreChange.absoluteDelta)
                                ScoreChangeDirection.LOWER -> stringResource(R.string.retake_change_lower, scoreChange.absoluteDelta)
                                ScoreChangeDirection.SAME -> stringResource(R.string.retake_change_same)
                            },
                            color = V2Colors.TextSecondary,
                            style = V2Type.Supporting,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(V2Radius.Compact),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(stringResource(R.string.profile_progress), color = secondaryAccent, style = V2Type.Eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.profile_dimensions_discovered, completedCount, totalQuizCount), color = V2Colors.TextPrimary, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(18.dp))
            ResultNextExplorationCard(
                currentQuiz = quiz,
                catalog = catalog,
                completed = completed,
                coverage = coverage,
                onQuizSelected = onQuizSelected
            )

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("result_done"),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = V2Colors.Ink),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.done_button), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("result_retry"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = V2Colors.TextPrimary),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.retry), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.result_optional_actions),
                color = V2Colors.TextSecondary,
                style = V2Type.Caption,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    AppEvents.resultShare(quiz.id, score)
                    ResultShare.share(context, quiz.id, quiz.title, resultTitle, score, quiz.metricLow, quiz.metricHigh, description)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("result_share"),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.share_my_result), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    AppEvents.challengeCreate(quiz.id, score)
                    ChallengeShare.share(context, quiz.id, quiz.title, score)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("result_compare"),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.compare_with_friend), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.friend_match_explainer), color = V2Colors.TextSecondary, style = V2Type.Supporting, textAlign = TextAlign.Center)

            Spacer(Modifier.height(30.dp))
            Text(stringResource(R.string.disclaimer), color = V2Colors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
        }
    }
}
