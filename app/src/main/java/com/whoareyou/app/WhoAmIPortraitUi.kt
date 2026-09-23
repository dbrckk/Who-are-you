package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun WhoAmIPortraitCards(
    portrait: WhoAmIPortrait,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WhoAmICardModel.from(portrait).forEach { card ->
            when (card.section) {
                WhoAmISection.PORTRAIT -> WhoAmITraitCard(
                    titleRes = R.string.who_am_i_portrait,
                    tag = "who_am_i_portrait",
                    traits = card.traits,
                    emptyCopyRes = R.string.who_am_i_discovery_empty
                )
                WhoAmISection.STABLE -> WhoAmITraitCard(
                    titleRes = R.string.who_am_i_stable,
                    tag = "who_am_i_stable",
                    traits = card.traits
                )
                WhoAmISection.NUANCES -> WhoAmITraitCard(
                    titleRes = R.string.who_am_i_nuances,
                    tag = "who_am_i_nuances",
                    traits = card.traits
                )
                WhoAmISection.DISCOVERY -> WhoAmIDiscoveryCard(card.discoveryGaps)
                WhoAmISection.EVOLUTION -> WhoAmITraitCard(
                    titleRes = R.string.who_am_i_evolution,
                    tag = "who_am_i_evolution",
                    traits = card.traits,
                    showTrend = true
                )
            }
        }
    }
}

@Composable
private fun WhoAmITraitCard(
    titleRes: Int,
    tag: String,
    traits: List<PersonalTrait>,
    emptyCopyRes: Int? = null,
    showTrend: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        shape = RoundedCornerShape(V2Radius.Card),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong,
                modifier = Modifier.semantics { heading() }
            )
            if (traits.isEmpty()) {
                emptyCopyRes?.let {
                    Text(
                        text = stringResource(it),
                        color = V2Colors.TextSecondary,
                        style = V2Type.Body
                    )
                }
            } else {
                traits.forEach { trait ->
                    WhoAmITraitRow(trait, showTrend)
                }
            }
        }
    }
}

@Composable
private fun WhoAmITraitRow(
    trait: PersonalTrait,
    showTrend: Boolean = false
) {
    val row = WhoAmIUiModel.traitRow(trait)
    val label = row.labelRes?.let { stringResource(it) }
        ?: stringResource(R.string.who_am_i_trait_fallback)
    val certainty = stringResource(row.certaintyRes)
    val trendRes = if (showTrend) WhoAmIProgressModel.trendLabel(trait.trend) else null

    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = V2Colors.TextPrimary,
            style = V2Type.BodyStrong
        )
        Text(
            text = certainty,
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting
        )
        trendRes?.let {
            Text(
                text = stringResource(it),
                color = V2Colors.TextSecondary,
                style = V2Type.Body
            )
        }
    }
}

@Composable
private fun WhoAmIDiscoveryCard(gaps: List<TraitDomainCoverage>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        shape = RoundedCornerShape(V2Radius.Card),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("who_am_i_discovery")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.who_am_i_discovering),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong,
                modifier = Modifier.semantics { heading() }
            )
            if (gaps.isEmpty()) {
                Text(
                    text = stringResource(R.string.who_am_i_discovery_empty),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Body
                )
            } else {
                gaps.take(3).forEach { gap ->
                    val domain = stringResource(WhoAmIProgressModel.domainLabel(gap.domain))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = domain,
                            color = V2Colors.TextPrimary,
                            style = V2Type.BodyStrong
                        )
                        Text(
                            text = stringResource(R.string.who_am_i_gap_copy, domain),
                            color = V2Colors.TextSecondary,
                            style = V2Type.Body
                        )
                    }
                }
            }
        }
    }
}
