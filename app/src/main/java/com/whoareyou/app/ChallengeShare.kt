package com.whoareyou.app

import android.content.Context
import android.content.Intent
import android.net.Uri

object ChallengeShare {
    fun buildUri(quizId: String, inviterScore: Int): Uri = Uri.Builder()
        .scheme("whoareyou")
        .authority("challenge")
        .appendQueryParameter("quiz", quizId)
        .appendQueryParameter("score", inviterScore.coerceIn(0, 100).toString())
        .build()

    fun share(context: Context, quizId: String, quizTitle: String, inviterScore: Int) {
        val uri = buildUri(quizId, inviterScore)
        val text = "I challenge you on $quizTitle. Take the same test and see how compatible we are: $uri"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Who Are You? challenge")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Challenge a friend"))
    }

    fun parse(uri: Uri?): IncomingChallenge? {
        if (uri == null || uri.scheme != "whoareyou" || uri.host != "challenge") return null
        val quizId = uri.getQueryParameter("quiz")?.takeIf { it.isNotBlank() } ?: return null
        val score = uri.getQueryParameter("score")?.toIntOrNull()?.coerceIn(0, 100) ?: return null
        return IncomingChallenge(quizId, score)
    }
}

data class IncomingChallenge(
    val quizId: String,
    val inviterScore: Int
)
