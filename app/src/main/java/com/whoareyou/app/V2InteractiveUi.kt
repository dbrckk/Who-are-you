package com.whoareyou.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun V2PressableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val reduceMotion = reducedMotionEnabled()
    val view = LocalView.current
    var focused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background by animateColorAsState(
        targetValue = if (pressed && enabled) V2Colors.SurfaceElevated else V2Colors.Surface,
        animationSpec = if (reduceMotion) snap() else tween(V2Motion.FastMillis),
        label = "pressableBackground"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) V2Motion.PressedScale else 1f,
        animationSpec = if (reduceMotion) snap() else tween(V2Motion.FastMillis),
        label = "pressableScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = when {
                    !enabled -> 0.58f
                    pressed -> 0.96f
                    else -> 1f
                }
                translationY = if (pressed && enabled) 1.5f else 0f
            }
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) V2Colors.AccentCyan else Color.Transparent,
                shape = RoundedCornerShape(V2Radius.Compact)
            )
            .clip(RoundedCornerShape(V2Radius.Compact))
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = role,
                enabled = enabled,
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            )
    ) {
        content()
    }
}

@Composable
fun V2PressableCard(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(V2Radius.Card),
    containerColor: Color = V2Colors.Surface,
    content: @Composable ColumnScope.() -> Unit
) {
    val reduceMotion = reducedMotionEnabled()
    val view = LocalView.current
    var focused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) V2Motion.PressedScale else 1f,
        animationSpec = if (reduceMotion) snap() else tween(V2Motion.FastMillis),
        label = "pressableCardScale"
    )

    Card(
        modifier = modifier
            .heightIn(min = 52.dp)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) V2Colors.AccentCyan else Color.Transparent,
                shape = shape
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (pressed && onClick != null) 0.97f else 1f
                translationY = if (pressed && onClick != null) 1.5f else 0f
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            onClick()
                        }
                    )
                } else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (focused) 1.5.dp else 1.dp,
            color = if (focused) V2Colors.AccentCyan.copy(alpha = 0.90f) else V2Colors.Hairline
        ),
        content = content
    )
}


@Composable
fun V2Card(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        border = BorderStroke(1.dp, V2Colors.Hairline)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(V2Spacing.Card),
            content = content
        )
    }
}
