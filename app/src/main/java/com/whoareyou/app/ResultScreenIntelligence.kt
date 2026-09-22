package com.whoareyou.app

data class ResultScreenIntelligenceState(
    val insight: ResultInsightSummary?,
    val evidence: List<ResultEvidence>
)

object ResultScreenIntelligence {
    fun derive(
        quiz: Quiz,
        score: Int,
        evidence: List<ResultEvidence>
    ): ResultScreenIntelligenceState = ResultScreenIntelligenceState(
        insight = ResultInsightEngine.derive(quiz, score),
        evidence = evidence
    )
}
