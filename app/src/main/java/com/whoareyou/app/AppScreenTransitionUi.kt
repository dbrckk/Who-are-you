package com.whoareyou.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

    return (
        fadeIn(tween(V2Motion.StandardMillis)) +
            slideInHorizontally(
                animationSpec = tween(V2Motion.EmphasizedMillis),
                initialOffsetX = { width -> enterDirection * (width / 12) }
            )
        ).togetherWith(
            fadeOut(tween(V2Motion.FastMillis)) +
                slideOutHorizontally(
                    animationSpec = tween(V2Motion.StandardMillis),
                    targetOffsetX = { width -> exitDirection * (width / 16) }
                )
        )
}

private fun navigationDepth(screen: AppScreen): Int = when (screen) {
    AppScreen.DISCOVER -> 0
    AppScreen.PROFILE -> 1
    AppScreen.QUIZ -> 2
    AppScreen.RESULT -> 3
}
