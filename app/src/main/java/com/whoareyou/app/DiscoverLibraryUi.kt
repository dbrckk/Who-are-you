package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val LibraryPageSize = 8

@Composable
fun DiscoverLibrary(
    quizzes: List<Quiz>,
    completed: Set<String>,
    onQuizSelected: (Quiz) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<QuizVisualTheme?>(null) }
    var unfinishedOnly by remember { mutableStateOf(false) }
    var visibleCount by remember { mutableStateOf(LibraryPageSize) }

    val summaries = remember(quizzes, completed) { DiscoverLibraryEngine.summaries(quizzes, completed) }
    val results = remember(quizzes, completed, query, selectedTheme, unfinishedOnly) {
        DiscoverLibraryEngine.search(
            quizzes = quizzes,
            query = query,
            theme = selectedTheme,
            completed = completed,
            unfinishedOnly = unfinishedOnly
        )
    }

    LaunchedEffect(query, selectedTheme, unfinishedOnly) {
        visibleCount = LibraryPageSize
    }

    Column {
        Text(
            stringResource(R.string.library_title),
            color = V2Colors.TextPrimary,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.library_subtitle),
            color = V2Colors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.library_search_hint)) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = V2Colors.TextPrimary,
                unfocusedTextColor = V2Colors.TextPrimary,
                focusedBorderColor = V2Colors.AccentViolet,
                unfocusedBorderColor = V2Colors.Hairline,
                cursorColor = V2Colors.AccentCyan,
                focusedContainerColor = V2Colors.Surface,
                unfocusedContainerColor = V2Colors.Surface
            )
        )

        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                LibraryChip(
                    label = stringResource(R.string.library_all),
                    selected = selectedTheme == null,
                    onClick = { selectedTheme = null }
                )
            }
            items(summaries, key = { it.theme.name }) { summary ->
                LibraryChip(
                    label = themeLabel(summary.theme),
                    selected = selectedTheme == summary.theme,
                    suffix = "${summary.completedCount}/${summary.totalCount}",
                    onClick = { selectedTheme = if (selectedTheme == summary.theme) null else summary.theme }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        LibraryChip(
            label = stringResource(R.string.library_unfinished_only),
            selected = unfinishedOnly,
            onClick = { unfinishedOnly = !unfinishedOnly }
        )

        Spacer(Modifier.height(16.dp))
        if (results.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(V2Radius.Card),
                colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.library_empty_title), color = V2Colors.TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(stringResource(R.string.library_empty_body), color = V2Colors.TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            results.take(visibleCount).forEach { quiz ->
                LibraryResultCard(
                    quiz = quiz,
                    completed = quiz.id in completed,
                    onClick = { onQuizSelected(quiz) }
                )
                Spacer(Modifier.height(10.dp))
            }

            val remaining = (results.size - visibleCount).coerceAtLeast(0)
            if (remaining > 0) {
                TextButton(
                    onClick = { visibleCount = (visibleCount + LibraryPageSize).coerceAtMost(results.size) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.library_show_more, minOf(LibraryPageSize, remaining), remaining),
                        color = V2Colors.AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryChip(
    label: String,
    selected: Boolean,
    suffix: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(V2Radius.Pill))
            .background(if (selected) V2Colors.AccentViolet.copy(alpha = 0.18f) else V2Colors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            color = if (selected) V2Colors.TextPrimary else V2Colors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        suffix?.let {
            Text(it, color = V2Colors.AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LibraryResultCard(
    quiz: Quiz,
    completed: Boolean,
    onClick: () -> Unit
) {
    val accent = QuizVisuals.accentFor(quiz)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(vertical = 14.dp)
            ) {
                Text(quiz.accent, modifier = Modifier.padding(horizontal = 15.dp), fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        quiz.title,
                        modifier = Modifier.weight(1f),
                        color = V2Colors.TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (completed) "✓" else "→", color = if (completed) V2Colors.AccentCyan else accent, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    quiz.hook,
                    color = V2Colors.TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(7.dp))
                Text(quiz.time, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun themeLabel(theme: QuizVisualTheme): String = when (theme) {
    QuizVisualTheme.IDENTITY -> stringResource(R.string.library_theme_identity)
    QuizVisualTheme.EMOTION -> stringResource(R.string.library_theme_emotion)
    QuizVisualTheme.MIND -> stringResource(R.string.library_theme_mind)
    QuizVisualTheme.CONTROL -> stringResource(R.string.library_theme_control)
    QuizVisualTheme.GROWTH -> stringResource(R.string.library_theme_growth)
    QuizVisualTheme.VALUES -> stringResource(R.string.library_theme_values)
    QuizVisualTheme.ENERGY -> stringResource(R.string.library_theme_energy)
    QuizVisualTheme.LIFESTYLE -> stringResource(R.string.library_theme_lifestyle)
    QuizVisualTheme.SOCIAL -> stringResource(R.string.library_theme_social)
}
