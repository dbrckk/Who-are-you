package com.whoareyou.app

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

/** Android special-access boundary for local app-usage observations. */
object UsageAccess {
    fun state(context: Context): BehaviorSourceState {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return BehaviorSourceState.UNSUPPORTED
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return if (mode == AppOpsManager.MODE_ALLOWED) {
            BehaviorSourceState.AVAILABLE
        } else {
            BehaviorSourceState.PERMISSION_REQUIRED
        }
    }

    fun settingsIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
}
