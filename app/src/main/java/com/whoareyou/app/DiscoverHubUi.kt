package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single Discover surface for the V2 product experience.
 *
 * Keeps the information architecture explicit: profile -> journeys -> curated discovery -> library.
 * The legacy long quiz list can remain available during migration without being the primary UX.
 */
@Composable
fun DiscoverHub(
    quizzes: List<Quiz>,
    profile: GlobalProfileSummary,
    completed: Set<String>,
    onOpenProfile: () -> Unit,
    onQuizSelected: (Quiz) -> Unit
) {
    val journeys = remember(quizzes, completed) {
        GuidedJourneyEngine.build(quizzes, completed)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = V2Colors.AccentViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = stringResource(R.string.discover_headline),
                color = V2Colors.TextPrimary,
                fontSize = 38.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.discover_subtitle),
                color = V2Colors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }

        item {
            PersonalizedDiscoverDashboard(
                profile = profile,
                quizzes = quizzes,
                completed = completed,
                onOpenProfile = onOpenProfile,
                onQuizSelected = onQuizSelected
            )
        }

        if (journeys.isNotEmpty()) {
            item {
                GuidedJourneySection(
                    journeys = journeys,
                    quizzes = quizzes,
                    onQuizSelected = onQuizSelected
                )
            }
        }

        item {
            DiscoverCollections(
                quizzes = quizzes,
                completed = completed,
                onQuizSelected = onQuizSelected
            )
        }

        item {
            DiscoverLibrary(
                quizzes = quizzes,
                completed = completed,
                onQuizSelected = onQuizSelected
            )
            Spacer(Modifier.height(108.dp))
        }
    }
}

@Composable
private fun GuidedJourneySection(
    journeys: List<GuidedJourneyProgress>,
    quizzes: List<Quiz>,
    onQuizSelected: (Quiz) -> Unit
) {
    val quizzesById = remember(quizzes) { quizzes.associateBy { it.id } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.journeys_title),
            color = V2Colors.TextPrimary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = stringResource(R.string.journeys_subtitle),
            color = V2Colors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        journeys.forEach { journey ->
            val nextQuiz = journey.nextQuizId?.let(quizzesById::get)
            JourneyCard(
                journey = journey,
                onClick = nextQuiz?.let { quiz -> { onQuizSelected(quiz) } }
            )
        }
    }
}

@Composable
private fun JourneyCard(
    journey: GuidedJourneyProgress,
    onClick: (() -> Unit)?
) {
    val title = when (journey.definition.id) {
        GuidedJourneyId.PERSONALITY -> stringResource(R.string.journey_personality)
        GuidedJourneyId.RELATIONSHIPS -> stringResource(R.string.journey_relationships)
        GuidedJourneyId.INNER_WORLD -> stringResource(R.string.journey_inner_world)
        GuidedJourneyId.VALUES_AND_DIRECTION -> stringResource(R.string.journey_values)
    }
    val body = when (journey.definition.id) {
        GuidedJourneyId.PERSONALITY -> stringResource(R.string.journey_personality_body)
        GuidedJourneyId.RELATIONSHIPS -> stringResource(R.string.journey_relationships_body)
        GuidedJourneyId.INNER_WORLD -> stringResource(R.string.journey_inner_world_body)
        GuidedJourneyId.VALUES_AND_DIRECTION -> stringResource(R.string.journey_values_body)
    }
    val modifier = Modifier
        .fillMaxWidth()
        .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = V2Colors.TextPrimary,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (journey.isComplete) "✓" else "${journey.progressPercent}%",
                    color = if (journey.isComplete) V2Colors.AccentCyan else V2Colors.AccentViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                color = V2Colors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { journey.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(5.dp),
                color = V2Colors.AccentViolet,
                trackColor = V2Colors.Hairline
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = if (journey.isComplete) {
                    stringResource(R.string.journey_revisit)
                } else {
                    stringResource(R.string.journey_progress, journey.completedCount, journey.totalCount)
                },
                color = V2Colors.AccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
