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
import androidx.compose.ui.platform.LocalConfiguration
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
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
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
        }

        if (signature != null) {
            val copy = SignatureProfiles.copy(signature.key, french)
            Text(
                text = if (french) "TA SIGNATURE" else "YOUR SIGNATURE",
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
                        text = if (french) "Confiance de correspondance : ${signature.confidence}%" else "Match confidence: ${signature.confidence}%",
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
                text = if (french) "CE QUE TON PROFIL RÉVÈLE" else "WHAT YOUR PROFILE REVEALS",
                color = InsightCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            insights.forEach { key ->
                val copy = localizedCopy(key, french)
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
            text = if (french) "Interprétation ludique basée uniquement sur tes réponses." else "A playful interpretation based only on your answers.",
            color = InsightMuted,
            fontSize = 10.sp
        )
    }
}

private fun localizedCopy(key: ProfileInsightKey, french: Boolean): InsightCopy = when (key) {
    ProfileInsightKey.STRONG_SIGNATURE -> if (french) {
        InsightCopy(
            "SIGNATURE MARQUÉE",
            "Plusieurs de tes dimensions sont nettement éloignées du centre. Ton profil fait ressortir des préférences assez affirmées plutôt qu’un ensemble neutre."
        )
    } else {
        InsightCopy(
            "STRONG SIGNATURE",
            "Several of your dimensions sit far from the middle. Your profile shows a set of fairly pronounced preferences rather than a neutral pattern."
        )
    }

    ProfileInsightKey.BALANCED_CORE -> if (french) {
        InsightCopy(
            "NOYAU ÉQUILIBRÉ",
            "Plusieurs dimensions restent proches du centre. Selon le contexte, tu sembles pouvoir naviguer entre les deux côtés plutôt que suivre une préférence unique."
        )
    } else {
        InsightCopy(
            "BALANCED CORE",
            "Several dimensions stay close to the middle. Depending on context, you may move between both sides instead of following one fixed preference."
        )
    }

    ProfileInsightKey.BOLD_CONTRAST -> if (french) {
        InsightCopy(
            "CONTRASTES FORTS",
            "Ton profil contient à la fois des scores très hauts et très bas. Certaines facettes de ta façon d’agir peuvent donc sembler très différentes selon la situation."
        )
    } else {
        InsightCopy(
            "BOLD CONTRAST",
            "Your profile contains both very high and very low scores. Different parts of how you act may therefore look quite different depending on the situation."
        )
    }
}
