package com.whoareyou.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareSafety {
    fun launch(context: Context, intent: Intent): Boolean = runCatching {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrElse { error ->
        notifyFailure(context, error)
        false
    }

    fun notifyFailure(context: Context, error: Throwable) {
        runCatching {
            AppEvents.recordError(
                error,
                mapOf("feature" to "share", "stage" to "launch_or_render")
            )
        }
        Toast.makeText(
            context.applicationContext,
            context.getString(R.string.share_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}
