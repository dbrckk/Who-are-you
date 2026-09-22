package com.whoareyou.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class Answer(val text: String, val score: Int)
data class Question(val text: String, val answers: List<Answer>)
data class QuizTraitWeight(val id: String, val weight: Double)
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
    val questions: List<Question>,
    val resultIntelligence: QuizResultIntelligenceContent? = null,
    val traits: List<QuizTraitWeight> = emptyList()
)

object QuizRepository {
    private val defaultAssets = listOf(
        "quizzes.json",
        "quizzes-extra.json",
        "quizzes-growth.json",
        "quizzes-growth2.json",
        "quizzes-growth3.json"
    )
    @Volatile private var cachedLanguage: String? = null
    @Volatile private var cached: List<Quiz>? = null

    fun load(context: Context): List<Quiz> {
        val appContext = context.applicationContext
        val deviceLanguage = appContext.resources.configuration.locales[0]?.language ?: Locale.getDefault().language
        val language = supportedCatalogLanguage(deviceLanguage)
        cached?.takeIf { cachedLanguage == language }?.let { return it }

        return synchronized(this) {
            cached?.takeIf { cachedLanguage == language } ?: run {
                val assets = defaultAssets.map { defaultName ->
                    val localizedName = defaultName.removeSuffix(".json") + "-$language.json"
                    if (language == "fr" && assetExists(appContext, localizedName)) localizedName else defaultName
                }
                assets.flatMap { assetName ->
                    parse(appContext.assets.open(assetName).bufferedReader().use { it.readText() })
                }
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
        require(root.getInt("version") == 2) { "Unsupported quiz catalog schema version" }
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
        val traits = json.optJSONArray("traits")?.mapObjects { traitJson ->
            val id = traitJson.getString("id")
            val weight = traitJson.getDouble("weight")
            require(id.isNotBlank()) { "Trait id cannot be blank" }
            require(weight in -1.0..1.0 && weight != 0.0) {
                "Trait weight for $id must be within [-1, 1] and non-zero"
            }
            QuizTraitWeight(id = id, weight = weight)
        }.orEmpty()
        require(traits.isNotEmpty()) { "Quiz ${json.getString("id")} must define at least one trait" }

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
            questions = questions,
            traits = traits
        )
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }
}


internal fun supportedCatalogLanguage(deviceLanguage: String?): String =
    if (deviceLanguage.equals("fr", ignoreCase = true)) "fr" else "en"
