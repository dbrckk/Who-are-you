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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun RetentionSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedProfile by ProfileStore.observe(context).collectAsState(initial = StoredProfile())
    val totalQuizCount = remember(context) { QuizRepository.load(context).size }
    val question = remember { DailyQuestionEngine.forDate() }
    val achievements = remember(storedProfile, totalQuizCount) { AchievementEngine.build(storedProfile, totalQuizCount) }
    val pendingAchievementId = storedProfile.pendingAchievementIds.firstOrNull()
    val pendingAchievement = pendingAchievementId?.let { id -> achievements.firstOrNull { it.id == id } }

    LaunchedEffect(question.id) { AppEvents.dailyQuestionView(question.id) }
    LaunchedEffect(pendingAchievementId, pendingAchievement) {
        if (pendingAchievementId != null && pendingAchievement == null) {
            ProfileStore.consumeAchievementUnlock(context, pendingAchievementId)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (pendingAchievement != null) {
            AchievementUnlockCard(pendingAchievement) {
                scope.launch { ProfileStore.consumeAchievementUnlock(context, pendingAchievement.id) }
            }
        }
        DailyQuestionCard(
            question = question,
            state = storedProfile.daily,
            onVote = { option ->
                if (!storedProfile.daily.answeredToday()) {
                    scope.launch {
                        val result = ProfileStore.saveDailyAnswer(context, question.id, option)
                        AppEvents.dailyQuestionVote(question.id, option)
                        AppEvents.streakContinue(result.currentStreak)
                    }
                }
            },
            onShare = { DailyQuestionShare.share(context, question) }
        )
        AchievementStrip(achievements)
    }
}

@Composable
private fun AchievementUnlockCard(achievement: Achievement, onDismiss: () -> Unit) {
    val (title, description) = localizedAchievement(achievement.id, achievement.title, achievement.description)
    Card(
        colors = CardDefaults.cardColors(containerColor = RetentionPanelSoft),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.achievement_unlocked_label), color = RetentionCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text("★", color = RetentionViolet, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(9.dp))
            Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text(description, color = RetentionMuted, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.achievement_unlocked_dismiss),
                color = RetentionViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }
    }
}

@Composable
fun DailyQuestionCard(
    question: DailyQuestion,
    state: DailyState,
    onVote: (Int) -> Unit,
    onShare: () -> Unit
) {
    val answered = state.answeredToday() && state.questionId == question.id
    Card(colors = CardDefaults.cardColors(containerColor = RetentionPanel), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
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
                if (answered) stringResource(R.string.daily_answer_locked, state.longestStreak) else stringResource(R.string.daily_pick_prompt),
                color = RetentionMuted,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.daily_share),
                color = RetentionViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onShare)
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
    val nextLocked = achievements.firstOrNull { !it.unlocked }
    var expanded by remember { mutableStateOf(false) }
    val visibleAchievements = if (expanded) achievements else unlocked.take(3)

    Card(
        colors = CardDefaults.cardColors(containerColor = RetentionPanel),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.achievements), color = RetentionCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${unlocked.size}/${achievements.size}  ${if (expanded) "⌃" else "⌄"}", color = RetentionViolet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            if (!expanded && unlocked.isEmpty()) {
                Text(stringResource(R.string.achievement_first_hint), color = RetentionMuted, fontSize = 13.sp)
            } else {
                visibleAchievements.forEach { achievement ->
                    val (title, description) = localizedAchievement(achievement.id, achievement.title, achievement.description)
                    Text(
                        "${if (achievement.unlocked) "✓" else "○"}  $title",
                        color = if (achievement.unlocked) Color.White else RetentionMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(description, color = RetentionMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                }
                if (!expanded && unlocked.size > 3) {
                    Text(stringResource(R.string.achievement_more_unlocked, unlocked.size - 3), color = RetentionMuted, fontSize = 12.sp)
                }
            }

            if (!expanded && nextLocked != null) {
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.next_achievement), color = RetentionViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                val (title, description) = localizedAchievement(nextLocked.id, nextLocked.title, nextLocked.description)
                Text("○  $title", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = RetentionMuted, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun localizedAchievement(id: String, fallbackTitle: String, fallbackDescription: String): Pair<String, String> = when (id) {
    "first_test" -> stringResource(R.string.achievement_first_test_title) to stringResource(R.string.achievement_first_test_description)
    "profile_builder" -> stringResource(R.string.achievement_profile_builder_title) to stringResource(R.string.achievement_profile_builder_description)
    "ten_tests" -> stringResource(R.string.achievement_ten_tests_title) to stringResource(R.string.achievement_ten_tests_description)
    "twenty_five_tests" -> stringResource(R.string.achievement_twenty_five_tests_title) to stringResource(R.string.achievement_twenty_five_tests_description)
    "profile_complete" -> stringResource(R.string.achievement_profile_complete_title) to stringResource(R.string.achievement_profile_complete_description)
    "strong_match" -> stringResource(R.string.achievement_strong_match_title) to stringResource(R.string.achievement_strong_match_description)
    "opposites" -> stringResource(R.string.achievement_opposites_title) to stringResource(R.string.achievement_opposites_description)
    "social_butterfly" -> stringResource(R.string.achievement_social_butterfly_title) to stringResource(R.string.achievement_social_butterfly_description)
    "streak_3" -> stringResource(R.string.achievement_streak_3_title) to stringResource(R.string.achievement_streak_3_description)
    "streak_7" -> stringResource(R.string.achievement_streak_7_title) to stringResource(R.string.achievement_streak_7_description)
    "streak_30" -> stringResource(R.string.achievement_streak_30_title) to stringResource(R.string.achievement_streak_30_description)
    else -> fallbackTitle to fallbackDescription
}
