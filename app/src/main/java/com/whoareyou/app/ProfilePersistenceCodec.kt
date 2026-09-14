package com.whoareyou.app

data class TimedScore(val score: Int, val epochDay: Long)

object ProfilePersistenceCodec {
    fun decodeSet(raw: String?): Set<String> = decodeList(raw).toSet()

    fun decodeList(raw: String?): List<String> = raw
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.distinct()
        ?: emptyList()

    fun encodeScores(scores: Map<String, Int>): String = scores.entries
        .mapNotNull { (rawId, score) -> rawId.trim().takeIf(String::isNotEmpty)?.let { it to score.coerceIn(0, 100) } }
        .distinctBy { it.first }
        .sortedBy { it.first }
        .joinToString(";") { (id, score) -> "$id:$score" }

    fun decodeScores(raw: String?): Map<String, Int> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val id = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val score = parts[1].trim().toIntOrNull()?.coerceIn(0, 100) ?: return@mapNotNull null
            id to score
        }
        ?.toMap()
        ?: emptyMap()

    fun encodeStringMap(values: Map<String, String>): String = values.entries
        .mapNotNull { (rawKey, rawValue) ->
            val key = rawKey.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val value = rawValue.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            key to value
        }
        .distinctBy { it.first }
        .sortedBy { it.first }
        .joinToString(";") { (key, value) -> "$key:$value" }

    fun encodeTimedScoreHistory(values: Map<String, List<TimedScore>>): String = values.entries
        .mapNotNull { (rawId, rawScores) ->
            val id = rawId.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val scores = rawScores.map {
                TimedScore(
                    score = it.score.coerceIn(0, 100),
                    epochDay = it.epochDay.coerceAtLeast(0)
                )
            }
            if (scores.isEmpty()) return@mapNotNull null
            id to scores
        }
        .sortedBy { it.first }
        .joinToString(";") { (id, scores) ->
            "$id:" + scores.joinToString("|") { "${it.score}@${it.epochDay}" }
        }

    fun decodeTimedScoreHistory(raw: String?): Map<String, List<TimedScore>> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val id = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val scores = parts[1].split('|').mapNotNull { token ->
                val scoreParts = token.split('@', limit = 2)
                if (scoreParts.size != 2) return@mapNotNull null
                val score = scoreParts[0].toIntOrNull()?.coerceIn(0, 100) ?: return@mapNotNull null
                val day = scoreParts[1].toLongOrNull()?.coerceAtLeast(0) ?: return@mapNotNull null
                TimedScore(score, day)
            }
            if (scores.isEmpty()) return@mapNotNull null
            id to scores
        }
        ?.toMap()
        ?: emptyMap()

    fun encodeScoreHistory(values: Map<String, List<Int>>): String = values.entries
        .mapNotNull { (rawId, rawScores) ->
            val id = rawId.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val scores = rawScores.map { it.coerceIn(0, 100) }
            if (scores.isEmpty()) return@mapNotNull null
            id to scores
        }
        .sortedBy { it.first }
        .joinToString(";") { (id, scores) ->
            "$id:" + scores.joinToString("|")
        }

    fun decodeScoreHistory(raw: String?): Map<String, List<Int>> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val id = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val scores = parts[1]
                .split('|')
                .mapNotNull { it.trim().toIntOrNull()?.coerceIn(0, 100) }
            if (scores.isEmpty()) return@mapNotNull null
            id to scores
        }
        ?.toMap()
        ?: emptyMap()

    fun decodeStringMap(raw: String?): Map<String, String> = raw
        ?.split(';')
        ?.mapNotNull { item ->
            val parts = item.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val key = parts[0].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val value = parts[1].trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            key to value
        }
        ?.toMap()
        ?: emptyMap()
}
