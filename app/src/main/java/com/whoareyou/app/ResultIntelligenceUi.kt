package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultIntelligencePanel(
    quiz: Quiz,
    score: Int,
    selectedAnswerIndexes: List<Int>
) {
    val summary = remember(quiz.id, score, selectedAnswerIndexes) {
        ResultIntelligenceEngine.derive(
            quiz = quiz,
            score = score,
            selectedAnswerIndexes = selectedAnswerIndexes
        )
    }
    val accent = QuizVisuals.accentFor(quiz)
    val companion = QuizVisuals.companionAccentFor(quiz)
    val primaryPole = if (summary.direction == ResultDirection.LOW) quiz.metricLow else quiz.metricHigh
    val oppositePole = if (summary.direction == ResultDirection.LOW) quiz.metricHigh else quiz.metricLow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_intelligence_panel")
    ) {
        Text(
            text = stringResource(R.string.result_intel_title),
            color = V2Colors.TextPrimary,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.result_intel_subtitle),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting
        )
        Spacer(Modifier.height(14.dp))

        ResultIntelligenceSection(
            title = stringResource(R.string.result_intel_evidence_title),
            accent = accent
        ) {
            Text(
                text = stringResource(R.string.result_intel_evidence_intro),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting
            )
            Spacer(Modifier.height(10.dp))
            if (summary.evidence.isEmpty()) {
                Text(
                    text = stringResource(R.string.result_intel_evidence_unavailable),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Body
                )
            } else {
                summary.evidence.forEachIndexed { index, evidence ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_intelligence_evidence_$index"),
                        shape = RoundedCornerShape(V2Radius.Compact),
                        colors = CardDefaults.cardColors(containerColor = V2Colors.Surface)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = evidence.questionText,
                                color = V2Colors.TextSecondary,
                                style = V2Type.Supporting
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = evidence.answerText,
                                color = V2Colors.TextPrimary,
                                style = V2Type.BodyStrong
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        ResultIntelligenceSection(
            title = stringResource(R.string.result_intel_strengths_title),
            accent = companion
        ) {
            summary.strengthCues.forEachIndexed { index, cue ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                Text(
                    text = resultIntelligenceCueText(cue, primaryPole, oppositePole),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Body
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        ResultIntelligenceSection(
            title = stringResource(R.string.result_intel_watchouts_title),
            accent = V2Colors.Peach
        ) {
            summary.watchOutCues.forEachIndexed { index, cue ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                Text(
                    text = resultIntelligenceCueText(cue, primaryPole, oppositePole),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Body
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        ResultIntelligenceSection(
            title = stringResource(R.string.result_intel_everyday_title),
            accent = accent
        ) {
            Text(
                text = if (summary.hasDirectionalLean) {
                    resultEverydayContextText(
                        theme = QuizVisuals.themeFor(quiz),
                        primaryPole = primaryPole,
                        oppositePole = oppositePole
                    )
                } else {
                    stringResource(
                        R.string.result_intel_everyday_balanced,
                        quiz.metricLow,
                        quiz.metricHigh
                    )
                },
                color = V2Colors.TextPrimary,
                style = V2Type.Body
            )
        }

        Spacer(Modifier.height(12.dp))
        ResultIntelligenceSection(
            title = stringResource(R.string.result_intel_action_title),
            accent = companion
        ) {
            Text(
                text = resultIntelligenceCueText(summary.actionCue, primaryPole, oppositePole),
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong
            )
        }
    }
}

@Composable
private fun ResultIntelligenceSection(
    title: String,
    accent: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(V2Radius.Card),
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.10f),
                            V2Colors.SurfaceElevated
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Text(
                text = title,
                color = accent,
                style = V2Type.Eyebrow
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun resultIntelligenceCueText(
    cue: ResultInsightCue,
    primaryPole: String,
    oppositePole: String
): String = when (cue) {
    ResultInsightCue.FLEXIBILITY ->
        stringResource(R.string.result_intel_strength_flexibility, primaryPole, oppositePole)
    ResultInsightCue.CONTEXT_AWARENESS ->
        stringResource(R.string.result_intel_strength_context)
    ResultInsightCue.CONSISTENCY ->
        stringResource(R.string.result_intel_strength_consistency, primaryPole)
    ResultInsightCue.DECISIVENESS ->
        stringResource(R.string.result_intel_strength_decisiveness, primaryPole)
    ResultInsightCue.AMBIGUITY ->
        stringResource(R.string.result_intel_watch_ambiguity)
    ResultInsightCue.CONTEXT_BLIND_SPOT ->
        stringResource(R.string.result_intel_watch_context_blind, primaryPole, oppositePole)
    ResultInsightCue.OVEREXTENSION ->
        stringResource(R.string.result_intel_watch_overextension, primaryPole, oppositePole)
    ResultInsightCue.NOTICE_CONTEXT ->
        stringResource(R.string.result_intel_action_notice_context, primaryPole, oppositePole)
    ResultInsightCue.USE_STRENGTH_DELIBERATELY ->
        stringResource(R.string.result_intel_action_use_strength, primaryPole, oppositePole)
    ResultInsightCue.TEST_OPPOSITE ->
        stringResource(R.string.result_intel_action_test_opposite, oppositePole)
}

@Composable
private fun resultEverydayContextText(
    theme: QuizVisualTheme,
    primaryPole: String,
    oppositePole: String
): String {
    val resource = when (theme) {
        QuizVisualTheme.SOCIAL -> R.string.result_intel_everyday_social
        QuizVisualTheme.EMOTION -> R.string.result_intel_everyday_emotion
        QuizVisualTheme.MIND -> R.string.result_intel_everyday_mind
        QuizVisualTheme.CONTROL -> R.string.result_intel_everyday_control
        QuizVisualTheme.GROWTH -> R.string.result_intel_everyday_growth
        QuizVisualTheme.VALUES -> R.string.result_intel_everyday_values
        QuizVisualTheme.ENERGY -> R.string.result_intel_everyday_energy
        QuizVisualTheme.LIFESTYLE -> R.string.result_intel_everyday_lifestyle
        QuizVisualTheme.IDENTITY -> R.string.result_intel_everyday_identity
    }
    return stringResource(resource, primaryPole, oppositePole)
}
