package com.whoareyou.app

import android.content.Context
import android.content.Intent

object CompatibilityShare {
    fun share(context: Context, quizId: String, quizTitle: String, compatibility: Int) {
        val safeCompatibility = compatibility.coerceIn(0, 100)
        val text = context.getString(
            R.string.compatibility_share_text,
            safeCompatibility,
            quizTitle
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.compatibility_share_subject))
            putExtra(Intent.EXTRA_TEXT, text)
        }
        AppEvents.compatibilityShare(quizId, safeCompatibility)
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compatibility_share_chooser)))
    }
}
