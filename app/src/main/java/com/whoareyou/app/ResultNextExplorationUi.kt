package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    val companion = QuizVisuals.companionAccentFor(nextQuiz)

    V2PressableCard(
        onClick = { onQuizSelected(nextQuiz) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        containerColor = V2Colors.SurfaceElevated
    ) {
        Column(
            Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.12f),
                            V2Colors.SurfaceElevated,
                            companion.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            QuizArtwork(nextQuiz, compact = true)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.result_next_label),
                color = companion,
                style = V2Type.Caption,
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
                style = V2Type.Supporting
            )
            Spacer(Modifier.height(11.dp))
            Text(
                stringResource(R.string.result_next_action),
                color = accent,
                style = V2Type.Caption,
                fontWeight = FontWeight.Black
            )
        }
    }
}
