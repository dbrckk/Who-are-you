package com.whoareyou.app

import android.content.Context
import android.content.Intent

object DailyQuestionShare {
    fun share(context: Context, question: DailyQuestion) {
        AppEvents.log("daily_question_share", mapOf("question_id" to question.id))
        val text = context.getString(
            R.string.daily_share_text,
            question.prompt,
            question.optionA,
            question.optionB
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.daily_share_chooser))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
