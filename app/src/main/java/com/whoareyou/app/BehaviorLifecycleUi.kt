package com.whoareyou.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Opportunistically refreshes enabled behavioral sources when the app starts or resumes.
 * The coordinator remains responsible for skipping disabled sources.
 */
@Composable
fun BehaviorRefreshOnResume(
    activity: ComponentActivity?,
    onRefresh: () -> Unit
) {
    val currentRefresh by rememberUpdatedState(onRefresh)

    DisposableEffect(activity) {
        val owner = activity
        if (owner == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    currentRefresh()
                }
            }
            owner.lifecycle.addObserver(observer)
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                currentRefresh()
            }
            onDispose {
                owner.lifecycle.removeObserver(observer)
            }
        }
    }
}
