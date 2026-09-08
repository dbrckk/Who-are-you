package com.whoareyou.app

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultNextExplorationCard(
    currentQuiz: Quiz,
    catalog: List<Quiz>,
    completed: Set<String>,
    onQuizSelected: (Quiz) -> Unit
) {
    val themeMap = remember(catalog) { catalog.associate { it.id to QuizVisuals.themeFor(it) } }
    val recommendation = remember(currentQuiz.id, catalog, completed) {
        ResultNextExplorationEngine.recommend(
            currentQuizId = currentQuiz.id,
            orderedQuizIds = catalog.map { it.id },
            themeByQuizId = themeMap,
            completed = completed
        )
    } ?: return
    val nextQuiz = catalog.firstOrNull { it.id == recommendation.quizId } ?: return
    val accent = QuizVisuals.accentFor(nextQuiz)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onQuizSelected(nextQuiz) },
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated)
    ) {
        Column(Modifier.padding(16.dp)) {
            QuizArtwork(nextQuiz, compact = true)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.result_next_label),
                color = V2Colors.AccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    nextQuiz.title,
                    modifier = Modifier.weight(1f),
                    color = V2Colors.TextPrimary,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(10.dp))
                Text(nextQuiz.accent, fontSize = 22.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    when (recommendation.reason) {
                        ResultNextReason.SAME_FACET -> R.string.result_next_same_facet
                        ResultNextReason.COMPLEMENTARY_FACET -> R.string.result_next_complementary
                        ResultNextReason.RETAKE_FACET -> R.string.result_next_retake
                    }
                ),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(11.dp))
            Text(
                stringResource(R.string.result_next_action),
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
