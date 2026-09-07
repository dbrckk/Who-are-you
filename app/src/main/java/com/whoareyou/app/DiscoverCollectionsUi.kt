package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DiscoverCollection(
    val theme: QuizVisualTheme,
    val titleRes: Int,
    val accent: Color
)

private val discoverCollections = listOf(
    DiscoverCollection(QuizVisualTheme.IDENTITY, R.string.discover_collection_identity, Color(0xFFB59CFF)),
    DiscoverCollection(QuizVisualTheme.EMOTION, R.string.discover_collection_relationships, Color(0xFFFF8EBB)),
    DiscoverCollection(QuizVisualTheme.MIND, R.string.discover_collection_mind, Color(0xFF7CDCF4)),
    DiscoverCollection(QuizVisualTheme.GROWTH, R.string.discover_collection_growth, Color(0xFF8DE0B7)),
    DiscoverCollection(QuizVisualTheme.ENERGY, R.string.discover_collection_energy, Color(0xFFFFC978)),
    DiscoverCollection(QuizVisualTheme.VALUES, R.string.discover_collection_values, Color(0xFFC6A4FF)),
    DiscoverCollection(QuizVisualTheme.SOCIAL, R.string.discover_collection_social, Color(0xFF74D4FF)),
    DiscoverCollection(QuizVisualTheme.CONTROL, R.string.discover_collection_control, Color(0xFFFF9C86)),
    DiscoverCollection(QuizVisualTheme.LIFESTYLE, R.string.discover_collection_lifestyle, Color(0xFFA3D7B6))
)

@Composable
fun DiscoverCollections(
    quizzes: List<Quiz>,
    completed: Set<String>,
    onQuizSelected: ((Quiz) -> Unit)? = null
) {
    val populated = remember(quizzes) {
        discoverCollections.mapNotNull { collection ->
            val matching = quizzes.filter { QuizVisuals.themeFor(it) == collection.theme }
            collection.takeIf { matching.isNotEmpty() }?.let { it to matching }
        }
    }

    if (populated.isEmpty()) return

    Column {
        Text(
            stringResource(R.string.discover_collections_title),
            color = Color.White,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.discover_collections_subtitle),
            color = V2Colors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(populated, key = { it.first.theme.name }) { (collection, matching) ->
                val previewQuiz = matching.firstOrNull { it.id !in completed } ?: matching.first()
                CollectionCard(
                    title = stringResource(collection.titleRes),
                    count = matching.size,
                    accent = collection.accent,
                    quiz = previewQuiz,
                    onClick = onQuizSelected?.let { callback -> { callback(previewQuiz) } }
                )
            }
        }
    }
}

@Composable
private fun CollectionCard(
    title: String,
    count: Int,
    accent: Color,
    quiz: Quiz,
    onClick: (() -> Unit)?
) {
    val cardModifier = if (onClick != null) {
        Modifier.width(236.dp).clickable(onClick = onClick)
    } else {
        Modifier.width(236.dp)
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(topStart = V2Radius.Card, topEnd = V2Radius.Card))
            ) {
                QuizArtwork(quiz, compact = true)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC090A0F))
                            )
                        )
                )
                Text(
                    quiz.accent,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    fontSize = 25.sp
                )
            }

            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Box(
                        Modifier.height(9.dp).width(9.dp).clip(RoundedCornerShape(99.dp)).background(accent)
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.discover_collection_count, count),
                    color = V2Colors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    quiz.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (onClick != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.discover_collection_open),
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverLibraryHeader() {
    Column {
        Text(stringResource(R.string.discover_library_title), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.discover_library_subtitle),
            color = V2Colors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
