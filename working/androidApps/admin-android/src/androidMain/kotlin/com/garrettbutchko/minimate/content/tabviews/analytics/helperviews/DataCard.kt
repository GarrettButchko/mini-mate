package com.garrettbutchko.minimate.content.tabviews.analytics.helperviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.viewModels.analytics.DataPointObject

// Parses named colors ("red", "blue", etc.) or "#RRGGBB"/"#AARRGGBB" hex strings into a Compose Color.
fun String.toComposeColor(): Color? {
    this.toColor()?.let { return it }
    val hex = this.trimStart('#')
    return when (hex.length) {
        6 -> hex.toLongOrNull(16)?.let { Color(it.or(0xFF000000L).toInt()) }
        8 -> hex.toLongOrNull(16)?.let { Color(it.toInt()) }
        else -> null
    }
}

@Composable
fun DataCard(
    data: DataPointObject,
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    cornerRadius: Dp = 12.dp,
    fontStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = color
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = fontStyle,
                    color = onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // InfoButton omitted — placeholder until Android InfoButton is implemented
                Spacer(modifier = Modifier.padding(start = 8.dp))
            }

            // Value + delta row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val valueColor = data.deltaColor?.toComposeColor() ?: onSurface
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (data.delta != null && data.deltaColor != null) {
                    val deltaColor = data.deltaColor!!.toComposeColor() ?: onSurface
                    Text(
                        text = data.delta!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = deltaColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
