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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TraitGraphCard(graph: TraitGraph, modifier: Modifier = Modifier) {
    val topTraits = remember(graph.traits) { graph.traits.take(5) }
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    if (topTraits.isEmpty()) return

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (french) "Empreinte d’identité" else "Identity fingerprint",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Tendances appuyées par tes tests terminés. Ce ne sont pas des diagnostics."
                else
                    "Patterns supported by completed tests. Tendencies, not diagnoses.",
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
                            text = TraitLocalization.label(trait.id, french),
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
                        text = if (french) {
                            "${trait.evidenceCount} dimension${if (trait.evidenceCount == 1) "" else "s"} contributive${if (trait.evidenceCount == 1) "" else "s"} · confiance ${trait.confidence}%"
                        } else {
                            "${trait.evidenceCount} supporting dimension${if (trait.evidenceCount == 1) "" else "s"} · confidence ${trait.confidence}%"
                        },
                        color = V2Colors.TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (trait.contradictoryEvidenceCount > 0) {
                        Text(
                            text = if (french)
                                "${trait.contradictoryEvidenceCount} signal contradictoire détecté"
                            else
                                "${trait.contradictoryEvidenceCount} contradictory signal detected",
                            color = V2Colors.TextMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
