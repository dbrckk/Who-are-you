package com.whoareyou.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** Lightweight vector-like identity artwork drawn entirely with Compose. */
@Composable
fun IdentityAura(
    score: Int,
    modifier: Modifier = Modifier
) {
    val normalized = score.coerceIn(0, 100) / 100f
    val warm = lerp(V2Colors.Orchid, V2Colors.Peach, normalized)
    val cool = lerp(V2Colors.VioletBright, V2Colors.Cyan, normalized)

    Box(
        modifier = modifier
            .size(156.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        warm.copy(alpha = 0.22f),
                        V2Colors.Plum.copy(alpha = 0.14f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(136.dp)) {
            val center = this.center
            val baseRadius = size.minDimension * 0.26f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cool.copy(alpha = 0.88f),
                        warm.copy(alpha = 0.48f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.65f
                ),
                radius = baseRadius * 1.65f,
                center = center
            )

            drawCircle(
                color = V2Colors.TextPrimary.copy(alpha = 0.82f),
                radius = baseRadius * 0.62f,
                center = center
            )
            drawCircle(
                color = V2Colors.Ink.copy(alpha = 0.78f),
                radius = baseRadius * 0.45f,
                center = center
            )

            repeat(10) { index ->
                val angle = index * (Math.PI * 2.0 / 10.0) + normalized * 0.7
                val orbit = baseRadius * (1.55f + (index % 3) * 0.18f)
                val point = Offset(
                    x = center.x + cos(angle).toFloat() * orbit,
                    y = center.y + sin(angle).toFloat() * orbit
                )
                drawCircle(
                    color = if (index % 2 == 0) {
                        warm.copy(alpha = 0.88f)
                    } else {
                        cool.copy(alpha = 0.82f)
                    },
                    radius = if (index % 3 == 0) 5.5f else 3.4f,
                    center = point
                )
            }

            drawCircle(
                color = V2Colors.TextPrimary.copy(alpha = 0.20f),
                radius = baseRadius * 1.28f,
                center = center,
                style = Stroke(width = 2.5f)
            )
        }
    }
}
