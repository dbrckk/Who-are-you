package com.whoareyou.app

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EvolutionPanel = Color(0xFF1B1D27)
private val EvolutionViolet = Color(0xFF9C7BFF)
private val EvolutionCyan = Color(0xFF6EE7F9)
private val EvolutionMuted = Color(0xFFA4A7B5)

@Composable
fun ProfileEvolutionCard(summary: GlobalProfileSummary) {
    val snapshot = ProfileEvolutionSummary.derive(summary.dimensions) ?: return
    val dimension = snapshot.mostChanged
    val change = dimension.scoreChange ?: return

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
            shape = RoundedCornerShape(22.dp)
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
                    trackColor = Color(0xFF14151D)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.evolution_snapshot_most_changed),
                    color = EvolutionViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
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
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
