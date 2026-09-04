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
        reportPreviousFatalCrash()
        installFatalCrashBuffer()
    }

    private fun reportPreviousFatalCrash() {
        val prefs = getSharedPreferences(CRASH_PREFS, MODE_PRIVATE)
        val errorClass = prefs.getString(KEY_CLASS, null) ?: return
        AppEvents.log(
            "fatal_crash_previous_session",
            mapOf(
                "error" to errorClass,
                "message" to prefs.getString(KEY_MESSAGE, "").orEmpty(),
                "stack" to prefs.getString(KEY_STACK, "").orEmpty().take(12000),
                "timestamp_ms" to prefs.getLong(KEY_TIMESTAMP, 0L)
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
