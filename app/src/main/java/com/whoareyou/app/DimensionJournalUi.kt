package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun DimensionJournalDialog(
    title: String,
    journal: DimensionJournal,
    onDismiss: () -> Unit
) {
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val locale = if (french) Locale.FRENCH else Locale.ENGLISH
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                JournalOverview(journal = journal, french = french)

                Text(
                    text = if (french) "HISTORIQUE" else "HISTORY",
                    color = V2Colors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                journal.entries.asReversed().forEachIndexed { reverseIndex, entry ->
                    val isLatest = reverseIndex == 0
                    JournalEntryRow(
                        entry = entry,
                        isLatest = isLatest,
                        french = french,
                        formatter = formatter
                    )
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
private fun JournalOverview(
    journal: DimensionJournal,
    french: Boolean
) {
    val total = journal.totalChange
    val signed = when {
        total > 0 -> "+$total"
        else -> "$total"
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (french) "Première mesure" else "First measurement",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${journal.firstScore}%",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (french) "Mesure actuelle" else "Current measurement",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${journal.latestScore}%  ($signed)",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        journal.periodComparison?.let { comparison ->
            val periodDelta = comparison.delta
            val periodSigned = if (periodDelta > 0) "+$periodDelta" else "$periodDelta"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (french) "Ancienne période" else "Earlier period",
                    color = V2Colors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${comparison.earlierAverage}%",
                    color = V2Colors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (french) "Période récente" else "Recent period",
                    color = V2Colors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${comparison.recentAverage}%  ($periodSigned)",
                    color = V2Colors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = journalTrendExplanation(journal.trendKind, french),
            color = V2Colors.TextMuted,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    isLatest: Boolean,
    french: Boolean,
    formatter: DateTimeFormatter
) {
    val date = if (entry.epochDay > 0) {
        LocalDate.ofEpochDay(entry.epochDay).format(formatter)
    } else {
        if (french) "Date non disponible" else "Date unavailable"
    }
    val delta = entry.deltaFromPrevious
    val deltaText = when {
        delta == null -> if (french) "Première mesure" else "First measurement"
        delta > 0 -> "+$delta"
        else -> "$delta"
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isLatest) {
                    if (french) "$date · actuelle" else "$date · current"
                } else {
                    date
                },
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${entry.score}%",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "$deltaText · ${journalEntryExplanation(entry.kind, french)}",
            color = V2Colors.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun journalEntryExplanation(kind: JournalChangeKind, french: Boolean): String = when (kind) {
    JournalChangeKind.FIRST_MEASUREMENT ->
        if (french) "point de départ" else "baseline"
    JournalChangeKind.RETAKE_CHANGE ->
        if (french) "variation notable au retake" else "notable retake change"
    JournalChangeKind.RETAKE_STABLE ->
        if (french) "retake globalement stable" else "mostly stable retake"
    JournalChangeKind.OUTLIER ->
        if (french) "score isolé possible" else "possible isolated score"
    JournalChangeKind.VOLATILE ->
        if (french) "série très variable" else "highly variable series"
}

private fun journalTrendExplanation(
    kind: LongitudinalTrendKind,
    french: Boolean
): String = when (kind) {
    LongitudinalTrendKind.RISING ->
        if (french) "La série montre une tendance haussière répétée." else "The series shows a repeated upward trend."
    LongitudinalTrendKind.FALLING ->
        if (french) "La série montre une tendance baissière répétée." else "The series shows a repeated downward trend."
    LongitudinalTrendKind.STABLE ->
        if (french) "Les mesures restent globalement stables." else "Measurements remain broadly stable."
    LongitudinalTrendKind.VOLATILE ->
        if (french) "La série varie fortement ; un score isolé doit être interprété avec prudence." else "The series varies widely; isolated scores should be interpreted cautiously."
    LongitudinalTrendKind.OUTLIER ->
        if (french) "La dernière mesure s’écarte nettement de l’historique récent." else "The latest measurement differs sharply from recent history."
    LongitudinalTrendKind.INSUFFICIENT ->
        if (french) "Pas encore assez de mesures pour une tendance." else "Not enough measurements for a trend yet."
}
