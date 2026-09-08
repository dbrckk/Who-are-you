package androidx.compose.foundation.lazy

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.whoareyou.app.DiscoverLibraryEngine
import com.whoareyou.app.GuidedJourneyEngine
import com.whoareyou.app.GuidedJourneyId
import com.whoareyou.app.GuidedJourneyProgress
import com.whoareyou.app.ProfileStore
import com.whoareyou.app.Quiz
import com.whoareyou.app.QuizVisualTheme
import com.whoareyou.app.QuizVisuals
import com.whoareyou.app.R
import com.whoareyou.app.StoredProfile
import com.whoareyou.app.V2Colors
import com.whoareyou.app.V2Radius

/** V2 adapter for the legacy Discover `items(List<Quiz>)` call. */
fun LazyListScope.items(
    items: List<Quiz>,
    key: ((Quiz) -> Any)? = null,
    itemContent: @Composable (Quiz) -> Unit
) {
    item(key = "discover-library-v2") {
        DiscoverIntegratedLibrary(quizzes = items, itemContent = itemContent)
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
    var activeJourneyId by remember { mutableStateOf<GuidedJourneyId?>(null) }

    val journeys = remember(quizzes, completed) { GuidedJourneyEngine.build(quizzes, completed) }
    val activeJourney = journeys.firstOrNull { it.definition.id == activeJourneyId }
    val activeJourneyQuiz = activeJourney?.nextQuizId?.let { id -> quizzes.firstOrNull { it.id == id } }
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
        if (journeys.isNotEmpty()) {
            Text(stringResource(R.string.journeys_title), color = V2Colors.TextPrimary, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text(stringResource(R.string.journeys_subtitle), color = V2Colors.TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(journeys, key = { it.definition.id.name }) { journey ->
                    val nextQuiz = journey.nextQuizId?.let { id -> quizzes.firstOrNull { it.id == id } }
                    GuidedJourneyCard(
                        journey = journey,
                        nextQuiz = nextQuiz,
                        selected = activeJourneyId == journey.definition.id,
                        onClick = { activeJourneyId = if (activeJourneyId == journey.definition.id) null else journey.definition.id }
                    )
                }
            }
            if (activeJourneyQuiz != null) {
                Spacer(Modifier.height(14.dp))
                itemContent(activeJourneyQuiz)
            }
            Spacer(Modifier.height(26.dp))
        }

        Text(stringResource(R.string.library_title), color = V2Colors.TextPrimary, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(5.dp))
        Text(stringResource(R.string.library_subtitle), color = V2Colors.TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it; expanded = false },
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
                    onClick = { selectedTheme = null; expanded = false }
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
            onClick = { unfinishedOnly = !unfinishedOnly; expanded = false }
        )

        Spacer(Modifier.height(16.dp))
        if (visible.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(V2Radius.Card), colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)) {
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(V2Radius.Pill)).background(V2Colors.Surface).clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.library_more_results, results.size - visibleLimit), color = V2Colors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("↓", color = V2Colors.AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun GuidedJourneyCard(
    journey: GuidedJourneyProgress,
    nextQuiz: Quiz?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = nextQuiz?.let(QuizVisuals::accentFor) ?: V2Colors.AccentViolet
    Card(
        modifier = Modifier.width(270.dp).clickable(enabled = nextQuiz != null, onClick = onClick),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = if (selected) accent.copy(alpha = 0.12f) else V2Colors.SurfaceElevated)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(journeyTitle(journey.definition.id), color = V2Colors.TextPrimary, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(7.dp))
            Text(journeyBody(journey.definition.id), color = V2Colors.TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.journey_progress, journey.completedCount, journey.totalCount), color = V2Colors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${journey.progressPercent}%", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = { journey.progressPercent / 100f }, modifier = Modifier.fillMaxWidth().height(7.dp), color = accent, trackColor = V2Colors.Hairline)
            Spacer(Modifier.height(14.dp))
            Text(stringResource(if (journey.isComplete) R.string.journey_revisit else R.string.journey_continue), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun journeyTitle(id: GuidedJourneyId): String = stringResource(when (id) {
    GuidedJourneyId.PERSONALITY -> R.string.journey_personality
    GuidedJourneyId.RELATIONSHIPS -> R.string.journey_relationships
    GuidedJourneyId.INNER_WORLD -> R.string.journey_inner_world
    GuidedJourneyId.VALUES_AND_DIRECTION -> R.string.journey_values
})

@Composable
private fun journeyBody(id: GuidedJourneyId): String = stringResource(when (id) {
    GuidedJourneyId.PERSONALITY -> R.string.journey_personality_body
    GuidedJourneyId.RELATIONSHIPS -> R.string.journey_relationships_body
    GuidedJourneyId.INNER_WORLD -> R.string.journey_inner_world_body
    GuidedJourneyId.VALUES_AND_DIRECTION -> R.string.journey_values_body
})

@Composable
private fun IntegratedLibraryChip(label: String, selected: Boolean, suffix: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(V2Radius.Pill)).background(if (selected) V2Colors.AccentViolet.copy(alpha = 0.18f) else V2Colors.Surface).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = if (selected) V2Colors.TextPrimary else V2Colors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        suffix?.let { Text(it, color = V2Colors.AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Black) }
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
