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
        QuizVisualTheme.SOCIAL -> V2Colors.Cyan
        QuizVisualTheme.EMOTION -> V2Colors.Rose
        QuizVisualTheme.MIND -> V2Colors.VioletBright
        QuizVisualTheme.CONTROL -> V2Colors.Peach
        QuizVisualTheme.GROWTH -> V2Colors.Success
        QuizVisualTheme.VALUES -> V2Colors.Orchid
        QuizVisualTheme.ENERGY -> V2Colors.Violet
        QuizVisualTheme.LIFESTYLE -> V2Colors.Blue
        QuizVisualTheme.IDENTITY -> V2Colors.Orchid
    }

    fun companionAccentFor(quiz: Quiz): Color = when (themeFor(quiz)) {
        QuizVisualTheme.SOCIAL -> V2Colors.Blue
        QuizVisualTheme.EMOTION -> V2Colors.Orchid
        QuizVisualTheme.MIND -> V2Colors.Blue
        QuizVisualTheme.CONTROL -> V2Colors.Rose
        QuizVisualTheme.GROWTH -> V2Colors.Cyan
        QuizVisualTheme.VALUES -> V2Colors.Peach
        QuizVisualTheme.ENERGY -> V2Colors.Cyan
        QuizVisualTheme.LIFESTYLE -> V2Colors.Peach
        QuizVisualTheme.IDENTITY -> V2Colors.VioletBright
    }

    private fun String.hasAny(vararg needles: String): Boolean = needles.any(::contains)
}

@Composable
fun QuizArtwork(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) V2Radius.Compact else V2Radius.Card)
    val accent = QuizVisuals.accentFor(quiz)
    val companion = QuizVisuals.companionAccentFor(quiz)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 118.dp else 172.dp)
            .clip(shape)
            .border(1.dp, accent.copy(alpha = 0.24f), shape)
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
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.10f),
                            Color.Transparent,
                            companion.copy(alpha = 0.13f)
                        )
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            V2Colors.OverlaySoft,
                            V2Colors.OverlayStrong
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
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(alpha = 0.24f),
                            V2Colors.SurfaceGlass
                        )
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.48f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = quiz.accent,
                color = V2Colors.TextPrimary,
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 14.dp, bottom = 13.dp)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(V2Radius.Pill))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = 0.22f),
                            companion.copy(alpha = 0.13f)
                        )
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(V2Radius.Pill))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = quiz.time,
                color = V2Colors.TextPrimary.copy(alpha = 0.90f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
