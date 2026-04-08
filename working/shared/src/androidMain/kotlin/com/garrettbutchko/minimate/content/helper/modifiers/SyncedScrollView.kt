package com.garrettbutchko.minimate.content.helper.modifiers

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

/**
 * Synchronizes the scroll position of multiple [ScrollState]s.
 * This is useful when you have multiple horizontally scrollable rows (like a scorecard)
 * that need to scroll together.
 */
@Composable
fun SyncScrollStates(vararg states: ScrollState) {
    val syncSource = remember { mutableStateOf<ScrollState?>(null) }

    states.forEach { state ->
        // Track which scroll state is currently being driven by the user
        LaunchedEffect(state) {
            snapshotFlow { state.isScrollInProgress }.collect { inProgress ->
                if (inProgress) {
                    syncSource.value = state
                } else if (syncSource.value == state) {
                    syncSource.value = null
                }
            }
        }

        // When the active scroll state changes, apply its offset to the others
        LaunchedEffect(state) {
            snapshotFlow { state.value }.collect { value ->
                if (syncSource.value == state) {
                    states.filter { it != state }.forEach { otherState ->
                        launch {
                            otherState.scrollTo(value)
                        }
                    }
                }
            }
        }
    }
}
