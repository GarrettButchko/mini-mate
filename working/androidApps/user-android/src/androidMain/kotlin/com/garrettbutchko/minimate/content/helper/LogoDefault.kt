package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.shared.R


@Composable
fun LogoDefault(
    modifier: Modifier = Modifier,
    topPadding: Dp = 16.dp
) {
    Image(
        painter = painterResource(id = R.drawable.logo_image_opp),
        contentDescription = "Logo",
        colorFilter = ColorFilter.tint(Color.Gray),
        modifier = modifier
            .padding(top = topPadding, bottom = 16.dp)
            .size(50.dp)
    )
}
