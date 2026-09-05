package com.whoareyou.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object GlobalProfileShare {
    private val shareScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private data class CardCopy(
        val profileTitle: String,
        val progress: (Int, Int, Int) -> String,
        val cta: String,
        val disclaimer: String,
        val shareText: (String) -> String,
        val chooser: String
    )

    fun share(context: Context, summary: GlobalProfileSummary) {
        shareScope.launch {
            val chooser = withContext(Dispatchers.IO) {
                val copy = cardCopy(context)
                val bitmap = render(summary, copy)
                try {
                    val dir = File(context.cacheDir, "shared_results").apply { mkdirs() }
                    val file = File(dir, "who_are_you_profile.png")
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, copy.shareText(summary.dominantArchetype))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    Intent.createChooser(intent, copy.chooser)
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
                profileTitle = "TON PROFIL",
                progress = { completed, total, percent -> "$completed/$total dimensions découvertes • $percent% complété" },
                cta = "Découvre le tien. Compare-toi avec tes amis.",
                disclaimer = "Pour le divertissement et la réflexion personnelle uniquement.",
                shareText = { archetype -> "Mon profil Who Are You? : $archetype. À quoi ressemble le tien ?" },
                chooser = "Partager ton profil"
            )
        } else {
            CardCopy(
                profileTitle = "YOUR PROFILE",
                progress = { completed, total, percent -> "$completed/$total dimensions discovered • $percent% complete" },
                cta = "Discover yours. Compare with friends.",
                disclaimer = "For entertainment and self-reflection only.",
                shareText = { archetype -> "My Who Are You? profile: $archetype. What does yours look like?" },
                chooser = "Share your profile"
            )
        }
    }

    private fun render(summary: GlobalProfileSummary, copy: CardCopy): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.rgb(9, 10, 15))
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        paint.color = Color.rgb(156, 123, 255)
        paint.textSize = 44f
        canvas.drawText("WHO ARE YOU?", 84f, 150f, paint)

        paint.color = Color.WHITE
        paint.textSize = 86f
        canvas.drawText(copy.profileTitle, 84f, 290f, paint)

        paint.color = Color.rgb(110, 231, 249)
        paint.textSize = 62f
        drawMultiline(canvas, summary.dominantArchetype.uppercase(), 84f, 405f, width - 168f, paint, 74f)

        paint.color = Color.rgb(164, 167, 181)
        paint.textSize = 32f
        drawFittedText(canvas, copy.progress(summary.completedCount, summary.totalCount, summary.completionPercent), 84f, 600f, width - 168f, paint, 32f, 24f)

        var y = 720f
        val dimensions = summary.dimensions.sortedByDescending { kotlin.math.abs(it.score - 50) }.take(6)
        dimensions.forEach { dimension ->
            paint.color = Color.WHITE
            paint.textSize = 38f
            canvas.drawText(dimension.title, 84f, y, paint)

            paint.color = Color.rgb(164, 167, 181)
            paint.textSize = 25f
            canvas.drawText(dimension.metricLabel, 84f, y + 42f, paint)

            paint.color = Color.rgb(156, 123, 255)
            paint.textSize = 40f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${dimension.score}%", width - 84f, y + 10f, paint)
            paint.textAlign = Paint.Align.LEFT

            val left = 84f
            val right = width - 84f
            val top = y + 72f
            paint.color = Color.rgb(27, 29, 39)
            canvas.drawRoundRect(left, top, right, top + 18f, 9f, 9f, paint)
            paint.color = Color.rgb(156, 123, 255)
            canvas.drawRoundRect(left, top, left + ((right - left) * dimension.score / 100f), top + 18f, 9f, 9f, paint)
            y += 166f
        }

        paint.color = Color.WHITE
        paint.textSize = 34f
        drawFittedText(canvas, copy.cta, 84f, 1740f, width - 168f, paint, 34f, 26f)
        paint.color = Color.rgb(164, 167, 181)
        paint.textSize = 25f
        drawFittedText(canvas, copy.disclaimer, 84f, 1800f, width - 168f, paint, 25f, 19f)
        return bitmap
    }

    private fun drawFittedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint, preferredSize: Float, minSize: Float) {
        paint.textSize = preferredSize
        while (paint.measureText(text) > maxWidth && paint.textSize > minSize) paint.textSize -= 1f
        canvas.drawText(text, x, y, paint)
    }

    private fun drawMultiline(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineHeight: Float) {
        val words = text.split(" ")
        var line = ""
        var y = startY
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, y, paint)
                y += lineHeight
                line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }
}
