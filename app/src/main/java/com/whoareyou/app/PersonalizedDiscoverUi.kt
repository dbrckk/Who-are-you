package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PersonalizedDiscoverDashboard(
    profile: GlobalProfileSummary,
    quizzes: List<Quiz>,
    completed: Set<String>,
    onOpenProfile: (() -> Unit)? = null,
    onQuizSelected: ((Quiz) -> Unit)? = null
) {
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val recommendation = remember(quizzes, completed, profile.dimensions) {
        DiscoverPersonalization.recommendation(quizzes, completed, profile.dimensions)
    }
    val stage = remember(profile.completedCount, profile.signature) { DiscoverPersonalization.stage(profile) }
    val strongest = remember(profile.dimensions) { DiscoverPersonalization.strongestDimension(profile.dimensions) }
    val momentum = remember(profile.dimensions) { ProfileMomentumEngine.derive(profile.dimensions) }
    val strongestDimensions = remember(profile.dimensions) {
        profile.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }.take(2)
    }
    val nextQuiz = recommendation?.quiz
    val accent = nextQuiz?.let(QuizVisuals::accentFor) ?: V2Colors.AccentViolet
    val stageLabel = when (stage) {
        DiscoverProfileStage.NEW -> R.string.personalized_stage_new
        DiscoverProfileStage.EMERGING -> R.string.personalized_stage_emerging
        DiscoverProfileStage.MAPPED -> R.string.personalized_stage_mapped
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val profileModifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(V2Radius.Hero))
            .background(
                Brush.linearGradient(
                    listOf(V2Colors.SurfaceElevated, V2Colors.AccentViolet.copy(alpha = 0.16f), V2Colors.AccentCyan.copy(alpha = 0.07f))
                )
            )
        Box(
            modifier = (if (onOpenProfile != null) profileModifier.clickable(onClick = onOpenProfile) else profileModifier).padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(stageLabel), color = V2Colors.AccentCyan, style = V2Type.Eyebrow)
                        Spacer(Modifier.height(6.dp))
                        Text(profile.dominantArchetype, color = V2Colors.TextPrimary, style = V2Type.SectionTitle)
                    }
                    Box(
                        modifier = Modifier.size(62.dp).clip(CircleShape).background(V2Colors.Ink.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${profile.completionPercent}%", color = V2Colors.AccentViolet, style = V2Type.BodyStrong, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InsightMetric(Modifier.weight(1f), profile.completedCount.toString(), stringResource(R.string.personalized_dimensions))
                    InsightMetric(Modifier.weight(1f), (profile.totalCount - profile.completedCount).coerceAtLeast(0).toString(), stringResource(R.string.personalized_left))
                    InsightMetric(Modifier.weight(1f), strongest?.score?.let { "$it%" } ?: "—", stringResource(R.string.personalized_signal))
                }

                Spacer(Modifier.height(16.dp))
                ProfileMapPreview(dimensions = profile.dimensions)

                if (momentum.changingCount > 0 || momentum.stableCount > 0) {
                    Spacer(Modifier.height(14.dp))
                    ProfileMomentumCard(momentum)
                }

                profile.signature?.let { signature ->
                    val copy = SignatureProfiles.copy(signature.key, french)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(V2Radius.Compact))
                            .background(V2Colors.AccentViolet.copy(alpha = 0.10f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.personalized_signature_label), color = V2Colors.AccentViolet, style = V2Type.Caption)
                            Spacer(Modifier.height(3.dp))
                            Text(copy.title, color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                        }
                        Text(
                            stringResource(R.string.personalized_signature_confidence, signature.confidence),
                            color = V2Colors.TextSecondary,
                            style = V2Type.Caption
                        )
                    }
                }

                if (strongestDimensions.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.personalized_signals_label), color = V2Colors.TextSecondary, style = V2Type.Caption)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        strongestDimensions.forEach { dimension ->
                            DimensionSignal(Modifier.weight(1f), dimension.metricLabel, dimension.score)
                        }
                    }
                }
            }
        }

        if (nextQuiz != null) {
            val reason = when (recommendation.reason) {
                DiscoverRecommendationReason.NEW_THEME -> R.string.personalized_next_new_theme
                DiscoverRecommendationReason.UNFINISHED -> R.string.personalized_next_unfinished
                DiscoverRecommendationReason.RETAKE -> R.string.personalized_next_retake
            }
            V2PressableCard(
                onClick = onQuizSelected?.let { callback -> { callback(nextQuiz) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(V2Radius.Card),
                containerColor = V2Colors.Surface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nextQuiz.accent, style = V2Type.SectionTitle)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 13.dp, end = 10.dp)) {
                        Text(stringResource(R.string.personalized_next_label), color = accent, style = V2Type.Caption)
                        Spacer(Modifier.height(4.dp))
                        Text(nextQuiz.title, color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(reason), color = V2Colors.TextSecondary, style = V2Type.Supporting)
                    }
                    if (onQuizSelected != null) {
                        Text("→", color = accent, style = V2Type.SectionTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMetric(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(V2Radius.Compact))
            .background(V2Colors.Ink.copy(alpha = 0.48f))
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Text(value, color = V2Colors.TextPrimary, style = V2Type.Metric)
        Spacer(Modifier.height(2.dp))
        Text(label, color = V2Colors.TextSecondary, style = V2Type.Caption)
    }
}

@Composable
private fun DimensionSignal(modifier: Modifier, label: String, score: Int) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(V2Radius.Compact))
            .background(V2Colors.Ink.copy(alpha = 0.40f))
            .padding(11.dp)
    ) {
        Text(label, color = V2Colors.TextPrimary, style = V2Type.Supporting, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(
            "$score%",
            color = if (score >= 50) V2Colors.AccentCyan else V2Colors.AccentViolet,
            style = V2Type.Caption,
            fontWeight = FontWeight.Black
        )
    }
}
