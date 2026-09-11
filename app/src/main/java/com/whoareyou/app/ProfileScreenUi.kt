package com.whoareyou.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    summary: GlobalProfileSummary,
    catalog: List<Quiz>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strongestDimension = summary.dimensions.maxByOrNull { kotlin.math.abs(it.score - 50) }
    val strongestQuiz = strongestDimension?.let { dimension -> catalog.firstOrNull { it.id == dimension.quizId } }
    val primaryAccent = strongestQuiz?.let(QuizVisuals::accentFor) ?: V2Colors.Orchid
    val companionAccent = strongestQuiz?.let(QuizVisuals::companionAccentFor) ?: V2Colors.Cyan
    val profileProgressText = stringResource(
        R.string.profile_header_progress,
        summary.completedCount,
        summary.totalCount,
        summary.completionPercent
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        primaryAccent.copy(alpha = 0.11f),
                        V2Colors.InkSoft,
                        V2Colors.Ink,
                        companionAccent.copy(alpha = 0.05f),
                        V2Colors.Ink
                    )
                )
            )
            .padding(horizontal = V2Spacing.Screen),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            AccessibleBackAction(onClick = onBack)
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.your_profile), color = primaryAccent, style = V2Type.Eyebrow)
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${summary.dominantArchetype}. $profileProgressText"
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IdentityAura(
                    score = strongestDimension?.score ?: summary.completionPercent,
                    primary = primaryAccent,
                    secondary = companionAccent
                )
                strongestQuiz?.let { quiz ->
                    Spacer(Modifier.height(12.dp))
                    QuizArtwork(quiz = quiz, compact = true)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    summary.dominantArchetype.uppercase(),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Hero,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    profileProgressText,
                    color = V2Colors.TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    AppEvents.profileShare(summary.dominantArchetype, summary.completedCount)
                    GlobalProfileShare.share(context, summary)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryAccent, contentColor = V2Colors.Ink),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.share_my_profile), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.compare_profile_friend), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (strongestDimension == null) stringResource(R.string.complete_test_unlock_compare)
                else stringResource(R.string.starts_with_dimension, strongestDimension.title),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (summary.dimensions.size >= 3) {
                Spacer(Modifier.height(18.dp))
                ProfileIdentityMap(summary)
                Spacer(Modifier.height(18.dp))
                ProfileStrengthNuanceCard(summary)
            }
            if (summary.dimensions.any { it.scoreChange != null }) {
                Spacer(Modifier.height(18.dp))
                ProfileEvolutionCard(summary)
            }
            if (summary.dimensions.size >= 5) {
                Spacer(Modifier.height(18.dp))
                ProfileInsightCards(summary)
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.all_dimensions), color = companionAccent, style = V2Type.Eyebrow)
        }

        if (summary.dimensions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(V2Radius.Card),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(stringResource(R.string.profile_undiscovered), color = V2Colors.TextPrimary, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Text(stringResource(R.string.complete_first_test_profile), color = V2Colors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        } else {
            items(summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }, key = { it.quizId }) { dimension ->
                val quiz = catalog.firstOrNull { it.id == dimension.quizId }
                val accent = quiz?.let(QuizVisuals::accentFor) ?: V2Colors.AccentViolet
                val companion = quiz?.let(QuizVisuals::companionAccentFor) ?: V2Colors.AccentCyan
                val animatedProgress by animateFloatAsState(
                    targetValue = dimension.score.coerceIn(0, 100) / 100f,
                    animationSpec = tween(V2Motion.EmphasizedMillis),
                    label = "profileDimensionProgress"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(V2Radius.Compact),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${dimension.title}. ${dimension.resultTitle}. ${dimension.score}%"
                        }
                ) {
                    Column(
                        Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        accent.copy(alpha = 0.08f),
                                        V2Colors.Surface,
                                        companion.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(dimension.title.uppercase(), color = V2Colors.TextPrimary, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(3.dp))
                                Text(dimension.resultTitle, color = companion, style = V2Type.Supporting, fontWeight = FontWeight.Bold)
                            }
                            Text("${dimension.score}%", color = accent, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = accent,
                            trackColor = companion.copy(alpha = 0.14f)
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(dimension.metricLabel, color = V2Colors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                        dimension.scoreChange?.let { change ->
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.profile_evolution), color = companion, fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.profile_evolution_values, change.previousScore, change.currentScore),
                                color = V2Colors.TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                when (change.direction) {
                                    ScoreChangeDirection.HIGHER -> stringResource(R.string.profile_evolution_higher, change.absoluteDelta)
                                    ScoreChangeDirection.LOWER -> stringResource(R.string.profile_evolution_lower, change.absoluteDelta)
                                    ScoreChangeDirection.SAME -> stringResource(R.string.profile_evolution_same)
                                },
                                color = V2Colors.TextSecondary,
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
            Text(
                stringResource(R.string.disclaimer),
                color = V2Colors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(108.dp))
        }
    }
}