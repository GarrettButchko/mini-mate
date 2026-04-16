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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Color.Companion.subVsColor(makeColor: Color?): Color {
    return makeColor?.copy(alpha = 0.50f) ?: colorResource(id = com.garrettbutchko.minimate.shared.R.color.sub)
}

@Composable
fun Color.Companion.subTwoVsColor(makeColor: Color?): Color {
    val subTwo = colorResource(id = com.garrettbutchko.minimate.shared.R.color.subTwo)
    return makeColor?.copy(alpha = 0.50f)?.mix(with = subTwo, by = 0.7f) ?: subTwo
}

fun Color.mix(with: Color, by: Float): Color {
    return lerp(this, with, by.coerceIn(0f, 1f))
}

