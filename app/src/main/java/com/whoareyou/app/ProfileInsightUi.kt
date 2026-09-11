package com.whoareyou.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InsightPanel = Color(0xFF1B1D27)
private val InsightViolet = Color(0xFF9C7BFF)
private val InsightCyan = Color(0xFF6EE7F9)
private val InsightMuted = Color(0xFFA4A7B5)

private data class InsightCopy(val title: String, val body: String)

@Composable
fun ProfileInsightCards(summary: GlobalProfileSummary) {
    val signature = summary.signature
    val insights = ProfileInsights.derive(summary.dimensions)
    val evolution = ProfileEvolutionSummary.derive(summary.dimensions)
    val hasVisualMap = summary.dimensions.size >= 3
    if (!hasVisualMap && signature == null && insights.isEmpty() && evolution == null) return

    LaunchedEffect(signature?.key) {
        if (signature != null) AppEvents.signatureUnlock(signature)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasVisualMap) {
            ProfileIdentityMap(summary)
            Spacer(Modifier.height(18.dp))
            ProfileStrengthNuanceCard(summary)
            Spacer(Modifier.height(18.dp))
        }

        if (signature != null) {
            val copy = localizedSignatureProfileCopy(signature.key)
            Text(
                text = stringResource(R.string.profile_insight_signature),
                color = InsightCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = InsightPanel),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(copy.title, color = InsightViolet, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    Text(copy.description, color = Color.White, fontSize = 14.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(9.dp))
                    Text(
                        text = stringResource(R.string.profile_insight_match_confidence, signature.confidence),
                        color = InsightMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (evolution != null) {
            ProfileEvolutionCard(summary)
        }

        if (insights.isNotEmpty()) {
            Text(
                text = stringResource(R.string.profile_insight_reveals),
                color = InsightCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            insights.forEach { key ->
                val copy = localizedCopy(key)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = InsightPanel),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(copy.title, color = InsightViolet, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(6.dp))
                        Text(copy.body, color = InsightMuted, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Text(
            text = stringResource(R.string.profile_insight_disclaimer),
            color = InsightMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun localizedCopy(key: ProfileInsightKey): InsightCopy = when (key) {
    ProfileInsightKey.STRONG_SIGNATURE -> InsightCopy(
        stringResource(R.string.profile_insight_strong_title),
        stringResource(R.string.profile_insight_strong_body)
    )
    ProfileInsightKey.BALANCED_CORE -> InsightCopy(
        stringResource(R.string.profile_insight_balanced_title),
        stringResource(R.string.profile_insight_balanced_body)
    )
    ProfileInsightKey.BOLD_CONTRAST -> InsightCopy(
        stringResource(R.string.profile_insight_contrast_title),
        stringResource(R.string.profile_insight_contrast_body)
    )
}
