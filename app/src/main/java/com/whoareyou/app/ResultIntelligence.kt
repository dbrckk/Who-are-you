package com.whoareyou.app

enum class ResultIntelligenceFrame {
    BALANCED,
    LEANING,
    PRONOUNCED
}

enum class ResultInsightCue {
    FLEXIBILITY,
    CONTEXT_AWARENESS,
    CONSISTENCY,
    DECISIVENESS,
    AMBIGUITY,
    CONTEXT_BLIND_SPOT,
    OVEREXTENSION,
    NOTICE_CONTEXT,
    USE_STRENGTH_DELIBERATELY,
    TEST_OPPOSITE
}

data class ResultAnswerEvidence(
    val questionIndex: Int,
    val questionText: String,
    val answerText: String,
    val answerScore: Int,
    val direction: ResultDirection,
    val influence: Int
)

data class ResultIntelligenceSummary(
    val score: Int,
    val direction: ResultDirection,
    val frame: ResultIntelligenceFrame,
    val hasDirectionalLean: Boolean,
    val evidence: List<ResultAnswerEvidence>,
    val strengthCues: List<ResultInsightCue>,
    val watchOutCues: List<ResultInsightCue>,
    val actionCue: ResultInsightCue
)

object ResultIntelligenceEngine {
    fun derive(
        quiz: Quiz,
        score: Int,
        selectedAnswerIndexes: List<Int>
    ): ResultIntelligenceSummary {
        val interpretation = ResultInterpretationEngine.derive(score)
        val frame = when (interpretation.strength) {
            ResultSignalStrength.BALANCED -> ResultIntelligenceFrame.BALANCED
            ResultSignalStrength.CLEAR -> ResultIntelligenceFrame.LEANING
            ResultSignalStrength.STRONG -> ResultIntelligenceFrame.PRONOUNCED
        }
        val evidence = quiz.questions.mapIndexedNotNull { questionIndex, question ->
            val answerIndex = selectedAnswerIndexes.getOrNull(questionIndex) ?: return@mapIndexedNotNull null
            val answer = question.answers.getOrNull(answerIndex) ?: return@mapIndexedNotNull null
            ResultAnswerEvidence(
                questionIndex = questionIndex,
                questionText = question.text,
                answerText = answer.text,
                answerScore = answer.score,
                direction = if (answer.score <= 1) ResultDirection.LOW else ResultDirection.HIGH,
                influence = kotlin.math.abs(answer.score * 2 - 3)
            )
        }.sortedWith(
            compareByDescending<ResultAnswerEvidence> { it.influence }
                .thenBy { it.questionIndex }
        ).take(3)

        val strengthCues = when (frame) {
            ResultIntelligenceFrame.BALANCED ->
                listOf(ResultInsightCue.FLEXIBILITY, ResultInsightCue.CONTEXT_AWARENESS)
            ResultIntelligenceFrame.LEANING ->
                listOf(ResultInsightCue.CONSISTENCY, ResultInsightCue.FLEXIBILITY)
            ResultIntelligenceFrame.PRONOUNCED ->
                listOf(ResultInsightCue.DECISIVENESS, ResultInsightCue.CONSISTENCY)
        }
        val watchOutCues = when (frame) {
            ResultIntelligenceFrame.BALANCED -> listOf(ResultInsightCue.AMBIGUITY)
            ResultIntelligenceFrame.LEANING -> listOf(ResultInsightCue.CONTEXT_BLIND_SPOT)
            ResultIntelligenceFrame.PRONOUNCED -> listOf(ResultInsightCue.OVEREXTENSION)
        }
        val actionCue = when (frame) {
            ResultIntelligenceFrame.BALANCED -> ResultInsightCue.NOTICE_CONTEXT
            ResultIntelligenceFrame.LEANING -> ResultInsightCue.USE_STRENGTH_DELIBERATELY
            ResultIntelligenceFrame.PRONOUNCED -> ResultInsightCue.TEST_OPPOSITE
        }

        return ResultIntelligenceSummary(
            score = interpretation.score,
            direction = interpretation.direction,
            frame = frame,
            hasDirectionalLean = frame != ResultIntelligenceFrame.BALANCED,
            evidence = evidence,
            strengthCues = strengthCues,
            watchOutCues = watchOutCues,
            actionCue = actionCue
        )
    }
}
