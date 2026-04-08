package com.garrettbutchko.minimate.content.helper

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ScannerCornerShape(
    private val lengthDp: Dp = 40.dp,
    private val radiusDp: Dp = 30.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val length = with(density) { lengthDp.toPx() }
        val radius = with(density) { radiusDp.toPx() }

        val path = Path().apply {
            val minX = 0f
            val minY = 0f
            val maxX = size.width
            val maxY = size.height

            // --- Top Left ---
            moveTo(minX, minY + length)
            lineTo(minX, minY + radius)
            arcTo(
                rect = Rect(minX, minY, minX + 2 * radius, minY + 2 * radius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(minX + length, minY)

            // --- Top Right ---
            moveTo(maxX - length, minY)
            lineTo(maxX - radius, minY)
            arcTo(
                rect = Rect(maxX - 2 * radius, minY, maxX, minY + 2 * radius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(maxX, minY + length)

            // --- Bottom Right ---
            moveTo(maxX, maxY - length)
            lineTo(maxX, maxY - radius)
            arcTo(
                rect = Rect(maxX - 2 * radius, maxY - 2 * radius, maxX, maxY),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(maxX - length, maxY)

            // --- Bottom Left ---
            moveTo(minX + length, maxY)
            lineTo(minX + radius, maxY)
            arcTo(
                rect = Rect(minX, maxY - 2 * radius, minX + 2 * radius, maxY),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(minX, maxY - length)
        }

        return Outline.Generic(path)
    }
}
