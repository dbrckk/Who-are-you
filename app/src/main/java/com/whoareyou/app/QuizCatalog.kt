package com.whoareyou.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class Answer(val text: String, val score: Int)
data class Question(val text: String, val answers: List<Answer>)
data class Quiz(
    val id: String,
    val title: String,
    val hook: String,
    val time: String,
    val accent: String,
    val lowTitle: String,
    val midTitle: String,
    val highTitle: String,
    val lowDescription: String,
    val midDescription: String,
    val highDescription: String,
    val metricLow: String,
    val metricHigh: String,
    val questions: List<Question>
)

object QuizRepository {
    private val defaultAssets = listOf("quizzes.json", "quizzes-extra.json")
    @Volatile private var cachedLanguage: String? = null
    @Volatile private var cached: List<Quiz>? = null

    fun load(context: Context): List<Quiz> {
        val language = context.resources.configuration.locales[0]?.language ?: Locale.getDefault().language
        val assets = defaultAssets.map { defaultName ->
            val localizedName = defaultName.removeSuffix(".json") + "-$language.json"
            if (language != "en" && assetExists(context, localizedName)) localizedName else defaultName
        }
        val current = cached
        if (current != null && cachedLanguage == language) return current

        return synchronized(this) {
            cached?.takeIf { cachedLanguage == language } ?: assets.flatMap { assetName ->
                parse(context.assets.open(assetName).bufferedReader().use { it.readText() })
            }.also { catalog ->
                require(catalog.isNotEmpty()) { "Quiz catalog cannot be empty" }
                require(catalog.map { it.id }.distinct().size == catalog.size) { "Quiz IDs must be unique" }
                cachedLanguage = language
                cached = catalog
            }
        }
    }

    fun find(context: Context, id: String): Quiz? = load(context).firstOrNull { it.id == id }

    private fun assetExists(context: Context, name: String): Boolean =
        runCatching { context.assets.open(name).close() }.isSuccess

    private fun parse(raw: String): List<Quiz> {
        val root = JSONObject(raw)
        val items = root.getJSONArray("quizzes")
        return List(items.length()) { index -> parseQuiz(items.getJSONObject(index)) }
    }

    private fun parseQuiz(json: JSONObject): Quiz {
        val questions = json.getJSONArray("questions").mapObjects { questionJson ->
            val answers = questionJson.getJSONArray("answers").mapObjects { answerJson ->
                Answer(answerJson.getString("text"), answerJson.getInt("score").coerceIn(0, 3))
            }
            require(answers.size == 4) { "Each question must have exactly 4 answers" }
            Question(questionJson.getString("text"), answers)
        }
        require(questions.isNotEmpty()) { "Quiz ${json.getString("id")} has no questions" }

        val results = json.getJSONObject("results")
        return Quiz(
            id = json.getString("id"),
            title = json.getString("title"),
            hook = json.getString("hook"),
            time = json.getString("time"),
            accent = json.getString("accent"),
            lowTitle = results.getJSONObject("low").getString("title"),
            midTitle = results.getJSONObject("mid").getString("title"),
            highTitle = results.getJSONObject("high").getString("title"),
            lowDescription = results.getJSONObject("low").getString("description"),
            midDescription = results.getJSONObject("mid").getString("description"),
            highDescription = results.getJSONObject("high").getString("description"),
            metricLow = json.getString("metricLow"),
            metricHigh = json.getString("metricHigh"),
            questions = questions
        )
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }
}
