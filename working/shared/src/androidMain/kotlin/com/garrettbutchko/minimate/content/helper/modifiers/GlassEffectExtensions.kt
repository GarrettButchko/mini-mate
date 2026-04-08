package com.garrettbutchko.minimate.content.helper.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android approximation of the SwiftUI glass helper.
 * Compose has no direct equivalent to iOS glassEffect, so this applies a clipped translucent fill + stroke.
 */
@Composable
fun Modifier.ifAvailableGlassEffect(
    shape: Shape = RoundedCornerShape(0.dp),
    strokeWidth: Dp = 1.dp,
    opacity: Float = 0.30f,
    makeColor: Color? = null
): Modifier {
    val baseColor = makeColor ?: MaterialTheme.colorScheme.surfaceVariant
    val fillColor = baseColor.copy(alpha = opacity.coerceIn(0f, 1f))
    val strokeColor = makeColor ?: MaterialTheme.colorScheme.outlineVariant

    return this
        .clip(shape)
        .background(color = fillColor, shape = shape)
        .border(width = strokeWidth, color = strokeColor, shape = shape)
}

@Composable
fun Modifier.ifAGENoDefaultColor(
    shape: Shape = RoundedCornerShape(0.dp)
): Modifier {
    return this
        .clip(shape)
        .background(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            shape = shape
        )
}

@Composable
fun Modifier.ultraThinMaterialVsColor(makeColor: Color?): Modifier {
    val bgColor = makeColor?.copy(alpha = 0.50f) ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    return this.background(bgColor)
}

@Composable
fun Color.Companion.subVsColor(makeColor: Color?): Color {
    return makeColor?.copy(alpha = 0.50f) ?: MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun Color.Companion.subTwoVsColor(makeColor: Color?): Color {
    val subTwo = MaterialTheme.colorScheme.secondaryContainer
    return makeColor?.copy(alpha = 0.50f)?.mix(with = subTwo, by = 0.7f) ?: subTwo
}

fun Color.mix(with: Color, by: Float): Color {
    return lerp(this, with, by.coerceIn(0f, 1f))
}

@Composable
fun Modifier.ultraThinMaterialVsColorFill(
    shape: Shape,
    makeColor: Color?
): Modifier {
    val fillColor = makeColor?.copy(alpha = 0.50f) ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    return this
        .clip(shape)
        .background(fillColor, shape)
}

