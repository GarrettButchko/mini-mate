package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.modifiers.colorCardLayout
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor

@Composable
fun SectionStatsCard(
    title: String,
    subtitle: String? = null,
    isBigTitle: Boolean = true,
    makeColor: Color? = null,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val subColor = Color.subVsColor(makeColor)

    Column(
        modifier = Modifier
            .colorCardLayout(makeColor)
            .background(
                color = subColor,
                shape = RoundedCornerShape(25.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = if (isBigTitle) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

