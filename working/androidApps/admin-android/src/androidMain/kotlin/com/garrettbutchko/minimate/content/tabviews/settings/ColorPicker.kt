package com.garrettbutchko.minimate.content.tabviews.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.AppAlertDialog

private val pickerColors = listOf(
    Color(0xFFFF383C), // red
    Color(0xFFFF8D28), // orange
    Color(0xFFFFCC00), // yellow
    Color(0xFF34C759), // green
    Color(0xFF0088FF), // blue
    Color(0xFF6155F5), // indigo
    Color(0xFFCB30E0), // purple
    Color(0xFFFF2D55), // pink
    Color(0xFFAC7F5E), // brown
)

@Composable
fun ColorPickerView(
    visible: Boolean,
    notScoreCard: Boolean,
    disabledColors: Set<Color> = emptySet(),
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = "Pick a Color",
        dismissText = "Cancel",
        onDismiss = onDismiss,
        content = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pickerColors) { color ->
                    val isDisabled = disabledColors.contains(color)
                    ColorSwatch(
                        color = color,
                        disabled = isDisabled && notScoreCard,
                        onClick = {
                            if (!(isDisabled && notScoreCard)) {
                                onColorSelected(color)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun ColorSwatch(
    color: Color,
    disabled: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (disabled) 0.25f else 1f))
            .clickable(enabled = !disabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (disabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Color already in use",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
    }
}
