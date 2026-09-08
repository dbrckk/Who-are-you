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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileMomentumCard(momentum: ProfileMomentum) {
    if (momentum.changingCount == 0 && momentum.stableCount == 0) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(V2Radius.Card))
            .background(V2Colors.Surface.copy(alpha = 0.94f))
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.personalized_momentum_label),
            color = V2Colors.AccentCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(
                    R.string.personalized_momentum_summary,
                    momentum.changingCount,
                    momentum.stableCount
                ),
                color = V2Colors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        momentum.strongestChange?.let { dimension ->
            val delta = dimension.change?.delta ?: 0
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.personalized_momentum_change,
                    dimension.metricLabel,
                    delta
                ),
                color = if (delta >= 0) V2Colors.AccentCyan else V2Colors.AccentViolet,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
