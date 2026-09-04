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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RetentionPanel = Color(0xFF14151D)
private val RetentionPanelSoft = Color(0xFF1B1D27)
private val RetentionViolet = Color(0xFF9C7BFF)
private val RetentionCyan = Color(0xFF6EE7F9)
private val RetentionMuted = Color(0xFFA4A7B5)

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
                Text("DAILY QUESTION", color = RetentionCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("🔥 ${state.currentStreak}", color = RetentionViolet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            Text(question.prompt, color = Color.White, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            DailyChoice(question.optionA, selected = answered && state.selectedOption == 0, enabled = !answered) { onVote(0) }
            Spacer(Modifier.height(9.dp))
            DailyChoice(question.optionB, selected = answered && state.selectedOption == 1, enabled = !answered) { onVote(1) }
            Spacer(Modifier.height(12.dp))
            Text(
                if (answered) "Answer locked for today • longest streak ${state.longestStreak} days"
                else "Pick one. A new question appears tomorrow.",
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
                Text("ACHIEVEMENTS", color = RetentionCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${unlocked.size}/${achievements.size}", color = RetentionViolet, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            if (unlocked.isEmpty()) {
                Text("Complete your first test to unlock your first badge.", color = RetentionMuted, fontSize = 13.sp)
            } else {
                unlocked.take(3).forEach { achievement ->
                    Text("${achievement.icon}  ${achievement.title}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                }
                if (unlocked.size > 3) {
                    Text("+${unlocked.size - 3} more unlocked", color = RetentionMuted, fontSize = 12.sp)
                }
            }
        }
    }
}
