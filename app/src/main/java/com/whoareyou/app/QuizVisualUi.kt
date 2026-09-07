package com.whoareyou.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class QuizVisualTheme {
    SOCIAL,
    EMOTION,
    MIND,
    CONTROL,
    GROWTH,
    VALUES,
    ENERGY,
    LIFESTYLE,
    IDENTITY
}

object QuizVisuals {
    fun themeFor(quiz: Quiz): QuizVisualTheme {
        val key = "${quiz.id} ${quiz.title} ${quiz.hook}".lowercase()
        return when {
            key.hasAny("love", "relationship", "attachment", "dating", "romance", "jealous", "affection", "intimacy", "trust") -> QuizVisualTheme.EMOTION
            key.hasAny("social", "friend", "people", "group", "confidence", "introvert", "extrovert", "conversation", "lonely") -> QuizVisualTheme.SOCIAL
            key.hasAny("sleep", "night", "morning", "energy", "battery", "rest", "pace", "burnout") -> QuizVisualTheme.ENERGY
            key.hasAny("lifestyle", "routine", "weekend", "adventure", "travel", "comfort", "novelty", "spontaneous") -> QuizVisualTheme.LIFESTYLE
            key.hasAny("logic", "decision", "overthink", "mind", "focus", "intuition", "analysis", "thinking", "stress", "worry") -> QuizVisualTheme.MIND
            key.hasAny("chaos", "control", "risk", "money", "security", "perfection", "impulse", "boundary", "planning") -> QuizVisualTheme.CONTROL
            key.hasAny("productivity", "growth", "discipline", "ambition", "goal", "resilience", "procrast", "motivation", "habit", "success") -> QuizVisualTheme.GROWTH
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
        QuizVisualTheme.ENERGY -> R.drawable.quiz_art_energy
        QuizVisualTheme.LIFESTYLE -> R.drawable.quiz_art_lifestyle
        QuizVisualTheme.IDENTITY -> R.drawable.quiz_art_identity
    }

    fun accentFor(quiz: Quiz): Color = when (themeFor(quiz)) {
        QuizVisualTheme.SOCIAL -> Color(0xFF74E4F4)
        QuizVisualTheme.EMOTION -> Color(0xFFFF89B8)
        QuizVisualTheme.MIND -> Color(0xFFAD91FF)
        QuizVisualTheme.CONTROL -> Color(0xFFFFBF69)
        QuizVisualTheme.GROWTH -> Color(0xFF8CE6A7)
        QuizVisualTheme.VALUES -> Color(0xFFFFD77A)
        QuizVisualTheme.ENERGY -> Color(0xFFA38BFF)
        QuizVisualTheme.LIFESTYLE -> Color(0xFF6EE7F9)
        QuizVisualTheme.IDENTITY -> Color(0xFFC5A8FF)
    }

    private fun String.hasAny(vararg needles: String): Boolean = needles.any(::contains)
}

@Composable
fun QuizArtwork(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) 20.dp else 26.dp)
    val accent = QuizVisuals.accentFor(quiz)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 118.dp else 172.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
    ) {
        Image(
            painter = painterResource(QuizVisuals.drawableFor(quiz)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x22090A0F),
                            Color(0xCC090A0F)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(14.dp)
                .size(if (compact) 38.dp else 46.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(Color(0xB814151D))
                .border(1.dp, accent.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = quiz.accent,
                color = Color.White,
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 14.dp, bottom = 13.dp)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.18f))
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = quiz.time,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
