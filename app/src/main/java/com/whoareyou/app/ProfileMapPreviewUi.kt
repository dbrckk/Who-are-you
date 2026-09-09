package com.whoareyou.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProfileMapPreview(
    dimensions: List<ProfileDimension>,
    modifier: Modifier = Modifier
) {
    val points = remember(dimensions) {
        dimensions
            .sortedByDescending { kotlin.math.abs(it.score - 50) }
            .take(6)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(V2Radius.Card))
            .background(
                Brush.linearGradient(
                    listOf(
                        V2Colors.Ink.copy(alpha = 0.72f),
                        V2Colors.Surface.copy(alpha = 0.92f)
                    )
                )
            )
            .padding(14.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.34f
            val axes = 6

            repeat(3) { ringIndex ->
                val ringRadius = radius * ((ringIndex + 1) / 3f)
                val ring = Path()
                repeat(axes) { index ->
                    val angle = -PI / 2 + (2 * PI * index / axes)
                    val point = Offset(
                        center.x + cos(angle).toFloat() * ringRadius,
                        center.y + sin(angle).toFloat() * ringRadius
                    )
                    if (index == 0) ring.moveTo(point.x, point.y) else ring.lineTo(point.x, point.y)
                }
                ring.close()
                drawPath(
                    path = ring,
                    color = V2Colors.Hairline.copy(alpha = 0.9f),
                    style = Stroke(width = 1.1f)
                )
            }

            repeat(axes) { index ->
                val angle = -PI / 2 + (2 * PI * index / axes)
                val end = Offset(
                    center.x + cos(angle).toFloat() * radius,
                    center.y + sin(angle).toFloat() * radius
                )
                drawLine(
                    color = V2Colors.Hairline.copy(alpha = 0.75f),
                    start = center,
                    end = end,
                    strokeWidth = 1f
                )
            }

            if (points.isNotEmpty()) {
                val polygon = Path()
                repeat(axes) { index ->
                    val dimension = points.getOrNull(index)
                    val normalized = dimension?.let { (kotlin.math.abs(it.score - 50) / 50f).coerceIn(0.16f, 1f) } ?: 0.12f
                    val angle = -PI / 2 + (2 * PI * index / axes)
                    val point = Offset(
                        center.x + cos(angle).toFloat() * radius * normalized,
                        center.y + sin(angle).toFloat() * radius * normalized
                    )
                    if (index == 0) polygon.moveTo(point.x, point.y) else polygon.lineTo(point.x, point.y)
                }
                polygon.close()

                drawPath(
                    path = polygon,
                    brush = Brush.linearGradient(
                        listOf(
                            V2Colors.AccentViolet.copy(alpha = 0.42f),
                            V2Colors.AccentCyan.copy(alpha = 0.20f)
                        )
                    )
                )
                drawPath(
                    path = polygon,
                    color = V2Colors.AccentViolet.copy(alpha = 0.86f),
                    style = Stroke(width = 2.2f)
                )

                repeat(axes) { index ->
                    val dimension = points.getOrNull(index) ?: return@repeat
                    val normalized = (kotlin.math.abs(dimension.score - 50) / 50f).coerceIn(0.16f, 1f)
                    val angle = -PI / 2 + (2 * PI * index / axes)
                    val point = Offset(
                        center.x + cos(angle).toFloat() * radius * normalized,
                        center.y + sin(angle).toFloat() * radius * normalized
                    )
                    drawCircle(
                        color = if (dimension.score >= 50) V2Colors.AccentCyan else V2Colors.AccentViolet,
                        radius = 4.2f,
                        center = point
                    )
                }
            } else {
                drawCircle(
                    color = V2Colors.AccentViolet.copy(alpha = 0.35f),
                    radius = radius * 0.18f,
                    center = center
                )
            }
        }
    }
}
