package com.whoareyou.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

fun premiumScreenTransition(
    initial: AppScreen,
    target: AppScreen,
    reduceMotion: Boolean = false
): ContentTransform {
    if (reduceMotion) {
        return fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
    }

    val forward = navigationDepth(target) >= navigationDepth(initial)
    val enterDirection = if (forward) 1 else -1
    val exitDirection = -enterDirection
    val isTopLevelSwitch =
        (initial == AppScreen.DISCOVER && target == AppScreen.PROFILE) ||
            (initial == AppScreen.PROFILE && target == AppScreen.DISCOVER)

    val enterScale = when {
        isTopLevelSwitch -> 0.996f
        forward -> 0.985f
        else -> 1.008f
    }
    val exitScale = when {
        isTopLevelSwitch -> 0.998f
        forward -> 0.992f
        else -> 1.012f
    }
    val enterDivisor = if (isTopLevelSwitch) 24 else 14
    val exitDivisor = if (isTopLevelSwitch) 28 else 18
    val enterMillis = if (isTopLevelSwitch) V2Motion.StandardMillis else V2Motion.EmphasizedMillis

    return (
        fadeIn(tween(V2Motion.StandardMillis)) +
            slideInHorizontally(
                animationSpec = tween(enterMillis),
                initialOffsetX = { width -> enterDirection * (width / enterDivisor) }
            ) +
            scaleIn(
                initialScale = enterScale,
                animationSpec = tween(enterMillis)
            )
        ).togetherWith(
            fadeOut(tween(V2Motion.FastMillis)) +
                slideOutHorizontally(
                    animationSpec = tween(V2Motion.StandardMillis),
                    targetOffsetX = { width -> exitDirection * (width / exitDivisor) }
                ) +
                scaleOut(
                    targetScale = exitScale,
                    animationSpec = tween(V2Motion.StandardMillis)
                )
        )
}

private fun navigationDepth(screen: AppScreen): Int = when (screen) {
    AppScreen.DISCOVER -> 0
    AppScreen.PROFILE -> 1
    AppScreen.HABITS -> 2
    AppScreen.QUIZ -> 2
    AppScreen.RESULT -> 3
}
