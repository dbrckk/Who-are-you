package com.whoareyou.app

data class ResultInsightContent(
    val strengths: List<String>,
    val watchOuts: List<String>,
    val everydayLife: List<String>,
    val reflection: String
)

data class QuizResultIntelligenceContent(
    val low: ResultInsightContent,
    val balanced: ResultInsightContent,
    val high: ResultInsightContent
)

data class ResultInsightSummary(
    val direction: ResultDirection,
    val strength: ResultSignalStrength,
    val strengths: List<String>,
    val watchOuts: List<String>,
    val everydayLife: List<String>,
    val reflection: String
)

object ResultInsightEngine {
    fun derive(quiz: Quiz, score: Int): ResultInsightSummary? {
        val authored = quiz.resultIntelligence ?: return null
        val interpretation = ResultInterpretationEngine.derive(score)
        val content = when {
            interpretation.strength == ResultSignalStrength.BALANCED -> authored.balanced
            interpretation.direction == ResultDirection.LOW -> authored.low
            else -> authored.high
        }
        return ResultInsightSummary(
            direction = interpretation.direction,
            strength = interpretation.strength,
            strengths = content.strengths.take(3),
            watchOuts = content.watchOuts.take(3),
            everydayLife = content.everydayLife.take(3),
            reflection = content.reflection
        )
    }
}
