package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.user.R

// import androidx.compose.foundation.Image
// import androidx.compose.ui.res.painterResource
// import com.garrettbutchko.minimate.user.R

@Composable
fun LogoDefault(
    modifier: Modifier = Modifier,
    topPadding: Dp = 16.dp
) {
    // A ColorMatrix with 0f saturation is the Android equivalent of SwiftUI's .grayscale(1.0)
    val grayscaleMatrix = ColorMatrix().apply { setToSaturation(0f) }

    Image(
        painter = painterResource(id = com.garrettbutchko.minimate.shared.R.drawable.logo_image_opp),
        contentDescription = "Logo",
        colorFilter = ColorFilter.colorMatrix(grayscaleMatrix),
        alpha = 0.5f, // Equivalent to .opacity(0.5)
        modifier = modifier
            .padding(top = topPadding, bottom = 16.dp)
            .size(50.dp) // Equivalent to .frame(width: 50, height: 50)
    )

}
