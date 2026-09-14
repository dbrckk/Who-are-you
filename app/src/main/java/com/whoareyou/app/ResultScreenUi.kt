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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
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
                accent.copy(alpha = 0.12f),
                Color.Transparent,
                secondaryAccent.copy(alpha = 0.08f)
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
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.your_result), color = accent, style = V2Type.Eyebrow)
            Spacer(Modifier.height(16.dp))
            IdentityAura(score = score, primary = accent, secondary = secondaryAccent)
            Spacer(Modifier.height(10.dp))
            QuizArtwork(quiz)
            Spacer(Modifier.height(18.dp))
            Text(
                resultTitle.uppercase(),
                color = V2Colors.TextPrimary,
                style = V2Type.Hero,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(quiz.title, color = V2Colors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(26.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                shape = RoundedCornerShape(V2Radius.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .background(scoreCardBrush)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$score%",
                        color = accent,
                        fontSize = 54.sp,
                        lineHeight = 60.sp,
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
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = accent,
                        trackColor = secondaryAccent.copy(alpha = 0.16f)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(description, color = V2Colors.TextPrimary, style = V2Type.Body, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(18.dp))
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
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("result_retry"),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.retry), textAlign = TextAlign.Center)
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
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
