package com.whoareyou.app

import androidx.compose.foundation.clickable
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
fun ProfileKnowledgeMapCard(
    coverage: ProfileCoverage,
    modifier: Modifier = Modifier,
    onTraitClick: (String) -> Unit = {}
) {
    if (coverage.totalTraitCount == 0) return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val map = remember(coverage) { ProfileKnowledgeMapEngine.build(coverage) }

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (french) "Carte de connaissance" else "Knowledge map",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french)
                    "Visualise les zones déjà solides, celles à approfondir et celles encore inconnues."
                else
                    "See which areas are well supported, still developing, or not explored yet.",
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            map.domains.forEach { domain ->
                DomainCoverageRow(domain, french)
            }

            KnowledgeSection(
                title = if (french) "BIEN DOCUMENTÉ" else "WELL SUPPORTED",
                traits = map.strong.take(5),
                french = french,
                onTraitClick = onTraitClick
            )
            KnowledgeSection(
                title = if (french) "À APPROFONDIR" else "DEVELOPING",
                traits = map.developing.take(5),
                french = french,
                onTraitClick = onTraitClick
            )
            KnowledgeSection(
                title = if (french) "INCONNU" else "UNEXPLORED",
                traits = map.unknown.take(5),
                french = french,
                onTraitClick = onTraitClick
            )
        }
    }
}

@Composable
private fun DomainCoverageRow(item: TraitDomainCoverage, french: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = domainLabel(item.domain, french),
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${item.knownCount}/${item.totalCount}",
                color = V2Colors.TextMuted,
                style = MaterialTheme.typography.labelMedium
            )
        }
        LinearProgressIndicator(
            progress = { item.coveragePercent / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = V2Colors.AccentCyan,
            trackColor = V2Colors.SurfaceElevated
        )
    }
}

@Composable
private fun KnowledgeSection(
    title: String,
    traits: List<TraitCoverage>,
    french: Boolean,
    onTraitClick: (String) -> Unit
) {
    if (traits.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            color = V2Colors.TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        traits.forEach { trait ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTraitClick(trait.traitId) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = TraitLocalization.label(trait.traitId, french),
                    color = V2Colors.TextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = when (trait.status) {
                        CoverageStatus.UNKNOWN -> "—"
                        else -> "${trait.confidence}%"
                    },
                    color = V2Colors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun domainLabel(domain: TraitDomain, french: Boolean): String = when (domain) {
    TraitDomain.SOCIAL -> if (french) "Social" else "Social"
    TraitDomain.EMOTIONAL -> if (french) "Émotionnel" else "Emotional"
    TraitDomain.THINKING -> if (french) "Réflexion" else "Thinking"
    TraitDomain.GROWTH -> if (french) "Exploration" else "Growth"
    TraitDomain.SELF_MANAGEMENT -> if (french) "Autorégulation" else "Self-management"
}
