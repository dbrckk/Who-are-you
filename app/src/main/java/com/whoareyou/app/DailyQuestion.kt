package com.whoareyou.app

import java.time.LocalDate
import java.util.Locale

data class DailyQuestion(
    val id: String,
    val prompt: String,
    val optionA: String,
    val optionB: String
)

object DailyQuestionEngine {
    private val english = listOf(
        DailyQuestion("perfect_skill_or_language", "Would you rather instantly master any skill or speak every language?", "Master any skill", "Speak every language"),
        DailyQuestion("future_or_past", "Would you rather see 10 years into your future or relive one day from your past?", "See my future", "Relive one day"),
        DailyQuestion("truth_or_comfort", "Would you rather always hear the truth or sometimes keep the comforting version?", "Always the truth", "Sometimes comfort"),
        DailyQuestion("famous_or_free", "Would you rather be famous everywhere or completely free from other people's opinions?", "Be famous", "Be free"),
        DailyQuestion("plan_or_spontaneous", "Would you rather have every weekend planned or decide everything at the last minute?", "Plan it", "Stay spontaneous"),
        DailyQuestion("mind_or_memory", "Would you rather read minds or have perfect memory?", "Read minds", "Perfect memory"),
        DailyQuestion("city_or_nature", "Would you rather live in the center of a huge city or far away in nature?", "Big city", "Nature"),
        DailyQuestion("risk_or_regret", "Would you rather take a risky chance or keep the safe option and wonder what could have happened?", "Take the chance", "Stay safe"),
        DailyQuestion("lead_or_support", "Would you rather lead the group or be the person everyone relies on behind the scenes?", "Lead", "Support"),
        DailyQuestion("love_or_success", "Would you rather find your ideal relationship or achieve your biggest career goal first?", "Relationship", "Career goal"),
        DailyQuestion("alone_or_crowd", "Would you rather spend a free day completely alone or surrounded by people you like?", "Completely alone", "With people"),
        DailyQuestion("logic_or_instinct", "Would you rather make a major decision with pure logic or trust your instinct?", "Pure logic", "Trust instinct"),
        DailyQuestion("repeat_or_unknown", "Would you rather repeat the best year of your life or jump into a completely unknown year?", "Repeat the best", "Choose unknown"),
        DailyQuestion("money_or_time", "Would you rather have twice as much money or twice as much free time?", "More money", "More free time")
    )

    private val french = listOf(
        DailyQuestion("perfect_skill_or_language", "Tu préfères maîtriser instantanément n’importe quelle compétence ou parler toutes les langues ?", "Maîtriser une compétence", "Parler toutes les langues"),
        DailyQuestion("future_or_past", "Tu préfères voir ta vie dans 10 ans ou revivre une journée de ton passé ?", "Voir mon avenir", "Revivre une journée"),
        DailyQuestion("truth_or_comfort", "Tu préfères toujours entendre la vérité ou parfois conserver une version rassurante ?", "Toujours la vérité", "Parfois le réconfort"),
        DailyQuestion("famous_or_free", "Tu préfères être célèbre partout ou totalement libre du regard des autres ?", "Être célèbre", "Être libre"),
        DailyQuestion("plan_or_spontaneous", "Tu préfères planifier tous tes week-ends ou tout décider au dernier moment ?", "Tout planifier", "Rester spontané"),
        DailyQuestion("mind_or_memory", "Tu préfères lire dans les pensées ou avoir une mémoire parfaite ?", "Lire les pensées", "Mémoire parfaite"),
        DailyQuestion("city_or_nature", "Tu préfères vivre au centre d’une immense ville ou loin de tout dans la nature ?", "Grande ville", "Nature"),
        DailyQuestion("risk_or_regret", "Tu préfères tenter une chance risquée ou choisir la sécurité en te demandant ce qui aurait pu arriver ?", "Tenter ma chance", "Rester prudent"),
        DailyQuestion("lead_or_support", "Tu préfères diriger le groupe ou être la personne fiable sur laquelle tout le monde s’appuie ?", "Diriger", "Soutenir"),
        DailyQuestion("love_or_success", "Tu préfères trouver ta relation idéale ou atteindre d’abord ton plus grand objectif professionnel ?", "Relation", "Objectif professionnel"),
        DailyQuestion("alone_or_crowd", "Tu préfères passer une journée libre totalement seul ou entouré de personnes que tu apprécies ?", "Totalement seul", "Avec les autres"),
        DailyQuestion("logic_or_instinct", "Pour une décision importante, tu préfères utiliser uniquement la logique ou suivre ton instinct ?", "Logique pure", "Suivre mon instinct"),
        DailyQuestion("repeat_or_unknown", "Tu préfères revivre la meilleure année de ta vie ou entrer dans une année totalement inconnue ?", "Revivre la meilleure", "Choisir l’inconnu"),
        DailyQuestion("money_or_time", "Tu préfères avoir deux fois plus d’argent ou deux fois plus de temps libre ?", "Plus d’argent", "Plus de temps libre")
    )

    private fun questions(): List<DailyQuestion> = if (Locale.getDefault().language == "fr") french else english

    fun forDate(date: LocalDate = LocalDate.now()): DailyQuestion {
        val questions = questions()
        val index = Math.floorMod(date.toEpochDay(), questions.size.toLong()).toInt()
        return questions[index]
    }

    fun all(): List<DailyQuestion> = questions()
}

data class DailyState(
    val answeredDate: String? = null,
    val questionId: String? = null,
    val selectedOption: Int? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String? = null
) {
    fun answeredToday(date: LocalDate = LocalDate.now()): Boolean = answeredDate == date.toString()
}

object StreakEngine {
    fun next(current: DailyState, activeDate: LocalDate): Pair<Int, Int> {
        val previous = current.lastActiveDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val streak = when {
            previous == activeDate -> current.currentStreak.coerceAtLeast(1)
            previous == activeDate.minusDays(1) -> current.currentStreak.coerceAtLeast(1) + 1
            else -> 1
        }
        return streak to maxOf(current.longestStreak, streak)
    }
}
