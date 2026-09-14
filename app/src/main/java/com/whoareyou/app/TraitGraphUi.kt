package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TraitGraphCard(graph: TraitGraph, modifier: Modifier = Modifier) {
    val topTraits = remember(graph.traits) { graph.traits.take(5) }
    if (topTraits.isEmpty()) return

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Identity fingerprint",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Patterns supported by completed tests. Tendencies, not diagnoses.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            topTraits.forEach { trait ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = trait.label,
                            color = V2Colors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${trait.score}%",
                            color = V2Colors.TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    LinearProgressIndicator(
                        progress = { trait.score / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = V2Colors.Orchid,
                        trackColor = V2Colors.SurfaceElevated
                    )
                    Text(
                        text = "${trait.evidenceCount} supporting dimension${if (trait.evidenceCount == 1) "" else "s"}",
                        color = V2Colors.TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
