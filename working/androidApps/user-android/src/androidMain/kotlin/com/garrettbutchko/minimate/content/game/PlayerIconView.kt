package com.garrettbutchko.minimate.content.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor

@Composable
fun HostPlayerIconView(
    player: Player,
    isRemovable: Boolean,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    imageSize: Dp = 30.dp
) {
    val iconModifier = if (isRemovable) {
        modifier
            .padding(horizontal = 16.dp)
            .clickable { onTap?.invoke() }
    } else {
        modifier.padding(horizontal = 16.dp)
    }

    PhotoIconView(
        photoURL = player.photoURL,
        name = player.name,
        ballColor = player.ballColor,
        imageSize = imageSize,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = iconModifier
    )
}
