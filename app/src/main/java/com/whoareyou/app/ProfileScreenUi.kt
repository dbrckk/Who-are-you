package com.whoareyou.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onQuizSelected: (Quiz) -> Unit,
    onBack: () -> Unit,
    onResetLocalData: () -> Unit
) {
    val context = LocalContext.current
    val reduceMotion = reducedMotionEnabled()
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedTraitId by remember { mutableStateOf<String?>(null) }
    var selectedJournalTrend by remember { mutableStateOf<LongitudinalTrend?>(null) }
    val catalogById = remember(catalog) { catalog.associateBy { it.id } }
    val longitudinalByQuizId = remember(summary.longitudinalTrends) {
        summary.longitudinalTrends.associateBy { it.quizId }
    }
    val sortedDimensions = remember(summary.dimensions) {
        summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }
    }
    val strongestDimension = sortedDimensions.firstOrNull()
    val strongestQuiz = strongestDimension?.let { dimension -> catalogById[dimension.quizId] }
    val primaryAccent = strongestQuiz?.let(QuizVisuals::accentFor) ?: V2Colors.Orchid
    val companionAccent = strongestQuiz?.let(QuizVisuals::companionAccentFor) ?: V2Colors.Cyan
    val profileBackground = remember(primaryAccent, companionAccent) {
        Brush.verticalGradient(
            listOf(
                primaryAccent.copy(alpha = 0.11f),
                V2Colors.InkSoft,
                V2Colors.Ink,
                companionAccent.copy(alpha = 0.05f),
                V2Colors.Ink
            )
        )
    }
    val profileProgressText = stringResource(
        R.string.profile_header_progress,
        summary.completedCount,
        summary.totalCount,
        summary.completionPercent
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .readableContentWidth()
            .background(profileBackground)
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
            Text(
                stringResource(R.string.profile_optional_social_actions),
                color = V2Colors.TextSecondary,
                style = V2Type.Caption,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    AppEvents.profileShare(summary.dominantArchetype, summary.completedCount)
                    GlobalProfileShare.share(context, summary)
                },
                enabled = summary.dimensions.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.share_my_profile), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
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
            if (summary.coverage.totalTraitCount > 0) {
                Spacer(Modifier.height(18.dp))
                ProfileCoverageCard(summary.coverage)
                Spacer(Modifier.height(18.dp))
                ProfileKnowledgeMapCard(
                    coverage = summary.coverage,
                    onTraitClick = { selectedTraitId = it }
                )
            }
            if (summary.longitudinalTrends.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                LongitudinalTrendsCard(
                    trends = summary.longitudinalTrends,
                    catalog = catalog
                )
            }
            if (
                summary.traitEvolution.meaningfulChanges.isNotEmpty() ||
                summary.traitEvolution.newEvidence.isNotEmpty()
            ) {
                Spacer(Modifier.height(18.dp))
                TraitEvolutionCard(
                    evolution = summary.traitEvolution,
                    catalog = catalog
                )
            }
            if (summary.traitGraph.traits.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                TraitGraphCard(summary.traitGraph)
            }
            if (summary.dimensions.size >= 3 || summary.signature != null || summary.dimensions.any { it.scoreChange != null }) {
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
            items(sortedDimensions, key = { it.quizId }, contentType = { "profile_dimension" }) { dimension ->
                val quiz = catalogById[dimension.quizId]
                val journalTrend = longitudinalByQuizId[dimension.quizId]
                val accent = quiz?.let(QuizVisuals::accentFor) ?: V2Colors.AccentViolet
                val companion = quiz?.let(QuizVisuals::companionAccentFor) ?: V2Colors.AccentCyan
                val dimensionBrush = remember(accent, companion) {
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.08f),
                            V2Colors.Surface,
                            companion.copy(alpha = 0.05f)
                        )
                    )
                }
                val animatedProgress by animateFloatAsState(
                    targetValue = dimension.score.coerceIn(0, 100) / 100f,
                    animationSpec = if (reduceMotion) snap() else tween(V2Motion.EmphasizedMillis),
                    label = "profileDimensionProgress"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(V2Radius.Compact),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = journalTrend != null) {
                            selectedJournalTrend = journalTrend
                        }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${dimension.title}. ${dimension.resultTitle}. ${dimension.score}%"
                        }
                ) {
                    Column(
                        Modifier
                            .background(dimensionBrush)
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
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    stringResource(R.string.reset_local_data_button),
                    color = V2Colors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.reset_local_data_hint),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
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

    selectedJournalTrend?.let { trend ->
        val title = catalogById[trend.quizId]?.title ?: trend.quizId
        DimensionJournalEngine.build(trend.quizId, trend.points)?.let { journal ->
            DimensionJournalDialog(
                title = title,
                journal = journal,
                onDismiss = { selectedJournalTrend = null }
            )
        }
    }

    selectedTraitId?.let { traitId ->
        TraitExplorationEngine.build(
            traitId = traitId,
            coverage = summary.coverage,
            graph = summary.traitGraph,
            catalog = catalog,
            completedQuizIds = summary.dimensions.mapTo(mutableSetOf()) { it.quizId }
        )?.let { exploration ->
            TraitExplorationDialog(
                exploration = exploration,
                catalogById = catalogById,
                onDismiss = { selectedTraitId = null },
                onStartQuiz = { quiz ->
                    selectedTraitId = null
                    onQuizSelected(quiz)
                }
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_local_data_title)) },
            text = { Text(stringResource(R.string.reset_local_data_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetLocalData()
                    }
                ) {
                    Text(stringResource(R.string.reset_local_data_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}