package com.whoareyou.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

enum class QuizVisualTheme {
    SOCIAL,
    EMOTION,
    MIND,
    CONTROL,
    GROWTH,
    VALUES,
    IDENTITY
}

object QuizVisuals {
    fun themeFor(quiz: Quiz): QuizVisualTheme {
        val key = "${quiz.id} ${quiz.title} ${quiz.hook}".lowercase()
        return when {
            key.hasAny("love", "relationship", "attachment", "dating", "romance", "jealous", "affection", "intimacy", "trust") -> QuizVisualTheme.EMOTION
            key.hasAny("social", "friend", "people", "group", "confidence", "introvert", "extrovert", "conversation", "lonely") -> QuizVisualTheme.SOCIAL
            key.hasAny("logic", "decision", "overthink", "mind", "focus", "intuition", "analysis", "thinking", "emotion", "stress", "worry") -> QuizVisualTheme.MIND
            key.hasAny("chaos", "control", "risk", "money", "security", "perfection", "impulse", "boundary", "planning") -> QuizVisualTheme.CONTROL
            key.hasAny("productivity", "growth", "discipline", "ambition", "goal", "resilience", "procrast", "motivation", "habit", "success", "confidence") -> QuizVisualTheme.GROWTH
            key.hasAny("value", "purpose", "meaning", "moral", "principle", "loyalty", "freedom", "priority") -> QuizVisualTheme.VALUES
            else -> QuizVisualTheme.IDENTITY
        }
    }

    fun drawableFor(quiz: Quiz): Int = when (themeFor(quiz)) {
        QuizVisualTheme.SOCIAL -> R.drawable.quiz_art_social
        QuizVisualTheme.EMOTION -> R.drawable.quiz_art_emotion
        QuizVisualTheme.MIND -> R.drawable.quiz_art_mind
        QuizVisualTheme.CONTROL -> R.drawable.quiz_art_control
        QuizVisualTheme.GROWTH -> R.drawable.quiz_art_growth
        QuizVisualTheme.VALUES -> R.drawable.quiz_art_values
        QuizVisualTheme.IDENTITY -> R.drawable.quiz_art_identity
    }

    private fun String.hasAny(vararg needles: String): Boolean = needles.any(::contains)
}

@Composable
fun QuizArtwork(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 112.dp else 156.dp)
            .clip(RoundedCornerShape(if (compact) 18.dp else 22.dp))
    ) {
        Image(
            painter = painterResource(QuizVisuals.drawableFor(quiz)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
