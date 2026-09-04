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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GlobalProfileShare {
    suspend fun share(context: Context, summary: GlobalProfileSummary) {
        val chooser = withContext(Dispatchers.IO) {
            val bitmap = render(summary)
            try {
                val dir = File(context.cacheDir, "shared_results").apply { mkdirs() }
                val file = File(dir, "who_are_you_profile.png")
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "My Who Are You? profile: ${summary.dominantArchetype}. What does yours look like?")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                Intent.createChooser(intent, "Share your profile")
            } finally {
                bitmap.recycle()
            }
        }
        context.startActivity(chooser)
    }

    private fun render(summary: GlobalProfileSummary): Bitmap {
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
        canvas.drawText("YOUR PROFILE", 84f, 290f, paint)

        paint.color = Color.rgb(110, 231, 249)
        paint.textSize = 62f
        drawMultiline(canvas, summary.dominantArchetype.uppercase(), 84f, 405f, width - 168f, paint, 74f)

        paint.color = Color.rgb(164, 167, 181)
        paint.textSize = 32f
        canvas.drawText("${summary.completedCount}/${summary.totalCount} dimensions discovered • ${summary.completionPercent}% complete", 84f, 600f, paint)

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
        canvas.drawText("Discover yours. Compare with friends.", 84f, 1740f, paint)
        paint.color = Color.rgb(164, 167, 181)
        paint.textSize = 25f
        canvas.drawText("For entertainment and self-reflection only.", 84f, 1800f, paint)
        return bitmap
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
