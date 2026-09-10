package com.whoareyou.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

fun premiumScreenTransition(): ContentTransform =
    (fadeIn(tween(V2Motion.StandardMillis)) +
        slideInHorizontally(
            animationSpec = tween(V2Motion.EmphasizedMillis),
            initialOffsetX = { width -> width / 10 }
        )).togetherWith(
        fadeOut(tween(V2Motion.FastMillis)) +
            slideOutHorizontally(
                animationSpec = tween(V2Motion.StandardMillis),
                targetOffsetX = { width -> -width / 14 }
            )
    )
