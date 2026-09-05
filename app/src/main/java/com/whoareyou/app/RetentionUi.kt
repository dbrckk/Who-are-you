package com.whoareyou.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val RetentionPanel = Color(0xFF14151D)
private val RetentionPanelSoft = Color(0xFF1B1D27)
private val RetentionViolet = Color(0xFF9C7BFF)
private val RetentionCyan = Color(0xFF6EE7F9)
private val RetentionMuted = Color(0xFFA4A7B5)

@Composable
fun RetentionSection(totalQuizCount: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedProfile by ProfileStore.observe(context).collectAsState(initial = StoredProfile())
    val question = remember { DailyQuestionEngine.forDate() }
    val achievements = remember(storedProfile, totalQuizCount) {
        AchievementEngine.build(storedProfile, totalQuizCount)
    }

    LaunchedEffect(question.id) {
        AppEvents.dailyQuestionView(question.id)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DailyQuestionCard(
            question = question,
            state = storedProfile.daily,
            onVote = { option ->
                if (!storedProfile.daily.answeredToday()) {
                    scope.launch {
                        val beforeUnlocked = AchievementEngine
                            .unlocked(storedProfile, totalQuizCount)
                            .map { it.id }
                            .toSet()
                        val result = ProfileStore.saveDailyAnswer(context, question.id, option)
                        AppEvents.dailyQuestionVote(question.id, option)
                        AppEvents.streakContinue(result.currentStreak)
                        val afterProfile = storedProfile.copy(daily = result)
                        AchievementEngine.unlocked(afterProfile, totalQuizCount)
                            .filterNot { it.id in beforeUnlocked }
                            .forEach { AppEvents.achievementUnlock(it.id) }
                    }
                }
            }
        )
        AchievementStrip(achievements)
    }
}

@Composable
fun DailyQuestionCard(
    question: DailyQuestion,
    state: DailyState,
    onVote: (Int) -> Unit
) {
    val answered = state.answeredToday() && state.questionId == question.id
    Card(
        colors = CardDefaults.cardColors(containerColor = RetentionPanel),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.daily_question), color = RetentionCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.streak_count, state.currentStreak), color = RetentionViolet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            Text(question.prompt, color = Color.White, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            DailyChoice(question.optionA, selected = answered && state.selectedOption == 0, enabled = !answered) { onVote(0) }
            Spacer(Modifier.height(9.dp))
            DailyChoice(question.optionB, selected = answered && state.selectedOption == 1, enabled = !answered) { onVote(1) }
            Spacer(Modifier.height(12.dp))
            Text(
                if (answered) stringResource(R.string.daily_answer_locked, state.longestStreak)
                else stringResource(R.string.daily_pick_prompt),
                color = RetentionMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DailyChoice(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) RetentionViolet else RetentionPanelSoft),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            if (selected) "✓  $label" else label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            color = if (selected) Color(0xFF090A0F) else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AchievementStrip(achievements: List<Achievement>) {
    val unlocked = achievements.filter { it.unlocked }
    Card(
        colors = CardDefaults.cardColors(containerColor = RetentionPanel),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.achievements), color = RetentionCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${unlocked.size}/${achievements.size}", color = RetentionViolet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            if (unlocked.isEmpty()) {
                Text(stringResource(R.string.achievement_first_hint), color = RetentionMuted, fontSize = 13.sp)
            } else {
                unlocked.take(3).forEach { achievement ->
                    val (title, description) = localizedAchievement(achievement.id, achievement.title, achievement.description)
                    Text("✓  $title", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(description, color = RetentionMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(7.dp))
                }
                if (unlocked.size > 3) {
                    Text(stringResource(R.string.achievement_more_unlocked, unlocked.size - 3), color = RetentionMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun localizedAchievement(id: String, fallbackTitle: String, fallbackDescription: String): Pair<String, String> = when (id) {
    "first_test" -> stringResource(R.string.achievement_first_test_title) to stringResource(R.string.achievement_first_test_description)
    "ten_tests" -> stringResource(R.string.achievement_ten_tests_title) to stringResource(R.string.achievement_ten_tests_description)
    "profile_builder" -> stringResource(R.string.achievement_profile_builder_title) to stringResource(R.string.achievement_profile_builder_description)
    "profile_complete" -> stringResource(R.string.achievement_profile_complete_title) to stringResource(R.string.achievement_profile_complete_description)
    "streak_3" -> stringResource(R.string.achievement_streak_3_title) to stringResource(R.string.achievement_streak_3_description)
    "streak_7" -> stringResource(R.string.achievement_streak_7_title) to stringResource(R.string.achievement_streak_7_description)
    "streak_30" -> stringResource(R.string.achievement_streak_30_title) to stringResource(R.string.achievement_streak_30_description)
    else -> fallbackTitle to fallbackDescription
}
