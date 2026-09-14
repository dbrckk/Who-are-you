package com.whoareyou.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

enum class BrandMascotMood {
    WELCOME,
    CURIOUS,
    CELEBRATE
}

/**
 * Living brand mark for Who Are You.
 *
 * It deliberately stays abstract: an identity "wisp" rather than a person,
 * keeping the product inclusive while giving the interface a recognizable face.
 */
@Composable
fun BrandMascot(
    mood: BrandMascotMood = BrandMascotMood.WELCOME,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    primary: Color = V2Colors.Orchid,
    secondary: Color = V2Colors.Cyan
) {
    val reduceMotion = reducedMotionEnabled() || !animated
    val transition = if (reduceMotion) null else rememberInfiniteTransition(label = "brandMascotAmbient")
    val breathe = transition?.animateFloat(
        initialValue = 0.975f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(V2Motion.AmbientMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brandMascotBreath"
    )
    val floatOffset = transition?.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(V2Motion.AmbientMillis + 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brandMascotFloat"
    )

    Box(
        modifier = modifier
            .size(164.dp)
            .graphicsLayer {
                val scale = breathe?.value ?: 1f
                scaleX = scale
                scaleY = scale
                translationY = floatOffset?.value ?: 0f
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(164.dp)) {
            val center = this.center
            val r = size.minDimension * 0.31f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.28f),
                        secondary.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.49f
                ),
                radius = size.minDimension * 0.49f,
                center = center
            )

            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.96f),
                        V2Colors.Violet.copy(alpha = 0.94f),
                        secondary.copy(alpha = 0.90f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(center.x - r, center.y - r),
                    end = androidx.compose.ui.geometry.Offset(center.x + r, center.y + r)
                ),
                radius = r,
                center = center
            )

            drawCircle(
                color = V2Colors.TextPrimary.copy(alpha = 0.16f),
                radius = r * 0.83f,
                center = androidx.compose.ui.geometry.Offset(center.x - r * 0.13f, center.y - r * 0.18f),
                style = Stroke(width = 2.2f)
            )

            val eyeY = center.y - r * 0.13f
            val eyeSpacing = r * 0.34f
            val eyeRadius = when (mood) {
                BrandMascotMood.CURIOUS -> r * 0.075f
                else -> r * 0.068f
            }
            val eyeColor = V2Colors.Ink.copy(alpha = 0.88f)
            drawCircle(eyeColor, eyeRadius, androidx.compose.ui.geometry.Offset(center.x - eyeSpacing, eyeY))
            drawCircle(eyeColor, eyeRadius, androidx.compose.ui.geometry.Offset(center.x + eyeSpacing, eyeY))

            when (mood) {
                BrandMascotMood.WELCOME -> {
                    drawArc(
                        color = V2Colors.Ink.copy(alpha = 0.78f),
                        startAngle = 18f,
                        sweepAngle = 144f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(center.x - r * 0.25f, center.y + r * 0.04f),
                        size = androidx.compose.ui.geometry.Size(r * 0.50f, r * 0.30f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                }
                BrandMascotMood.CURIOUS -> {
                    drawCircle(
                        color = V2Colors.Ink.copy(alpha = 0.78f),
                        radius = r * 0.055f,
                        center = androidx.compose.ui.geometry.Offset(center.x, center.y + r * 0.30f)
                    )
                }
                BrandMascotMood.CELEBRATE -> {
                    drawArc(
                        color = V2Colors.Ink.copy(alpha = 0.82f),
                        startAngle = 5f,
                        sweepAngle = 170f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(center.x - r * 0.30f, center.y),
                        size = androidx.compose.ui.geometry.Size(r * 0.60f, r * 0.42f),
                        style = Stroke(width = 3.4f, cap = StrokeCap.Round)
                    )
                }
            }

            // Signature question-mark tail: the visual bridge to the launcher icon.
            val tail = Path().apply {
                moveTo(center.x + r * 0.55f, center.y + r * 0.62f)
                cubicTo(
                    center.x + r * 0.92f, center.y + r * 0.82f,
                    center.x + r * 0.80f, center.y + r * 1.18f,
                    center.x + r * 0.56f, center.y + r * 1.18f
                )
            }
            drawPath(
                path = tail,
                color = secondary.copy(alpha = 0.76f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
            drawCircle(
                color = secondary.copy(alpha = 0.88f),
                radius = 4.5f,
                center = androidx.compose.ui.geometry.Offset(center.x + r * 0.49f, center.y + r * 1.34f)
            )

            if (mood == BrandMascotMood.CELEBRATE) {
                listOf(
                    Triple(-0.82f, -0.78f, primary),
                    Triple(0.86f, -0.66f, secondary),
                    Triple(-0.96f, 0.16f, V2Colors.Peach),
                    Triple(0.94f, 0.22f, V2Colors.Rose)
                ).forEach { (dx, dy, color) ->
                    drawCircle(
                        color = color.copy(alpha = 0.82f),
                        radius = 4.2f,
                        center = androidx.compose.ui.geometry.Offset(
                            center.x + r * dx,
                            center.y + r * dy
                        )
                    )
                }
            }
        }
    }
}
