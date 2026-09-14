package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NextQuizRecommendationCard(
    recommendation: NextQuizRecommendation,
    catalog: List<Quiz>,
    onStartQuiz: (Quiz) -> Unit,
    modifier: Modifier = Modifier
) {
    val quiz = catalog.firstOrNull { it.id == recommendation.quizId } ?: return
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val traits = recommendation.targetedTraitIds
        .map { TraitLocalization.label(it, french) }
        .joinToString(", ")

    V2Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (french) "Prochaine étape utile" else "Useful next step",
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = quiz.title,
                color = V2Colors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recommendationReason(recommendation, traits, french),
                color = V2Colors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { onStartQuiz(quiz) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (recommendation.reason == NextQuizReason.RETAKE_DUE) {
                        if (french) "Refaire ce test" else "Retake this test"
                    } else {
                        if (french) "Faire ce test" else "Take this test"
                    }
                )
            }
        }
    }
}

private fun recommendationReason(
    recommendation: NextQuizRecommendation,
    traits: String,
    french: Boolean
): String = when (recommendation.reason) {
    NextQuizReason.NEW_COVERAGE -> if (french) {
        "Pourquoi maintenant ? Ce test apporte de nouvelles informations sur : $traits."
    } else {
        "Why now? This test adds new information about: $traits."
    }
    NextQuizReason.RESOLVE_UNCERTAINTY -> if (french) {
        "Pourquoi maintenant ? Certaines informations restent incertaines ou contradictoires sur : $traits."
    } else {
        "Why now? Some evidence remains uncertain or contradictory for: $traits."
    }
    NextQuizReason.RETAKE_DUE -> {
        val days = recommendation.daysSinceLastAttempt ?: 0
        if (french) {
            "Pourquoi maintenant ? $days jours se sont écoulés depuis la dernière mesure. Un retake peut actualiser : $traits."
        } else {
            "Why now? It has been $days days since the last measurement. A retake can refresh: $traits."
        }
    }
    NextQuizReason.NONE -> if (french) "Aucune action nécessaire pour le moment." else "No action is needed right now."
}
