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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
 * Information architecture: identity -> momentum -> guided journeys -> curated discovery -> library -> premium.
 * Feature-specific sections stay self-contained so the hub does not recursively render other Discover surfaces.
 */
@Composable
fun DiscoverHub(
    quizzes: List<Quiz>,
    profile: GlobalProfileSummary,
    storedProfile: StoredProfile,
    completed: Set<String>,
    adsRemoved: Boolean,
    onOpenProfile: () -> Unit,
    onQuizSelected: (Quiz) -> Unit,
    onRemoveAds: () -> Unit
) {
    val journeys = remember(quizzes, completed) {
        GuidedJourneyEngine.build(quizzes, completed)
    }
    val unlockedAchievements = remember(storedProfile, quizzes.size) {
        AchievementEngine.build(storedProfile, quizzes.size).count { it.unlocked }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = V2Spacing.Screen),
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

        item {
            DiscoverMomentumCard(
                streak = storedProfile.daily.currentStreak,
                unlockedAchievements = unlockedAchievements,
                matchCount = storedProfile.matchCount
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
        }

        item {
            PremiumDiscoverCard(
                adsRemoved = adsRemoved,
                onRemoveAds = onRemoveAds
            )
            Spacer(Modifier.height(108.dp))
        }
    }
}

@Composable
private fun DiscoverMomentumCard(
    streak: Int,
    unlockedAchievements: Int,
    matchCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.discover_momentum_title),
                color = V2Colors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.discover_momentum_subtitle),
                color = V2Colors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MomentumStat(
                    value = streak.toString(),
                    label = stringResource(R.string.discover_streak_label),
                    modifier = Modifier.weight(1f)
                )
                MomentumStat(
                    value = unlockedAchievements.toString(),
                    label = stringResource(R.string.discover_achievements_label),
                    modifier = Modifier.weight(1f)
                )
                MomentumStat(
                    value = matchCount.toString(),
                    label = stringResource(R.string.discover_matches_label),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MomentumStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(V2Colors.Ink.copy(alpha = 0.48f), RoundedCornerShape(V2Radius.Compact))
            .padding(horizontal = 11.dp, vertical = 12.dp)
    ) {
        Text(
            text = value,
            color = V2Colors.AccentCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = V2Colors.TextSecondary,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold
        )
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

@Composable
private fun PremiumDiscoverCard(
    adsRemoved: Boolean,
    onRemoveAds: () -> Unit
) {
    val premiumPrice = BillingPriceState.displayPrice

    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface),
        shape = RoundedCornerShape(V2Radius.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                stringResource(if (adsRemoved) R.string.lifetime_upgrade_active else R.string.remove_ads_forever),
                color = V2Colors.AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(9.dp))
            Text(
                if (adsRemoved) stringResource(R.string.no_ads_ever)
                else stringResource(R.string.premium_once_no_subscription, premiumPrice),
                color = V2Colors.TextPrimary,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(7.dp))
            Text(
                stringResource(if (adsRemoved) R.string.premium_restore_copy else R.string.premium_copy),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            if (!adsRemoved) {
                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = onRemoveAds,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentViolet),
                    shape = RoundedCornerShape(V2Radius.Compact)
                ) {
                    Text(
                        stringResource(R.string.remove_ads_button, premiumPrice),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
