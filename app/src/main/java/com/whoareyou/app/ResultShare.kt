package com.whoareyou.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

object ResultShare {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920

    suspend fun share(
        context: Context,
        quizTitle: String,
        resultTitle: String,
        score: Int,
        metricLow: String,
        metricHigh: String,
        description: String
    ) {
        val chooser = withContext(Dispatchers.IO) {
            val bitmap = render(
                quizTitle = quizTitle,
                resultTitle = resultTitle,
                score = score,
                metricLow = metricLow,
                metricHigh = metricHigh,
                description = description
            )

            try {
                val directory = File(context.cacheDir, "shared_results").apply { mkdirs() }
                val file = File(directory, "who_are_you_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val challengeUri = quizIdFromTitle(quizTitle)?.let { quizId ->
                    ChallengeShare.buildUri(quizId, score).toString()
                }

                val shareText = buildString {
                    append("I got $resultTitle — $score% on $quizTitle. What are you?")
                    if (challengeUri != null) {
                        append("\n\nTake the same test and compare with me: $challengeUri")
                    }
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                Intent.createChooser(intent, "Share your result")
            } finally {
                bitmap.recycle()
            }
        }

        context.startActivity(chooser)
    }

    private fun quizIdFromTitle(title: String): String? = when (title) {
        "Social Battery" -> "social_battery"
        "Logic vs Emotion" -> "logic_emotion"
        "Overthinker" -> "overthinker"
        "Chaos vs Control" -> "chaos_control"
        "Risk Taker" -> "risk_taker"
        else -> null
    }

    private fun render(
        quizTitle: String,
        resultTitle: String,
        score: Int,
        metricLow: String,
        metricHigh: String,
        description: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = Color.rgb(9, 10, 15)
        val panel = Color.rgb(20, 21, 29)
        val panelSoft = Color.rgb(27, 29, 39)
        val violet = Color.rgb(156, 123, 255)
        val cyan = Color.rgb(110, 231, 249)
        val white = Color.WHITE
        val muted = Color.rgb(164, 167, 181)

        canvas.drawColor(background)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        paint.color = violet
        paint.textSize = 38f
        canvas.drawText("WHO ARE YOU?", 72f, 110f, paint)

        paint.color = muted
        paint.textSize = 31f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(quizTitle.uppercase(), 72f, 185f, paint)

        paint.color = white
        paint.textSize = 84f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        drawWrappedText(canvas, resultTitle.uppercase(), paint, 72f, 315f, 936f, 96f)

        val cardLeft = 72f
        val cardTop = 560f
        val cardRight = 1008f
        val cardBottom = 1260f
        paint.color = panel
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 48f, 48f, paint)

        paint.color = violet
        paint.textSize = 188f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val scoreText = "$score%"
        val scoreWidth = paint.measureText(scoreText)
        canvas.drawText(scoreText, (WIDTH - scoreWidth) / 2f, 790f, paint)

        paint.color = muted
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(metricLow.uppercase(), 126f, 910f, paint)
        val rightWidth = paint.measureText(metricHigh.uppercase())
        canvas.drawText(metricHigh.uppercase(), 954f - rightWidth, 910f, paint)

        paint.color = panelSoft
        canvas.drawRoundRect(126f, 950f, 954f, 982f, 16f, 16f, paint)
        paint.color = violet
        val progressRight = 126f + (828f * score.coerceIn(0, 100) / 100f)
        canvas.drawRoundRect(126f, 950f, progressRight, 982f, 16f, 16f, paint)

        paint.color = white
        paint.textSize = 36f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, description, paint, 126f, 1070f, 828f, 52f)

        paint.color = cyan
        paint.textSize = 29f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("COMPARE YOUR RESULT", 72f, 1435f, paint)

        paint.color = white
        paint.textSize = 52f
        canvas.drawText("What are you?", 72f, 1510f, paint)

        paint.color = muted
        paint.textSize = 31f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Take the test. Compare with friends.", 72f, 1570f, paint)

        paint.color = panel
        canvas.drawRoundRect(72f, 1660f, 1008f, 1780f, 38f, 38f, paint)
        paint.color = violet
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brand = "WHO ARE YOU?  •  ANDROID"
        val brandWidth = paint.measureText(brand)
        canvas.drawText(brand, max(72f, (WIDTH - brandWidth) / 2f), 1735f, paint)

        paint.color = muted
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val disclaimer = "For entertainment and self-reflection only."
        val disclaimerWidth = paint.measureText(disclaimer)
        canvas.drawText(disclaimer, (WIDTH - disclaimerWidth) / 2f, 1855f, paint)

        return bitmap
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float
    ) {
        val words = text.split(" ")
        var line = ""
        var y = startY

        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) <= maxWidth) {
                line = candidate
            } else {
                if (line.isNotEmpty()) {
                    canvas.drawText(line, x, y, paint)
                    y += lineHeight
                }
                line = word
            }
        }

        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }
}
