package com.whoareyou.app

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun reducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return animationsDisabledBySystem(context) || powerSaveModeEnabled(context)
}

private fun animationsDisabledBySystem(context: Context): Boolean = runCatching {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
}.getOrDefault(false)

private fun powerSaveModeEnabled(context: Context): Boolean = runCatching {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    powerManager?.isPowerSaveMode == true
}.getOrDefault(false)
