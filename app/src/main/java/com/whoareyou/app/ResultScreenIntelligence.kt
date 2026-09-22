package com.whoareyou.app

data class ResultScreenIntelligenceState(
    val insight: ResultInsightSummary?,
    val evidence: List<ResultEvidence>,
    val connections: List<ResultProfileConnection>
)

object ResultScreenIntelligence {
    fun derive(
        quiz: Quiz,
        score: Int,
        evidence: List<ResultEvidence>,
        graph: TraitGraph = TraitGraph(emptyList(), 0)
    ): ResultScreenIntelligenceState = ResultScreenIntelligenceState(
        insight = ResultInsightEngine.derive(quiz, score),
        evidence = evidence,
        connections = ResultProfileConnectionEngine.derive(quiz, score, graph)
    )
}
