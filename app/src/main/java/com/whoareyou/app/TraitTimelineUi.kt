package com.whoareyou.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun TraitTimelineCard(
    timelines: List<TraitTimeline>,
    catalog: List<Quiz>,
    modifier: Modifier = Modifier
) {
    val visible = remember(timelines) {
        timelines.filter { it.points.size >= 2 }.take(4)
    }
    if (visible.isEmpty()) return

    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val catalogById = remember(catalog) { catalog.associateBy { it.id } }
    var selected by remember { mutableStateOf<TraitTimeline?>(null) }

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (french) "Historique des traits" else "Trait history",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Ces tendances fusionnent les quiz qui contribuent au même trait."
                else
                    "These trends combine quizzes that contribute to the same trait.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            visible.forEach { timeline ->
                val latest = timeline.points.last()
                val label = TraitLocalization.label(timeline.traitId, french)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = timeline },
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = V2Colors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${latest.score}% · ${latest.confidence}%",
                            color = V2Colors.TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TraitTimelineSparkline(
                        points = timeline.points.map { it.score },
                        description = "$label. ${traitTrendText(timeline.trend.kind, french)}"
                    )
                    Text(
                        text = traitTrendText(timeline.trend.kind, french),
                        color = V2Colors.TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    selected?.let { timeline ->
        TraitTimelineDialog(
            timeline = timeline,
            catalogById = catalogById,
            onDismiss = { selected = null }
        )
    }
}

@Composable
private fun TraitTimelineDialog(
    timeline: TraitTimeline,
    catalogById: Map<String, Quiz>,
    onDismiss: () -> Unit
) {
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(if (french) Locale.FRENCH else Locale.ENGLISH)
    val label = TraitLocalization.label(timeline.traitId, french)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = traitTrendText(timeline.trend.kind, french),
                    color = V2Colors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                timeline.points.asReversed().forEach { point ->
                    val date = if (point.epochDay > 0) {
                        LocalDate.ofEpochDay(point.epochDay).format(formatter)
                    } else {
                        if (french) "Date non disponible" else "Date unavailable"
                    }

                    val retakes = point.retakeQuizIds
                        .mapNotNull { catalogById[it]?.title }
                        .distinct()
                    val newEvidence = point.newEvidenceQuizIds
                        .mapNotNull { catalogById[it]?.title }
                        .distinct()

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = date,
                                color = V2Colors.TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (french)
                                    "${point.score}% · confiance ${point.confidence}%"
                                else
                                    "${point.score}% · confidence ${point.confidence}%",
                                color = V2Colors.TextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (retakes.isNotEmpty()) {
                            Text(
                                text = if (french)
                                    "Retake : ${retakes.joinToString(", ")}"
                                else
                                    "Retake: ${retakes.joinToString(", ")}",
                                color = V2Colors.TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (newEvidence.isNotEmpty()) {
                            Text(
                                text = if (french)
                                    "Nouvelle preuve : ${newEvidence.joinToString(", ")}"
                                else
                                    "New evidence: ${newEvidence.joinToString(", ")}",
                                color = V2Colors.TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (point.contradictoryEvidenceCount > 0) {
                            Text(
                                text = if (french)
                                    "${point.contradictoryEvidenceCount} signal contradictoire"
                                else
                                    "${point.contradictoryEvidenceCount} contradictory signal",
                                color = V2Colors.TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (french) "Fermer" else "Close")
            }
        }
    )
}

@Composable
private fun TraitTimelineSparkline(
    points: List<Int>,
    description: String
) {
    val lineColor = V2Colors.Orchid
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
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
                    val dots = points.mapIndexed { index, value ->
                        val point = Offset(
                            x = step * index,
                            y = size.height - ((value - min) / range) * size.height
                        )
                        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                        point
                    }
                    onDrawBehind {
                        drawPath(path, color = lineColor, style = Stroke(width = 4f))
                        dots.forEach { drawCircle(lineColor, radius = 4f, center = it) }
                    }
                }
            }
    )
}

private fun traitTrendText(kind: LongitudinalTrendKind, french: Boolean): String = when (kind) {
    LongitudinalTrendKind.RISING -> if (french) "Tendance haussière" else "Rising trend"
    LongitudinalTrendKind.FALLING -> if (french) "Tendance baissière" else "Falling trend"
    LongitudinalTrendKind.STABLE -> if (french) "Globalement stable" else "Mostly stable"
    LongitudinalTrendKind.VOLATILE -> if (french) "Très variable" else "Highly variable"
    LongitudinalTrendKind.OUTLIER -> if (french) "Dernier point atypique" else "Latest point looks atypical"
    LongitudinalTrendKind.INSUFFICIENT -> if (french) "Pas assez de mesures" else "Not enough measurements"
}
