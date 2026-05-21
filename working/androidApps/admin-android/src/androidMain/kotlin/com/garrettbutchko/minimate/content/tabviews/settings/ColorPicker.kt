package com.garrettbutchko.minimate.content.tabviews.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ColorPickerView
 *
 * Full-screen dimmed overlay with a popup card containing a 4-column color grid.
 * Mirrors iOS ColorPickerView (.ultraThinMaterial card + LazyVGrid).
 *
 * @param visible       whether the overlay is showing
 * @param disabledColors colors that should be shown locked (already in use as course colors)
 * @param onColorSelected called with the chosen Color
 * @param onDismiss     called when the user taps Cancel or taps the backdrop
 */
@Composable
fun ColorPickerView(
    visible: Boolean,
    disabledColors: Set<Color> = emptySet(),
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "colorPickerAlpha"
    )

    // Full-screen backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f * alpha))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Popup card — absorbs click events so backdrop tap only dismisses on the dim area
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(38.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Pick a Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal
                )

                // 4-column color grid
                val colors = listOf(
                    Color.Red,
                    Color(0xFFFF9800), // orange
                    Color.Yellow,
                    Color.Green,
                    Color(0xFF007AFF), // blue
                    Color(0xFF5856D6), // indigo
                    Color(0xFFAF52DE), // purple
                    Color(0xFFFF2D55), // pink
                    Color(0xFF8B4513), // brown
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(colors) { color ->
                        val isDisabled = disabledColors.contains(color)
                        ColorSwatch(
                            color = color,
                            disabled = isDisabled,
                            onClick = {
                                if (!isDisabled) {
                                    onColorSelected(color)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    disabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            .clickable(enabled = !disabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (disabled) 0.2f else 1f)),
            contentAlignment = Alignment.Center
        ) {
            if (disabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Color already in use",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
