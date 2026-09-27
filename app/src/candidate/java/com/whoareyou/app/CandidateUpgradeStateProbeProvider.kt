package com.whoareyou.app

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CandidateUpgradeStateProbeProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        if (method != METHOD_STATE) {
            return Bundle().apply { putString("probe_error", "unsupported_method") }
        }

        val appContext = requireNotNull(context).applicationContext
        return runCatching {
            runBlocking {
                val profile = ProfileStore.observe(appContext).first()
                val behavior = BehaviorRepository.observe(appContext).first()
                val goals = BehaviorGoalRepository.observe(appContext).first()
                val today = behavior.today
                val goal = goals.firstOrNull { it.id == "upgrade-probe-goal" }

                Bundle().apply {
                    putString("onboarding", profile.onboardingComplete.toString())
                    putString("ads_removed", profile.adsRemoved.toString())
                    putString(
                        "activity_state",
                        behavior.sourceStates[BehaviorSource.ACTIVITY]?.name.orEmpty()
                    )
                    putString("steps", (today?.steps ?: -1L).toString())
                    putString("goal_id", goal?.id.orEmpty())
                    putString("goal_metric", goal?.metric?.name.orEmpty())
                    putString("goal_target", (goal?.targetValue ?: -1L).toString())
                }
            }
        }.getOrElse { error ->
            Bundle().apply { putString("probe_error", error.javaClass.simpleName) }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        const val METHOD_STATE = "state"
    }
}
