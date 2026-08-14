package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaitingIndicatorView(
    label: String,
    // This app's onSurfaceVariant maps to subOpp (#1C1C1F) — still near-black, not
    // actually gray. The codebase's real "secondary" convention (matching iOS's
    // .secondary) is a translucent overlay of onSurface.
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    size: Dp = 40.dp
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color
        )
        Spacer(modifier = Modifier.height(0.5.dp))
        Text(
            text = label,
            style = LocalTextStyle.current.copy(
                fontSize = with(LocalDensity.current) { 9.dp.toSp() },
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}