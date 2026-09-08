package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun ProfileStrengthNuanceCard(summary: GlobalProfileSummary) {
    if (summary.dimensions.size < 3) return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val snapshot = remember(summary.dimensions) { ProfileStrengthNuanceEngine.build(summary.dimensions) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            if (french) "FORCES & NUANCES" else "STRENGTHS & NUANCES",
            color = V2Colors.AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceRaised)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                snapshot.strongest.forEachIndexed { index, dimension ->
                    SignalRow(
                        eyebrow = if (index == 0) {
                            if (french) "SIGNAL LE PLUS MARQUÉ" else "STRONGEST SIGNAL"
                        } else {
                            if (french) "SECOND SIGNAL" else "SECOND SIGNAL"
                        },
                        dimension = dimension,
                        accent = if (index == 0) V2Colors.AccentViolet else V2Colors.AccentCyan
                    )
                }

                snapshot.mostNuanced?.let { nuanced ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.045f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            if (french) "ZONE LA PLUS NUANCÉE" else "MOST NUANCED AREA",
                            color = V2Colors.TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(nuanced.title, color = V2Colors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (french) {
                                "${nuanced.metricLabel} · ${nuanced.score}% · proche du centre, donc plus dépendant du contexte."
                            } else {
                                "${nuanced.metricLabel} · ${nuanced.score}% · closer to the center, so this area may depend more on context."
                            },
                            color = V2Colors.TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                snapshot.contrastPair?.let { (low, high) ->
                    val spread = high.score - low.score
                    if (spread >= 35) {
                        Column {
                            Text(
                                if (french) "CONTRASTE DU PROFIL" else "PROFILE CONTRAST",
                                color = V2Colors.TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                if (french) "${low.title} ↔ ${high.title}" else "${low.title} ↔ ${high.title}",
                                color = V2Colors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                if (french) "$spread points séparent ces deux dimensions : ton profil n'est donc pas uniforme." else "$spread points separate these dimensions, so your profile is not one-dimensional.",
                                color = V2Colors.TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalRow(eyebrow: String, dimension: ProfileDimension, accent: Color) {
    val intensity = (abs(dimension.score - 50) * 2).coerceIn(0, 100)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text(dimension.title, color = V2Colors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(dimension.metricLabel, color = V2Colors.TextSecondary, fontSize = 11.sp)
            }
            Text("$intensity%", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { intensity / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = accent,
            trackColor = Color.White.copy(alpha = 0.07f)
        )
    }
}
