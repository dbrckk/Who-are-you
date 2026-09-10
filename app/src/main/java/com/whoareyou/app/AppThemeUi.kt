package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun WhoAreYouTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        background = V2Colors.Ink,
        surface = V2Colors.Surface,
        surfaceVariant = V2Colors.SurfaceRaised,
        primary = V2Colors.AccentViolet,
        onPrimary = Color.White,
        secondary = V2Colors.AccentCyan,
        tertiary = V2Colors.Coral,
        onBackground = V2Colors.TextPrimary,
        onSurface = V2Colors.TextPrimary,
        onSurfaceVariant = V2Colors.TextSecondary,
        outline = V2Colors.HairlineStrong,
        error = V2Colors.Coral
    )

    MaterialTheme(colorScheme = scheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            V2Colors.InkSoft,
                            V2Colors.Ink,
                            Color(0xFF080B17)
                        )
                    )
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                content()
            }
        }
    }
}
