package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TraitExplorationDialog(
    exploration: TraitExploration,
    catalogById: Map<String, Quiz>,
    onDismiss: () -> Unit,
    onStartQuiz: (Quiz) -> Unit
) {
    val french = LocalConfiguration.current.locales[0]?.language == "fr"
    val traitLabel = TraitLocalization.label(exploration.trait.traitId, french)
    val recommendation = exploration.recommendedQuizId?.let(catalogById::get)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(traitLabel, modifier = Modifier.semantics { heading() }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = statusExplanation(exploration.trait, french),
                    color = V2Colors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (exploration.evidence.isNotEmpty()) {
                    Text(
                        text = if (french) "CE QUI CONTRIBUE" else "WHAT CONTRIBUTES",
                        color = V2Colors.TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    exploration.evidence.take(4).forEach { evidence ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clearAndSetSemantics {
                                    contentDescription = "${evidence.quizTitle}. ${evidence.contribution}%"
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = evidence.quizTitle,
                                modifier = Modifier.weight(1f),
                                color = V2Colors.TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${evidence.contribution}%",
                                color = V2Colors.TextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (french)
                            "Aucun test terminé n’apporte encore de preuve sur ce trait."
                        else
                            "No completed test provides evidence for this trait yet.",
                        color = V2Colors.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                recommendation?.let { quiz ->
                    Text(
                        text = if (french)
                            "Meilleure prochaine mesure : ${quiz.title}"
                        else
                            "Best next measurement: ${quiz.title}",
                        color = V2Colors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { onStartQuiz(quiz) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(if (french) "Explorer ce trait" else "Explore this trait")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (french) "Fermer" else "Close")
            }
        }
    )
}

private fun statusExplanation(trait: TraitCoverage, french: Boolean): String = when (trait.status) {
    CoverageStatus.UNKNOWN -> if (french)
        "Ce trait n’a pas encore été mesuré."
    else
        "This trait has not been measured yet."
    CoverageStatus.LOW -> if (french)
        "Le signal existe, mais il repose encore sur trop peu de données ou une confiance faible."
    else
        "A signal exists, but it still relies on limited evidence or low confidence."
    CoverageStatus.DEVELOPING -> if (french)
        "Plusieurs éléments contribuent déjà à ce trait, mais le profil peut encore être précisé."
    else
        "Several signals already contribute to this trait, but the profile can still be refined."
    CoverageStatus.STRONG -> if (french)
        "Ce trait est soutenu par plusieurs signaux cohérents."
    else
        "This trait is supported by multiple consistent signals."
}
