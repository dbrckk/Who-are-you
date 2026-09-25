package com.whoareyou.app

enum class AppScreen {
    DISCOVER,
    PROFILE,
    HABITS,
    QUIZ,
    RESULT
}

object AppNavigation {
    fun backDestination(screen: AppScreen): AppScreen? = when (screen) {
        AppScreen.DISCOVER -> null
        AppScreen.PROFILE,
        AppScreen.HABITS,
        AppScreen.QUIZ,
        AppScreen.RESULT -> AppScreen.DISCOVER
    }

    fun habitsDestination(): AppScreen = AppScreen.HABITS

    fun hasUsableCatalog(quizCount: Int): Boolean = quizCount > 0
}
