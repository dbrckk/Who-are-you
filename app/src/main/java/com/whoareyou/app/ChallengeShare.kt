package com.whoareyou.app

import android.content.Context
import android.content.Intent
import android.net.Uri

object ChallengeShare {
    private const val WEB_SCHEME = "https"
    private const val WEB_HOST = "dbrckk.github.io"
    private const val WEB_PATH = "/Who-are-you/challenge/"

    fun buildUri(quizId: String, inviterScore: Int): Uri = Uri.Builder()
        .scheme(WEB_SCHEME)
        .authority(WEB_HOST)
        .path(WEB_PATH)
        .appendQueryParameter("quiz", quizId)
        .appendQueryParameter("score", inviterScore.coerceIn(0, 100).toString())
        .build()

    fun buildAppUri(quizId: String, inviterScore: Int): Uri = Uri.Builder()
        .scheme("whoareyou")
        .authority("challenge")
        .appendQueryParameter("quiz", quizId)
        .appendQueryParameter("score", inviterScore.coerceIn(0, 100).toString())
        .build()

    fun share(context: Context, quizId: String, quizTitle: String, inviterScore: Int) {
        val uri = buildUri(quizId, inviterScore)
        val text = context.getString(R.string.challenge_share_text, quizTitle, uri.toString())
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.challenge_share_subject))
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.challenge_share_chooser)))
    }

    fun parse(uri: Uri?): IncomingChallenge? {
        if (uri == null || !isSupportedChallengeUri(uri)) return null
        val quizId = uri.getQueryParameter("quiz")?.takeIf { it.isNotBlank() } ?: return null
        val score = uri.getQueryParameter("score")?.toIntOrNull()?.coerceIn(0, 100) ?: return null
        return IncomingChallenge(quizId, score)
    }

    private fun isSupportedChallengeUri(uri: Uri): Boolean {
        val legacy = uri.scheme == "whoareyou" && uri.host == "challenge"
        val web = uri.scheme == WEB_SCHEME &&
            uri.host == WEB_HOST &&
            uri.path?.startsWith(WEB_PATH) == true
        return legacy || web
    }
}

data class IncomingChallenge(
    val quizId: String,
    val inviterScore: Int
)
