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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultNextExplorationCard(
    currentQuiz: Quiz,
    catalog: List<Quiz>,
    completed: Set<String>,
    coverage: ProfileCoverage,
    onQuizSelected: (Quiz) -> Unit
) {
    val themeMap = remember(catalog) { catalog.associate { it.id to QuizVisuals.themeFor(it) } }
    val catalogById = remember(catalog) { catalog.associateBy { it.id } }
    val orderedQuizIds = remember(catalog) { catalog.map { it.id } }
    val recommendation = remember(
        currentQuiz.id,
        orderedQuizIds,
        themeMap,
        completed,
        coverage
    ) {
        ResultNextExplorationEngine.recommendForCoverage(
            catalog = catalog,
            completed = completed,
            coverage = coverage
        ) ?: ResultNextExplorationEngine.recommend(
            currentQuizId = currentQuiz.id,
            orderedQuizIds = orderedQuizIds,
            themeByQuizId = themeMap,
            completed = completed
        )
    } ?: return
    val nextQuiz = catalogById[recommendation.quizId] ?: return
    val accent = QuizVisuals.accentFor(nextQuiz)
    val companion = QuizVisuals.companionAccentFor(nextQuiz)
    val nextActionLabel = stringResource(R.string.result_next_action)
    val signatureGuided = recommendation.reason == ResultNextReason.PROFILE_GAP
    var viewedRecommendationKey by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var startedRecommendationKey by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val recommendationKey = "${currentQuiz.id}:${nextQuiz.id}:$signatureGuided"
    LaunchedEffect(recommendationKey) {
        if (viewedRecommendationKey != recommendationKey) {
            runCatching { AppEvents.recommendationView(nextQuiz.id, signatureGuided) }
            viewedRecommendationKey = recommendationKey
        }
    }
    val cardBrush = remember(accent, companion) {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.12f),
                V2Colors.SurfaceElevated,
                companion.copy(alpha = 0.08f)
            )
        )
    }

    V2PressableCard(
        onClick = {
            if (startedRecommendationKey != recommendationKey) {
                startedRecommendationKey = recommendationKey
                runCatching {
                    AppEvents.recommendationStart(nextQuiz.id, signatureGuided)
                    onQuizSelected(nextQuiz)
                }.onFailure {
                    startedRecommendationKey = null
                    AppEvents.recommendationCancel(nextQuiz.id)
                    AppEvents.recordError(
                        it,
                        mapOf(
                            "surface" to "result_next_exploration",
                            "quiz_id" to nextQuiz.id
                        )
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_next_exploration")
            .semantics {
                contentDescription = "$nextActionLabel: ${nextQuiz.title}"
                role = Role.Button
            },
        shape = RoundedCornerShape(V2Radius.Card),
        containerColor = V2Colors.SurfaceElevated
    ) {
        Column(
            Modifier
                .background(cardBrush)
                .padding(16.dp)
        ) {
            QuizArtwork(nextQuiz, compact = true)
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.result_next_label),
                color = companion,
                style = V2Type.Caption,
                fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { heading() }
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
                        ResultNextReason.PROFILE_GAP -> R.string.result_next_profile_gap
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
                nextActionLabel,
                color = accent,
                style = V2Type.Caption,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("result_next_exploration_action")
            )
        }
    }
}
