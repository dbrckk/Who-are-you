package com.whoareyou.app

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun reducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }.getOrDefault(false)
}
