package com.whoareyou.app

enum class AppScreen {
    DISCOVER,
    PROFILE,
    QUIZ,
    RESULT
}

object AppNavigation {
    fun backDestination(screen: AppScreen): AppScreen? = when (screen) {
        AppScreen.DISCOVER -> null
        AppScreen.PROFILE,
        AppScreen.QUIZ,
        AppScreen.RESULT -> AppScreen.DISCOVER
    }

    fun hasUsableCatalog(quizCount: Int): Boolean = quizCount > 0
}
