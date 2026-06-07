package com.garrettbutchko.minimate.content.helper

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LogoLoadingView(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val grayscaleMatrix = ColorMatrix().also { it.setToSaturation(0f) }

    Image(
        painter = painterResource(id = com.garrettbutchko.minimate.shared.R.drawable.logo_image),
        contentDescription = "Loading",
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.colorMatrix(grayscaleMatrix),
        modifier = modifier
            .size(size)
            .alpha(0.5f)
            .rotate(rotation)
    )
}
