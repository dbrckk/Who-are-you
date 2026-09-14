package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun ProfileNarrativeCard(
    summary: ProfileNarrativeSummary,
    modifier: Modifier = Modifier
) {
    if (summary.insights.isEmpty()) return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (french) "Ce que montrent tes mesures" else "What your measurements show",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Synthèse factuelle des retakes et des preuves disponibles, sans diagnostic."
                else
                    "A factual summary of available retakes and evidence, without diagnosis.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            summary.insights.forEach { insight ->
                Text(
                    text = narrativeText(insight, french),
                    color = V2Colors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun narrativeText(
    insight: ProfileNarrativeInsight,
    french: Boolean
): String {
    val trait = TraitLocalization.label(insight.traitId, french)
    val signedScore = signed(insight.scoreDelta)
    val signedConfidence = signed(insight.confidenceDelta)

    return when (insight.kind) {
        ProfileNarrativeKind.REPEATED_MOVEMENT -> if (french) {
            "$trait : les mesures récentes diffèrent de $signedScore points en moyenne par rapport aux précédentes. La tendance repose sur plusieurs mesures ; confiance actuelle ${insight.currentConfidence}%."
        } else {
            "$trait: recent measurements differ by $signedScore points on average from earlier ones. The trend is based on multiple measurements; current confidence ${insight.currentConfidence}%."
        }

        ProfileNarrativeKind.STABLE_WITH_MORE_EVIDENCE -> if (french) {
            "$trait : le score reste globalement stable (écart moyen ${abs(insight.scoreDelta)} points), tandis que la confiance augmente de $signedConfidence points."
        } else {
            "$trait: the score remains broadly stable (average difference ${abs(insight.scoreDelta)} points), while confidence changes by $signedConfidence points."
        }

        ProfileNarrativeKind.BETTER_DOCUMENTED -> if (french) {
            "$trait : ce trait est maintenant mieux documenté par ${insight.evidenceCount} source(s) de quiz ; confiance actuelle ${insight.currentConfidence}%."
        } else {
            "$trait: this trait is now better documented by ${insight.evidenceCount} quiz source(s); current confidence ${insight.currentConfidence}%."
        }

        ProfileNarrativeKind.VOLATILE -> if (french) {
            "$trait : les mesures varient fortement. Il n’y a pas encore de direction suffisamment stable à mettre en avant."
        } else {
            "$trait: measurements vary substantially. There is not yet a stable enough direction to highlight."
        }

        ProfileNarrativeKind.CONTRADICTORY -> if (french) {
            "$trait : plusieurs sources ne vont pas dans le même sens. Le profil conserve cette contradiction au lieu de conclure."
        } else {
            "$trait: several sources point in different directions. The profile preserves that contradiction instead of drawing a conclusion."
        }

        ProfileNarrativeKind.LOW_CONFIDENCE -> if (french) {
            "$trait : les données disponibles restent insuffisamment solides pour décrire une évolution avec confiance."
        } else {
            "$trait: the available data is not yet strong enough to describe a change with confidence."
        }
    }
}

private fun signed(value: Int): String = if (value > 0) "+$value" else "$value"
