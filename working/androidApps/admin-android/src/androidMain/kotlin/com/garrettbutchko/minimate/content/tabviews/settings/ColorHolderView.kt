package com.garrettbutchko.minimate.content.tabviews.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * ColorHolderView
 *
 * A 40×40 dp circular button that shows either:
 * - An existing color (tap to delete it)
 * - A "+" add button (tap to open the color picker)
 *
 * Mirrors iOS ColorHolderView exactly.
 *
 * @param color         optional — if non-null the color swatch is shown; null shows the "+" add state
 * @param showFunction  called when the "+" circle is tapped (opens ColorPickerView)
 * @param deleteFunction called when the color swatch is tapped (requests deletion)
 */
@Composable
fun ColorHolderView(
    color: Color? = null,
    showFunction: () -> Unit = {},
    deleteFunction: () -> Unit = {}
) {
    if (color != null) {
        // Color swatch — tapping deletes it
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { deleteFunction() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    } else {
        // Add button — tapping shows the color picker
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showFunction() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add color",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
