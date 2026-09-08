package com.whoareyou.app

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
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
    val editorial = remember(quizzes, completed) { editorialPicks(quizzes, completed) }

    if (populated.isEmpty()) return

    Column {
        if (editorial.isNotEmpty()) {
            EditorialStrip(editorial, completed, onQuizSelected)
            Spacer(Modifier.height(28.dp))
        }

        Text(
            stringResource(R.string.discover_collections_title),
            color = V2Colors.TextPrimary,
            style = V2Type.SectionTitle
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.discover_collections_subtitle),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting
        )
        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(populated, key = { it.first.theme.name }) { (collection, matching) ->
                val previewQuiz = matching.firstOrNull { it.id !in completed } ?: matching.first()
                val completedCount = matching.count { it.id in completed }
                CollectionCard(
                    title = stringResource(collection.titleRes),
                    completedCount = completedCount,
                    totalCount = matching.size,
                    accent = collection.accent,
                    quiz = previewQuiz,
                    onClick = onQuizSelected?.let { callback -> { callback(previewQuiz) } }
                )
            }
        }
    }
}

private fun editorialPicks(quizzes: List<Quiz>, completed: Set<String>): List<Quiz> {
    val preferred = quizzes.filterNot { it.id in completed } + quizzes.filter { it.id in completed }
    val seenThemes = mutableSetOf<QuizVisualTheme>()
    val picks = mutableListOf<Quiz>()

    preferred.forEach { quiz ->
        val theme = QuizVisuals.themeFor(quiz)
        if (theme !in seenThemes) {
            seenThemes += theme
            picks += quiz
        }
        if (picks.size == 3) return picks
    }

    if (picks.size < 3) {
        preferred.forEach { quiz ->
            if (quiz !in picks) picks += quiz
            if (picks.size == 3) return picks
        }
    }
    return picks
}

@Composable
private fun EditorialStrip(
    quizzes: List<Quiz>,
    completed: Set<String>,
    onQuizSelected: ((Quiz) -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.discover_editorial_title),
                    color = V2Colors.TextPrimary,
                    style = V2Type.SectionTitle
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.discover_editorial_subtitle),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Supporting
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.discover_editorial_badge),
                color = V2Colors.AccentCyan,
                style = V2Type.Caption
            )
        }
        Spacer(Modifier.height(15.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(quizzes, key = { _, quiz -> quiz.id }) { index, quiz ->
                EditorialCard(
                    quiz = quiz,
                    completed = quiz.id in completed,
                    featured = index == 0,
                    onClick = onQuizSelected?.let { callback -> { callback(quiz) } }
                )
            }
        }
    }
}

@Composable
private fun EditorialCard(
    quiz: Quiz,
    completed: Boolean,
    featured: Boolean,
    onClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(if (featured) 30.dp else 24.dp)
    V2PressableCard(
        onClick = onClick,
        modifier = Modifier.width(if (featured) 286.dp else 218.dp),
        shape = shape,
        containerColor = V2Colors.SurfaceElevated
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (featured) 164.dp else 126.dp)
                    .clip(RoundedCornerShape(topStart = if (featured) 30.dp else 24.dp, topEnd = if (featured) 30.dp else 24.dp))
            ) {
                QuizArtwork(quiz, compact = !featured)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x22090A0F), Color(0xE8090A0F))
                            )
                        )
                )
                Text(
                    if (completed) "✓" else stringResource(R.string.discover_editorial_next),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xB30B0C12))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    color = if (completed) V2Colors.AccentCyan else V2Colors.TextPrimary,
                    style = V2Type.Caption
                )
            }

            Column(Modifier.padding(if (featured) 18.dp else 15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        quiz.title,
                        modifier = Modifier.weight(1f),
                        color = V2Colors.TextPrimary,
                        fontSize = if (featured) 19.sp else 15.sp,
                        lineHeight = if (featured) 24.sp else 19.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(quiz.accent, fontSize = if (featured) 22.sp else 18.sp)
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    quiz.hook,
                    color = V2Colors.TextSecondary,
                    style = V2Type.Supporting,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    quiz.time,
                    color = V2Colors.AccentViolet,
                    style = V2Type.Caption
                )
            }
        }
    }
}

@Composable
private fun CollectionCard(
    title: String,
    completedCount: Int,
    totalCount: Int,
    accent: Color,
    quiz: Quiz,
    onClick: (() -> Unit)?
) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    V2PressableCard(
        onClick = onClick,
        modifier = Modifier.width(236.dp),
        shape = RoundedCornerShape(V2Radius.Card),
        containerColor = V2Colors.Surface
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
                    Text(title, color = V2Colors.TextPrimary, style = V2Type.Metric)
                    Text(
                        "$completedCount/$totalCount",
                        color = if (completedCount == totalCount) V2Colors.AccentCyan else accent,
                        style = V2Type.Caption
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = accent,
                    trackColor = V2Colors.Hairline
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.discover_collection_count, totalCount),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Caption
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    quiz.title,
                    color = V2Colors.TextPrimary,
                    style = V2Type.Supporting,
                    fontWeight = FontWeight.SemiBold
                )
                if (onClick != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.discover_collection_open),
                        color = accent,
                        style = V2Type.Caption
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverLibraryHeader() {
    Column {
        Text(
            stringResource(R.string.discover_library_title),
            color = V2Colors.TextPrimary,
            style = V2Type.SectionTitle
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.discover_library_subtitle),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting
        )
    }
}
