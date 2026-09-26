package com.whoareyou.app

import java.nio.charset.StandardCharsets
import java.util.Base64

object BehaviorGoalStoreCodec {
    private const val VERSION = "v1"
    private const val MAX_GOALS = 20
    private const val NULL = "~"

    fun encode(goals: List<BehaviorGoal>): String = buildString {
        append(VERSION)
        retain(goals).forEach { goal ->
            append('\n')
            append(
                listOf(
                    encodeText(goal.id),
                    goal.metric.name,
                    goal.targetValue.toString(),
                    goal.startEpochDay.toString(),
                    goal.durationDays.toString(),
                    if (goal.paused) "1" else "0",
                    goal.packageName?.let(::encodeText) ?: NULL
                ).joinToString("|")
            )
        }
    }

    fun decode(payload: String?): List<BehaviorGoal> {
        if (payload.isNullOrBlank()) return emptyList()
        return runCatching {
            val lines = payload.lineSequence().toList()
            require(lines.firstOrNull() == VERSION)
            retain(
                lines.drop(1)
                    .filter { it.isNotBlank() }
                    .map(::decodeGoal)
            )
        }.getOrElse { emptyList() }
    }

    fun retain(goals: List<BehaviorGoal>): List<BehaviorGoal> = goals
        .groupBy { it.id }
        .map { (_, duplicates) -> duplicates.maxBy(::canonicalGoalKey) }
        .sortedWith(
            compareByDescending<BehaviorGoal> { it.startEpochDay }
                .thenBy { it.id }
        )
        .take(MAX_GOALS)

    fun upsert(goals: List<BehaviorGoal>, goal: BehaviorGoal): List<BehaviorGoal> =
        retain(goals.filterNot { it.id == goal.id } + goal)

    fun setPaused(goals: List<BehaviorGoal>, id: String, paused: Boolean): List<BehaviorGoal> =
        retain(goals.map { goal -> if (goal.id == id) goal.copy(paused = paused) else goal })

    fun remove(goals: List<BehaviorGoal>, id: String): List<BehaviorGoal> =
        retain(goals.filterNot { it.id == id })

    private fun decodeGoal(line: String): BehaviorGoal {
        val fields = line.split('|', limit = 7)
        require(fields.size == 7)
        return BehaviorGoal(
            id = decodeText(fields[0]),
            metric = BehaviorGoalMetric.valueOf(fields[1]),
            targetValue = fields[2].toLong(),
            startEpochDay = fields[3].toLong(),
            durationDays = fields[4].toInt(),
            paused = when (fields[5]) {
                "1" -> true
                "0" -> false
                else -> error("Invalid paused flag")
            },
            packageName = if (fields[6] == NULL) null else decodeText(fields[6])
        )
    }

    private fun canonicalGoalKey(goal: BehaviorGoal): String = listOf(
        goal.metric.name,
        goal.targetValue.toString(),
        goal.startEpochDay.toString(),
        goal.durationDays.toString(),
        if (goal.paused) "1" else "0",
        goal.packageName.orEmpty()
    ).joinToString("|")

    private fun encodeText(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )
}
