package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ResultEvidenceCard(
    evidence: List<ResultEvidence>,
    modifier: Modifier = Modifier
) {
    if (evidence.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_evidence")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.m771_why_result),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong
            )
            evidence.forEach { item ->
                Column {
                    Text(
                        text = item.questionText,
                        color = V2Colors.TextPrimary,
                        style = V2Type.BodyStrong
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.answerText,
                        color = V2Colors.TextSecondary,
                        style = V2Type.Supporting
                    )
                }
            }
        }
    }
}

@Composable
fun ResultInsightCards(
    insight: ResultInsightSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_strengths_watchouts")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(stringResource(R.string.m771_strengths), color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                insight.strengths.forEach {
                    Text(it, color = V2Colors.TextSecondary, style = V2Type.Body)
                }
                Text(stringResource(R.string.m771_watchouts), color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                insight.watchOuts.forEach {
                    Text(it, color = V2Colors.TextSecondary, style = V2Type.Body)
                }
            }
        }

        if (insight.everydayLife.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_everyday_life")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(stringResource(R.string.m771_everyday_life), color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                    insight.everydayLife.forEach {
                        Text(it, color = V2Colors.TextSecondary, style = V2Type.Body)
                    }
                }
            }
        }

        if (insight.reflection.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_reflection")
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.m771_reflection), color = V2Colors.TextPrimary, style = V2Type.BodyStrong)
                    Spacer(Modifier.height(6.dp))
                    Text(insight.reflection, color = V2Colors.TextSecondary, style = V2Type.Body)
                }
            }
        }
    }
}

@Composable
fun ResultProfileConnectionsCard(
    connections: List<ResultProfileConnection>,
    modifier: Modifier = Modifier
) {
    if (connections.isEmpty()) return

    val language = LocalConfiguration.current.locales[0]?.language
    val french = language.equals("fr", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_profile_connections")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.m771_profile_connections),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong
            )
            connections.forEach { connection ->
                val label = TraitLocalization.label(connection.traitId, french)
                val message = when (connection.kind) {
                    ResultProfileConnectionKind.REINFORCING ->
                        stringResource(R.string.m771_connection_reinforcing, label)
                    ResultProfileConnectionKind.CONTRASTING ->
                        stringResource(R.string.m771_connection_contrasting, label)
                    ResultProfileConnectionKind.CONTEXTUAL ->
                        stringResource(R.string.m771_connection_contextual, label)
                }
                Text(
                    text = message,
                    color = V2Colors.TextSecondary,
                    style = V2Type.Body
                )
            }
        }
    }
}
