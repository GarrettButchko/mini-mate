package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun PhotoIconView(
    photoURL: String?,
    name: String,
    ballColor: Color?,
    imageSize: Dp,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(imageSize + 10.dp)
                .background(color = backgroundColor, shape = CircleShape)
        ) {
            SubcomposeAsyncImage(
                model = photoURL,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error || photoURL.isNullOrBlank()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(CircleShape)
                            .background(colorResource(id = com.garrettbutchko.minimate.shared.R.color.sub).copy(alpha = 0.1f))
                    ) {
                        Image(
                            painter = painterResource(id = com.garrettbutchko.minimate.shared.R.drawable.logo),
                            contentDescription = name,
                            modifier = Modifier.size(imageSize),
                            colorFilter = ColorFilter.tint(ballColor ?: MaterialTheme.colorScheme.onSurface)
                        )
                    }
                } else {
                    // Loaded image: respect aspect ratio (fit) and keep circular clipping
                    SubcomposeAsyncImageContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        val fontSizeSp = with(LocalDensity.current) { (imageSize.value * 0.3f).dp.toSp() }

        Text(
            text = name,
            fontSize = fontSizeSp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = com.garrettbutchko.minimate.shared.R.color.mainOpp)
        )
    }
}
