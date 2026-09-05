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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

object ResultShare {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private val shareScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private data class CardCopy(
        val compare: String,
        val question: String,
        val subtitle: String,
        val brand: String,
        val disclaimer: String
    )

    fun share(
        context: Context,
        quizTitle: String,
        resultTitle: String,
        score: Int,
        metricLow: String,
        metricHigh: String,
        description: String
    ) {
        shareScope.launch {
            val chooser = withContext(Dispatchers.IO) {
                val copy = cardCopy(context)
                val bitmap = render(
                    quizTitle = quizTitle,
                    resultTitle = resultTitle,
                    score = score,
                    metricLow = metricLow,
                    metricHigh = metricHigh,
                    description = description,
                    copy = copy
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

                    val quizId = QuizRepository.load(context).firstOrNull { it.title == quizTitle }?.id
                    val challengeUri = quizId?.let { ChallengeShare.buildUri(it, score).toString() }

                    val shareText = buildString {
                        append(context.getString(R.string.result_share_text, resultTitle, score, quizTitle))
                        if (challengeUri != null) {
                            append("\n\n")
                            append(context.getString(R.string.result_share_challenge, challengeUri))
                        }
                    }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    Intent.createChooser(intent, context.getString(R.string.result_share_chooser))
                } finally {
                    bitmap.recycle()
                }
            }

            context.startActivity(chooser)
        }
    }

    private fun cardCopy(context: Context): CardCopy {
        val french = context.resources.configuration.locales[0]?.language == "fr"
        return if (french) {
            CardCopy(
                compare = "COMPARE TON RÉSULTAT",
                question = "Et toi, qui es-tu ?",
                subtitle = "Fais le test. Compare-toi avec tes amis.",
                brand = "WHO ARE YOU?  •  ANDROID",
                disclaimer = "Pour le divertissement et la réflexion personnelle uniquement."
            )
        } else {
            CardCopy(
                compare = "COMPARE YOUR RESULT",
                question = "What are you?",
                subtitle = "Take the test. Compare with friends.",
                brand = "WHO ARE YOU?  •  ANDROID",
                disclaimer = "For entertainment and self-reflection only."
            )
        }
    }

    private fun render(
        quizTitle: String,
        resultTitle: String,
        score: Int,
        metricLow: String,
        metricHigh: String,
        description: String,
        copy: CardCopy
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
        canvas.drawText(copy.compare, 72f, 1435f, paint)

        paint.color = white
        paint.textSize = 52f
        canvas.drawText(copy.question, 72f, 1510f, paint)

        paint.color = muted
        paint.textSize = 31f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, copy.subtitle, paint, 72f, 1570f, 936f, 42f)

        paint.color = panel
        canvas.drawRoundRect(72f, 1660f, 1008f, 1780f, 38f, 38f, paint)
        paint.color = violet
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brandWidth = paint.measureText(copy.brand)
        canvas.drawText(copy.brand, max(72f, (WIDTH - brandWidth) / 2f), 1735f, paint)

        paint.color = muted
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawCenteredFittedText(canvas, copy.disclaimer, paint, 1855f, 900f, 24f, 19f)

        return bitmap
    }

    private fun drawCenteredFittedText(canvas: Canvas, text: String, paint: Paint, y: Float, maxWidth: Float, preferredSize: Float, minSize: Float) {
        paint.textSize = preferredSize
        while (paint.measureText(text) > maxWidth && paint.textSize > minSize) paint.textSize -= 1f
        canvas.drawText(text, (WIDTH - paint.measureText(text)) / 2f, y, paint)
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
