package com.whoareyou.app

import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareSafety {
    fun launch(context: Context, intent: Intent): Boolean = runCatching {
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
