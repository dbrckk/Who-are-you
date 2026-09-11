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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultInterpretationPanel(quiz: Quiz, score: Int) {
    val summary = remember(score) { ResultInterpretationEngine.derive(score) }
    val accent = QuizVisuals.accentFor(quiz)
    val companion = QuizVisuals.companionAccentFor(quiz)
    val animatedNuance by animateFloatAsState(
        targetValue = summary.nuancePercent / 100f,
        animationSpec = tween(V2Motion.EmphasizedMillis),
        label = "resultNuanceProgress"
    )
    val pole = if (summary.direction == ResultDirection.LOW) quiz.metricLow else quiz.metricHigh
    val strengthLabel = stringResource(
        when (summary.strength) {
            ResultSignalStrength.BALANCED -> R.string.result_signal_balanced
            ResultSignalStrength.CLEAR -> R.string.result_signal_clear
            ResultSignalStrength.STRONG -> R.string.result_signal_strong
        }
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.result_interpretation_title),
            color = V2Colors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.result_interpretation_subtitle),
            color = V2Colors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(V2Radius.Card),
            colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.result_signal_label),
                            color = companion,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            strengthLabel,
                            color = V2Colors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${summary.distanceFromNeutral}",
                        color = accent,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.result_pole_label),
                    color = V2Colors.TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(pole, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.result_nuance_label),
                        color = V2Colors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${summary.nuancePercent}%",
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedNuance },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = accent,
                    trackColor = Color.White.copy(alpha = 0.06f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        when (summary.strength) {
                            ResultSignalStrength.BALANCED -> R.string.result_nuance_balanced_body
                            ResultSignalStrength.CLEAR -> R.string.result_nuance_clear_body
                            ResultSignalStrength.STRONG -> R.string.result_nuance_strong_body
                        }
                    ),
                    color = V2Colors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(V2Colors.Surface, RoundedCornerShape(V2Radius.Compact))
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.result_context_label),
                color = companion,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.result_context_body),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
