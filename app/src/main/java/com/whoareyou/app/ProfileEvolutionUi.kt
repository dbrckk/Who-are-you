package com.whoareyou.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EvolutionPanel = V2Colors.SurfaceElevated
private val EvolutionViolet = V2Colors.Violet
private val EvolutionCyan = V2Colors.Cyan
private val EvolutionMuted = V2Colors.TextSecondary

@Composable
fun ProfileEvolutionCard(summary: GlobalProfileSummary) {
    val snapshot = ProfileEvolutionSummary.derive(summary.dimensions) ?: return
    val dimension = snapshot.mostChanged
    val change = dimension.scoreChange ?: return
    val tracked = summary.dimensions
        .filter { it.scoreChange != null }
        .sortedByDescending { it.scoreChange?.absoluteDelta ?: 0 }
        .take(3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.evolution_snapshot_title),
            color = EvolutionCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EvolutionPanel),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    stringResource(
                        R.string.evolution_snapshot_coverage,
                        snapshot.trackedCount,
                        snapshot.totalDimensions,
                        snapshot.coveragePercent
                    ),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(9.dp))
                LinearProgressIndicator(
                    progress = { snapshot.coveragePercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = EvolutionViolet,
                    trackColor = V2Colors.Ink
                )

                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.evolution_snapshot_most_changed),
                    color = EvolutionViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(dimension.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))

                EvolutionCompareBar(
                    label = stringResource(R.string.evolution_before),
                    value = change.previousScore,
                    color = EvolutionMuted
                )
                Spacer(Modifier.height(9.dp))
                EvolutionCompareBar(
                    label = stringResource(R.string.evolution_now),
                    value = change.currentScore,
                    color = EvolutionCyan
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    when (change.direction) {
                        ScoreChangeDirection.HIGHER -> stringResource(
                            R.string.evolution_snapshot_higher,
                            dimension.title,
                            change.absoluteDelta
                        )
                        ScoreChangeDirection.LOWER -> stringResource(
                            R.string.evolution_snapshot_lower,
                            dimension.title,
                            change.absoluteDelta
                        )
                        ScoreChangeDirection.SAME -> stringResource(
                            R.string.evolution_snapshot_same,
                            dimension.title
                        )
                    },
                    color = EvolutionMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                if (tracked.size > 1) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        stringResource(R.string.evolution_other_movements),
                        color = EvolutionMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    tracked.drop(1).forEach { item ->
                        val itemChange = item.scoreChange ?: return@forEach
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.045f), RoundedCornerShape(15.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.title, modifier = Modifier.weight(1f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                when (itemChange.direction) {
                                    ScoreChangeDirection.HIGHER -> "+${itemChange.absoluteDelta}"
                                    ScoreChangeDirection.LOWER -> "−${itemChange.absoluteDelta}"
                                    ScoreChangeDirection.SAME -> "="
                                },
                                color = EvolutionCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EvolutionCompareBar(label: String, value: Int, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0, 100) / 100f,
        animationSpec = tween(V2Motion.EmphasizedMillis),
        label = "evolutionCompareProgress"
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = EvolutionMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text("$value%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.height(5.dp))
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth().height(7.dp),
        color = color,
        trackColor = V2Colors.Hairline
    )
}
