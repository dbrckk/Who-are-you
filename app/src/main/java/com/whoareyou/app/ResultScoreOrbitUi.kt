package com.whoareyou.app

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Decorative, allocation-light score orbit. Numeric score and accessible
 * explanation belong to the overlaid Text in ResultScreen, never this canvas.
 * Its animation is driven by ResultScreen's reduced-motion-aware progress.
 */
@Composable
fun ResultScoreOrbit(
    progress: Float,
    accent: Color,
    companion: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val fraction = progress.coerceIn(0f, 1f)
        val strokeWidth = 12.dp.toPx()
        val inset = strokeWidth / 2f + 12.dp.toPx()
        val diameter = (size.minDimension - inset * 2f).coerceAtLeast(0f)
        val bounds = Size(diameter, diameter)
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f
        )

        drawCircle(
            brush = Brush.radialGradient(
                listOf(accent.copy(alpha = 0.16f), companion.copy(alpha = 0.07f), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.43f
            ),
            radius = size.minDimension * 0.43f
        )
        drawCircle(
            color = companion.copy(alpha = 0.18f),
            radius = diameter / 2f + strokeWidth,
            style = Stroke(width = 1.dp.toPx())
        )
        drawArc(
            color = companion.copy(alpha = 0.20f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = bounds,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        if (fraction > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(accent, companion, accent, companion, accent),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = fraction * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = bounds,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
