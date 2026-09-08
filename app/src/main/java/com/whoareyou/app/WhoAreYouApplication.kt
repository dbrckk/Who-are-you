package com.whoareyou.app

import android.app.Application

class WhoAreYouApplication : Application() {
    companion object {
        private const val CRASH_PREFS = "fatal_crash_buffer"
        private const val KEY_CLASS = "class"
        private const val KEY_MESSAGE = "message"
        private const val KEY_STACK = "stack"
        private const val KEY_TIMESTAMP = "timestamp"
    }

    override fun onCreate() {
        super.onCreate()
        AppEvents.configure()
        AppEvents.appOpen()
        reportPreviousFatalCrash()
        installFatalCrashBuffer()
    }

    private fun reportPreviousFatalCrash() {
        val prefs = getSharedPreferences(CRASH_PREFS, MODE_PRIVATE)
        val errorClass = prefs.getString(KEY_CLASS, null) ?: return
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        val ageMinutes = if (timestamp > 0L) {
            ((System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 60_000L).coerceAtMost(10_080L)
        } else {
            -1L
        }

        AppEvents.log(
            "fatal_crash_previous_session",
            mapOf(
                "error" to errorClass.substringAfterLast('.').take(64),
                "age_minutes" to ageMinutes
            )
        )
        prefs.edit().clear().apply()
    }

    private fun installFatalCrashBuffer() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                getSharedPreferences(CRASH_PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_CLASS, throwable::class.java.name)
                    .putString(KEY_MESSAGE, throwable.message.orEmpty())
                    .putString(KEY_STACK, throwable.stackTraceToString().take(12000))
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .commit()
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
