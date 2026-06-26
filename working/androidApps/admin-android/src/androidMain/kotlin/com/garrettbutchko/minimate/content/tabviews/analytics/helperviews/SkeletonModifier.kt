package com.garrettbutchko.minimate.content.tabviews.analytics.helperviews

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Shimmer skeleton modifier — equivalent to SwiftUI's SkeletonModifier.
 *
 * Usage: Modifier.skeleton(active = isLoading)
 *
 * The SwiftUI version used .redacted(reason: .placeholder) + a moving white shimmer overlay.
 * On Android, we replicate this with a drawWithContent sweep gradient that scrolls
 * horizontally across the composable.
 */
fun Modifier.skeleton(active: Boolean): Modifier = if (!active) this else composed {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonPhase"
    )

    drawWithContent {
        drawContent()

        // Sweep a translucent highlight stripe from left to right
        val sweepWidth = size.width * 0.4f
        val startX = size.width * phase * 2f - sweepWidth

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + sweepWidth, 0f)
            )
        )
    }
}
