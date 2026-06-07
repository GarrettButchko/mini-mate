package com.garrettbutchko.minimate.content.tabviews.analytics.helperviews

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.BaseStatCard
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.viewModels.analytics.DataPointObject

@Composable
fun DataCard(
    modifier: Modifier = Modifier,
    data: DataPointObject,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    cornerRadius: Dp = 12.dp,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    BaseStatCard(
        modifier = modifier,
        title = data.title,
        infoText = data.infoText,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor
    ) {
        val valueColor = data.deltaColor?.toColor() ?: onSurface
        Text(
            text = data.value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (data.delta != null && data.deltaColor != null) {
            val deltaColor = data.deltaColor!!.toColor() ?: onSurface
            Text(
                text = data.delta!!,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = deltaColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
