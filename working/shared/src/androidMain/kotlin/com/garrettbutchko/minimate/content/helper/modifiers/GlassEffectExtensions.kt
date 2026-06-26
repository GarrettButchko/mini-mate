package com.garrettbutchko.minimate.content.helper.modifiers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Color.Companion.subVsColor(makeColor: Color?): Color {
    return makeColor?.copy(alpha = 0.20f) ?: colorResource(id = com.garrettbutchko.minimate.shared.R.color.sub)
}

@Composable
fun Color.Companion.subTwoVsColor(makeColor: Color?): Color {
    val subTwo = colorResource(id = com.garrettbutchko.minimate.shared.R.color.subTwo)
    return makeColor?.copy(alpha = 0.2f)?.mix(with = subTwo, by = 0.95f) ?: subTwo
}

fun Color.mix(with: Color, by: Float): Color {
    return lerp(this, with, by.coerceIn(0f, 1f))
}

fun Modifier.colorCardLayout(makeColor: Color?, typeFill: Modifier = Modifier.fillMaxWidth()): Modifier =
    if (makeColor != null) this.then(typeFill)
    else this.then(typeFill).subtleShadow()

fun Modifier.subtleShadow(
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(25.dp),
    alpha: Float = 0.25f
): Modifier = this.graphicsLayer {
    this.shadowElevation = elevation.toPx()
    this.shape = shape
    this.clip = false
    this.spotShadowColor = Color.Black.copy(alpha = alpha)
    this.ambientShadowColor = Color.Black.copy(alpha = alpha)
}

