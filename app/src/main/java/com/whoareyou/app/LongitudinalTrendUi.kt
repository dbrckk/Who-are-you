package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun LongitudinalTrendsCard(
    trends: List<LongitudinalTrend>,
    catalog: List<Quiz>,
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val catalogById = remember(catalog) { catalog.associateBy { it.id } }
    val visible = remember(trends) { trends.take(4) }

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (french) "Tendances dans le temps" else "Trends over time",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Les courbes distinguent une tendance répétée d’un score isolé."
                else
                    "These curves separate repeated trends from isolated scores.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            visible.forEach { trend ->
                val title = catalogById[trend.quizId]?.title ?: trend.quizId
                TrendRow(title = title, trend = trend, french = french)
            }
        }
    }
}

@Composable
private fun TrendRow(
    title: String,
    trend: LongitudinalTrend,
    french: Boolean
) {
    val summary = trendSummary(trend, french)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (trend.netChange > 0) "+${trend.netChange}" else "${trend.netChange}",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
        }

        TrendSparkline(
            points = trend.points.map { it.score },
            description = "$title. $summary"
        )

        Text(
            text = summary,
            color = V2Colors.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TrendSparkline(
    points: List<Int>,
    description: String
) {
    val lineColor = V2Colors.AccentCyan
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .semantics { contentDescription = description }
            .drawWithCache {
                if (points.size < 2) {
                    onDrawBehind { }
                } else {
                    val min = points.minOrNull() ?: 0
                    val max = points.maxOrNull() ?: 100
                    val range = (max - min).coerceAtLeast(1).toFloat()
                    val step = size.width / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { index, value ->
                        val x = step * index
                        val y = size.height - ((value - min) / range) * size.height
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    val dots = points.mapIndexed { index, value ->
                        Offset(
                            x = step * index,
                            y = size.height - ((value - min) / range) * size.height
                        )
                    }

                    onDrawBehind {
                        drawPath(path = path, color = lineColor, style = Stroke(width = 4f))
                        dots.forEach { drawCircle(color = lineColor, radius = 4f, center = it) }
                    }
                }
            }
    )
}

private fun trendSummary(trend: LongitudinalTrend, french: Boolean): String = when (trend.kind) {
    LongitudinalTrendKind.RISING -> if (french)
        "Tendance haussière sur ${trend.points.size} mesures."
    else
        "Rising trend across ${trend.points.size} measurements."

    LongitudinalTrendKind.FALLING -> if (french)
        "Tendance baissière sur ${trend.points.size} mesures."
    else
        "Falling trend across ${trend.points.size} measurements."

    LongitudinalTrendKind.STABLE -> if (french)
        "Profil globalement stable sur ${trend.points.size} mesures."
    else
        "Mostly stable across ${trend.points.size} measurements."

    LongitudinalTrendKind.VOLATILE -> if (french)
        "Variation importante entre les mesures ; évite de surinterpréter un seul score."
    else
        "Large variation between measurements; avoid over-interpreting a single score."

    LongitudinalTrendKind.OUTLIER -> if (french)
        "La dernière mesure ressemble à un score isolé plutôt qu’à une tendance confirmée."
    else
        "The latest measurement looks more like an isolated score than a confirmed trend."

    LongitudinalTrendKind.INSUFFICIENT -> if (french)
        "Pas encore assez de mesures."
    else
        "Not enough measurements yet."
}
