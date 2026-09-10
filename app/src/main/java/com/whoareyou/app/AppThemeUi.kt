package com.whoareyou.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WhoAreYouTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            background = V2Colors.Ink,
            surface = V2Colors.Surface,
            primary = V2Colors.AccentViolet,
            secondary = V2Colors.AccentCyan
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = V2Colors.Ink
        ) {
            content()
        }
    }
}
