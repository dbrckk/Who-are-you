package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun TraitEvolutionCard(
    evolution: TraitEvolutionSummary,
    catalog: List<Quiz>,
    modifier: Modifier = Modifier
) {
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val catalogById = remember(catalog) { catalog.associateBy { it.id } }
    val visible = remember(evolution) {
        (evolution.meaningfulChanges.take(3) + evolution.newEvidence.take(3))
            .distinctBy { it.traitId }
            .take(5)
    }
    if (visible.isEmpty()) return

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (french) "Évolution des traits" else "Trait evolution",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Les nouvelles preuves sont séparées des changements observés sur des traits déjà mesurés."
                else
                    "New evidence is separated from changes in traits that were already measured.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            visible.forEach { item ->
                TraitEvolutionRow(
                    item = item,
                    french = french,
                    catalogById = catalogById
                )
            }
        }
    }
}

@Composable
private fun TraitEvolutionRow(
    item: TraitEvolution,
    french: Boolean,
    catalogById: Map<String, Quiz>
) {
    val label = TraitLocalization.label(item.traitId, french)
    val detail = when (item.kind) {
        TraitEvolutionKind.NEW_EVIDENCE -> {
            val source = item.newEvidenceQuizIds
                .mapNotNull { catalogById[it]?.title }
                .distinct()
                .take(2)
                .joinToString(", ")
            if (french) {
                if (source.isNotBlank())
                    "Nouvelle information via $source · confiance ${item.currentConfidence}%"
                else
                    "Nouvelle information · confiance ${item.currentConfidence}%"
            } else {
                if (source.isNotBlank())
                    "New evidence via $source · confidence ${item.currentConfidence}%"
                else
                    "New evidence · confidence ${item.currentConfidence}%"
            }
        }

        TraitEvolutionKind.MOVED -> {
            val delta = item.delta ?: 0
            val sign = if (delta > 0) "+" else "−"
            if (french)
                "Déplacement observé · $sign${abs(delta)} pts · confiance ${item.currentConfidence}%"
            else
                "Observed movement · $sign${abs(delta)} pts · confidence ${item.currentConfidence}%"
        }

        TraitEvolutionKind.STABLE -> if (french)
            "Stable · confiance ${item.currentConfidence}%"
        else
            "Stable · confidence ${item.currentConfidence}%"

        TraitEvolutionKind.LOW_CONFIDENCE -> if (french)
            "Signal encore trop faible pour conclure"
        else
            "Signal still too weak to interpret"

        TraitEvolutionKind.CONTRADICTORY -> if (french)
            "Signaux contradictoires · prudence d’interprétation"
        else
            "Contradictory signals · interpret cautiously"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                color = V2Colors.TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Text(
            text = "${item.currentScore}%",
            color = V2Colors.TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
