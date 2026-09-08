package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = V2Spacing.Screen),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.back),
                color = V2Colors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(Modifier.height(26.dp))
            Text(stringResource(R.string.your_profile), color = V2Colors.AccentViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(summary.dominantArchetype.uppercase(), color = V2Colors.TextPrimary, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("${summary.completedCount}/${summary.totalCount} dimensions • ${summary.completionPercent}%", color = V2Colors.TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    AppEvents.profileShare(summary.dominantArchetype, summary.completedCount)
                    GlobalProfileShare.share(context, summary)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentViolet),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.share_my_profile), fontWeight = FontWeight.Black)
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
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.compare_profile_friend), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (strongestDimension == null) stringResource(R.string.complete_test_unlock_compare)
                else stringResource(R.string.starts_with_dimension, strongestDimension.title),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (summary.dimensions.size >= 5) {
                Spacer(Modifier.height(18.dp))
                ProfileInsightCards(summary)
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.all_dimensions), color = V2Colors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (summary.dimensions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(stringResource(R.string.profile_undiscovered), color = V2Colors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Text(stringResource(R.string.complete_first_test_profile), color = V2Colors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        } else {
            items(summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }, key = { it.quizId }) { dimension ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(dimension.title.uppercase(), color = V2Colors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(3.dp))
                                Text(dimension.resultTitle, color = V2Colors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${dimension.score}%", color = V2Colors.AccentViolet, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { dimension.score / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = V2Colors.AccentViolet,
                            trackColor = V2Colors.Hairline
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(dimension.metricLabel, color = V2Colors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        dimension.scoreChange?.let { change ->
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.profile_evolution), color = V2Colors.AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.profile_evolution_values, change.previousScore, change.currentScore),
                                color = V2Colors.TextPrimary,
                                fontSize = 12.sp,
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
