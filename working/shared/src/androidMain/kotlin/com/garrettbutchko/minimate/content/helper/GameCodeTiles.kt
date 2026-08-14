package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Number of characters in a Mini Mate game code. */
const val GAME_CODE_LENGTH = 6

/**
 * One rounded-square character tile, passcode style. Shared by the host's
 * "Show Game Code" popup and the join screen's code entry so a code looks the
 * same wherever it appears.
 */
@Composable
fun GameCodeTile(
    char: Char?,
    modifier: Modifier = Modifier,
    placeholder: Char = '0',
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .width(38.dp)
            .height(52.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
            .border(if (isActive) 2.dp else 1.dp, borderColor, shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (char ?: placeholder).toString(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (char != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )
    }
}

/** Read-only row of [GameCodeTile]s spelling out [code]. */
@Composable
fun GameCodeTiles(
    code: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        code.forEach { char ->
            GameCodeTile(char = char)
        }
    }
}
