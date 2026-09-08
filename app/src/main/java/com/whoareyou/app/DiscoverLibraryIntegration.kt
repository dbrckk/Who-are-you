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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Specialized overload for the legacy Discover quiz list.
 *
 * MainActivity still calls `items(List<Quiz>)`; this more-specific overload turns that
 * flat list into the V2 searchable/filterable library while preserving the existing
 * quiz-card navigation callback carried by [itemContent]. Lists of other model types
 * continue to use Compose's normal generic `items` overload.
 */
fun LazyListScope.items(
    items: List<Quiz>,
    key: ((Quiz) -> Any)? = null,
    itemContent: @Composable (Quiz) -> Unit
) {
    item(key = "discover-library-v2") {
        DiscoverIntegratedLibrary(
            quizzes = items,
            itemContent = itemContent
        )
    }
}

@Composable
private fun DiscoverIntegratedLibrary(
    quizzes: List<Quiz>,
    itemContent: @Composable (Quiz) -> Unit
) {
    val context = LocalContext.current
    val storedProfile by ProfileStore.observe(context).collectAsState(initial = StoredProfile())
    val completed = storedProfile.completedQuizIds

    var query by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<QuizVisualTheme?>(null) }
    var unfinishedOnly by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
    val activeFilter = query.isNotBlank() || selectedTheme != null || unfinishedOnly
    val visibleLimit = if (expanded || activeFilter) 8 else 4
    val visible = results.take(visibleLimit)

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
            onValueChange = {
                query = it
                expanded = false
            },
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
                IntegratedLibraryChip(
                    label = stringResource(R.string.library_all),
                    selected = selectedTheme == null,
                    onClick = {
                        selectedTheme = null
                        expanded = false
                    }
                )
            }
            items(summaries, key = { it.theme.name }) { summary ->
                IntegratedLibraryChip(
                    label = integratedThemeLabel(summary.theme),
                    selected = selectedTheme == summary.theme,
                    suffix = "${summary.completedCount}/${summary.totalCount}",
                    onClick = {
                        selectedTheme = if (selectedTheme == summary.theme) null else summary.theme
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        IntegratedLibraryChip(
            label = stringResource(R.string.library_unfinished_only),
            selected = unfinishedOnly,
            onClick = {
                unfinishedOnly = !unfinishedOnly
                expanded = false
            }
        )

        Spacer(Modifier.height(16.dp))
        if (visible.isEmpty()) {
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
            visible.forEach { quiz ->
                itemContent(quiz)
                Spacer(Modifier.height(10.dp))
            }
        }

        if (!activeFilter && !expanded && results.size > visibleLimit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(V2Radius.Pill))
                    .background(V2Colors.Surface)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.library_more_results, results.size - visibleLimit),
                    color = V2Colors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "↓",
                    color = V2Colors.AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun IntegratedLibraryChip(
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
private fun integratedThemeLabel(theme: QuizVisualTheme): String = when (theme) {
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
