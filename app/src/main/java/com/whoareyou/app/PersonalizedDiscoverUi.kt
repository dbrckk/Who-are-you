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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PersonalizedDiscoverDashboard(
    profile: GlobalProfileSummary,
    quizzes: List<Quiz>,
    completed: Set<String>,
    onOpenProfile: (() -> Unit)? = null,
    onQuizSelected: ((Quiz) -> Unit)? = null
) {
    val nextQuiz = remember(quizzes, completed, profile.dimensions) {
        DiscoverPersonalization.nextQuiz(quizzes, completed, profile.dimensions)
    }
    val strongest = remember(profile.dimensions) {
        DiscoverPersonalization.strongestDimension(profile.dimensions)
    }
    val accent = nextQuiz?.let(QuizVisuals::accentFor) ?: V2Colors.AccentViolet

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val profileModifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(V2Radius.Hero))
            .background(
                Brush.linearGradient(
                    listOf(
                        V2Colors.SurfaceElevated,
                        V2Colors.AccentViolet.copy(alpha = 0.16f),
                        V2Colors.AccentCyan.copy(alpha = 0.07f)
                    )
                )
            )
        Box(
            modifier = (if (onOpenProfile != null) profileModifier.clickable(onClick = onOpenProfile) else profileModifier)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.personalized_snapshot_label),
                            color = V2Colors.AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            profile.dominantArchetype,
                            color = V2Colors.TextPrimary,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(V2Colors.Ink.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${profile.completionPercent}%",
                            color = V2Colors.AccentViolet,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InsightMetric(Modifier.weight(1f), profile.completedCount.toString(), stringResource(R.string.personalized_dimensions))
                    InsightMetric(Modifier.weight(1f), (profile.totalCount - profile.completedCount).coerceAtLeast(0).toString(), stringResource(R.string.personalized_left))
                    InsightMetric(Modifier.weight(1f), strongest?.score?.let { "$it%" } ?: "—", stringResource(R.string.personalized_signal))
                }

                strongest?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(R.string.personalized_strongest, it.metricLabel),
                        color = V2Colors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        nextQuiz?.let { quiz ->
            val nextModifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(V2Radius.Card))
                .background(V2Colors.Surface)
            Row(
                modifier = (if (onQuizSelected != null) nextModifier.clickable { onQuizSelected(quiz) } else nextModifier)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(quiz.accent, fontSize = 23.sp)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 13.dp, end = 10.dp)) {
                    Text(stringResource(R.string.personalized_next_label), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                    Text(quiz.title, color = V2Colors.TextPrimary, fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Text(stringResource(R.string.personalized_next_reason), color = V2Colors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                }
                if (onQuizSelected != null) Text("→", color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
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
        Text(value, color = V2Colors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        Text(label, color = V2Colors.TextSecondary, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
    }
}
