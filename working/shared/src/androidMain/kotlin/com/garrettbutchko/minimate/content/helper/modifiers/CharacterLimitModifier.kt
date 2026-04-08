package com.garrettbutchko.minimate.content.helper.modifiers

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * A Compose Modifier equivalent to SwiftUI's characterLimit.
 * Restricts a text state to a maximum character count by observing changes.
 *
 * @param text The current text value to monitor.
 * @param maxLength The maximum allowed length.
 * @param onTextChange Callback triggered when the text needs to be truncated.
 */
fun Modifier.characterLimit(
    text: String,
    maxLength: Int,
    onTextChange: (String) -> Unit
): Modifier = composed {
    LaunchedEffect(text) {
        if (text.length > maxLength) {
            onTextChange(text.take(maxLength))
        }
    }
    this
}
