package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileCoverageCard(
    coverage: ProfileCoverage,
    modifier: Modifier = Modifier
) {
    if (coverage.totalTraitCount == 0) return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (french) "Couverture du profil" else "Profile coverage",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (french) {
                    "Ce score mesure combien de traits sont déjà documentés, pas à quel point ton profil est « bon »."
                } else {
                    "This measures how much of your profile is documented, not how “good” your profile is."
                },
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            LinearProgressIndicator(
                progress = { coverage.coveragePercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = V2Colors.AccentCyan,
                trackColor = V2Colors.SurfaceElevated
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoverageStat(
                    value = "${coverage.coveragePercent}%",
                    label = if (french) "couvert" else "covered"
                )
                CoverageStat(
                    value = "${coverage.averageConfidence}%",
                    label = if (french) "confiance moy." else "avg confidence"
                )
                CoverageStat(
                    value = "${coverage.strongTraitCount}",
                    label = if (french) "traits solides" else "strong traits"
                )
            }

            Text(
                text = if (french) {
                    "${coverage.uncertainTraitCount} trait${if (coverage.uncertainTraitCount == 1) "" else "s"} encore peu documenté${if (coverage.uncertainTraitCount == 1) "" else "s"} ou contradictoire${if (coverage.uncertainTraitCount == 1) "" else "s"}."
                } else {
                    "${coverage.uncertainTraitCount} trait${if (coverage.uncertainTraitCount == 1) "" else "s"} still under-documented or contradictory."
                },
                color = V2Colors.TextMuted,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CoverageStat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            color = V2Colors.TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = V2Colors.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
