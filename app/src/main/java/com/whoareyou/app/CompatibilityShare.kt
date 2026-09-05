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

object CompatibilityShare {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private val shareScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun share(
        context: Context,
        quizId: String,
        quizTitle: String,
        inviterScore: Int,
        myScore: Int,
        compatibility: Int
    ) {
        val safeCompatibility = compatibility.coerceIn(0, 100)
        val safeInviterScore = inviterScore.coerceIn(0, 100)
        val safeMyScore = myScore.coerceIn(0, 100)
        AppEvents.compatibilityShare(quizId, safeCompatibility)

        shareScope.launch {
            val chooser = withContext(Dispatchers.IO) {
                val matchLabel = matchLabel(context, safeCompatibility)
                val bitmap = render(
                    context = context,
                    quizTitle = quizTitle,
                    inviterScore = safeInviterScore,
                    myScore = safeMyScore,
                    compatibility = safeCompatibility,
                    matchLabel = matchLabel
                )

                try {
                    val directory = File(context.cacheDir, "shared_results").apply { mkdirs() }
                    val file = File(directory, "who_are_you_match_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { output -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, output) }

                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val challengeUri = ChallengeShare.buildUri(quizId, safeMyScore).toString()
                    val text = buildString {
                        append(context.getString(R.string.compatibility_share_text, safeCompatibility, quizTitle))
                        append("\n\n")
                        append(context.getString(R.string.compatibility_share_challenge, challengeUri))
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.compatibility_share_subject))
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, text)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    Intent.createChooser(intent, context.getString(R.string.compatibility_share_chooser))
                } finally {
                    bitmap.recycle()
                }
            }
            context.startActivity(chooser)
        }
    }

    private fun matchLabel(context: Context, compatibility: Int): String = when {
        compatibility >= 90 -> context.getString(R.string.challenge_match_identical)
        compatibility >= 75 -> context.getString(R.string.challenge_match_strong)
        compatibility >= 55 -> context.getString(R.string.challenge_match_mixed)
        else -> context.getString(R.string.challenge_match_opposite)
    }

    private fun render(
        context: Context,
        quizTitle: String,
        inviterScore: Int,
        myScore: Int,
        compatibility: Int,
        matchLabel: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = Color.rgb(9, 10, 15)
        val panel = Color.rgb(20, 21, 29)
        val panelSoft = Color.rgb(27, 29, 39)
        val violet = Color.rgb(156, 123, 255)
        val cyan = Color.rgb(110, 231, 249)
        val muted = Color.rgb(164, 167, 181)
        val white = Color.WHITE
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(background)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = violet
        paint.textSize = 38f
        canvas.drawText("WHO ARE YOU?", 72f, 112f, paint)

        paint.color = cyan
        paint.textSize = 30f
        canvas.drawText(context.getString(R.string.challenge_match_header), 72f, 192f, paint)

        paint.color = white
        paint.textSize = 58f
        drawWrappedText(canvas, quizTitle.uppercase(), paint, 72f, 286f, 936f, 68f)

        paint.color = panel
        canvas.drawRoundRect(72f, 470f, 1008f, 1080f, 52f, 52f, paint)

        paint.color = violet
        paint.textSize = 196f
        val compatibilityText = "$compatibility%"
        canvas.drawText(compatibilityText, (WIDTH - paint.measureText(compatibilityText)) / 2f, 720f, paint)

        paint.color = white
        paint.textSize = 48f
        val labelWidth = paint.measureText(matchLabel)
        canvas.drawText(matchLabel, (WIDTH - labelWidth) / 2f, 810f, paint)

        drawScoreBar(
            canvas = canvas,
            paint = paint,
            label = context.getString(R.string.challenge_friend),
            score = inviterScore,
            y = 910f,
            cyan = cyan,
            white = white,
            muted = muted,
            panelSoft = panelSoft,
            violet = violet
        )
        drawScoreBar(
            canvas = canvas,
            paint = paint,
            label = context.getString(R.string.challenge_you),
            score = myScore,
            y = 1010f,
            cyan = cyan,
            white = white,
            muted = muted,
            panelSoft = panelSoft,
            violet = violet
        )

        paint.color = cyan
        paint.textSize = 31f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.compatibility_card_hook), 72f, 1260f, paint)

        paint.color = white
        paint.textSize = 54f
        drawWrappedText(canvas, context.getString(R.string.compatibility_card_cta), paint, 72f, 1340f, 936f, 65f)

        paint.color = muted
        paint.textSize = 31f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, context.getString(R.string.compatibility_card_subtitle), paint, 72f, 1505f, 936f, 45f)

        paint.color = panel
        canvas.drawRoundRect(72f, 1650f, 1008f, 1780f, 38f, 38f, paint)
        paint.color = violet
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brand = "WHO ARE YOU?  •  ANDROID"
        canvas.drawText(brand, (WIDTH - paint.measureText(brand)) / 2f, 1730f, paint)

        paint.color = muted
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val disclaimer = context.getString(R.string.challenge_disclaimer)
        canvas.drawText(disclaimer, (WIDTH - paint.measureText(disclaimer)) / 2f, 1860f, paint)

        return bitmap
    }

    private fun drawScoreBar(
        canvas: Canvas,
        paint: Paint,
        label: String,
        score: Int,
        y: Float,
        cyan: Int,
        white: Int,
        muted: Int,
        panelSoft: Int,
        violet: Int
    ) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = cyan
        paint.textSize = 24f
        canvas.drawText(label, 126f, y, paint)

        paint.color = white
        paint.textSize = 34f
        val scoreText = "$score%"
        canvas.drawText(scoreText, 954f - paint.measureText(scoreText), y, paint)

        paint.color = panelSoft
        canvas.drawRoundRect(126f, y + 28f, 954f, y + 56f, 14f, 14f, paint)
        paint.color = violet
        val progressRight = 126f + 828f * score / 100f
        canvas.drawRoundRect(126f, y + 28f, progressRight, y + 56f, 14f, 14f, paint)

        paint.color = muted
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
