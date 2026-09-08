package com.whoareyou.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class ChallengeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incomingUri = intent?.data
        val incoming = ChallengeShare.parse(incomingUri)
        val quiz = incoming?.let { QuizRepository.find(this, it.quizId) }

        if (incomingUri?.scheme == "https") {
            AppEvents.appLinkOpen(incomingUri.path.orEmpty())
        }
        if (incoming != null) {
            val source = if (incomingUri?.scheme == "https") "https" else "legacy_scheme"
            AppEvents.challengeOpen(incoming.quizId, source)
        }

        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = V2Colors.Ink,
                    surface = V2Colors.Surface,
                    primary = V2Colors.AccentViolet,
                    secondary = V2Colors.AccentCyan
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = V2Colors.Ink) {
                    if (incoming == null || quiz == null) {
                        InvalidChallengeScreen { finish() }
                    } else {
                        ChallengeFlow(quiz, incoming.inviterScore) { finish() }
                    }
                }
            }
        }
    }
}
