package com.garrettbutchko.minimate.content.stats

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.BaseStatCard
import com.garrettbutchko.minimate.content.helper.modifiers.subTwoVsColor

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color? = null,
    makeColor: Color? = null,
    cornerRadius: Dp = 12.dp,
    infoText: String = "No Text Yet"
) {
    val valueStyle = if (color != null) {
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = color
        )
    } else {
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            brush = Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF34C759))
            )
        )
    }

    BaseStatCard(
        modifier = modifier,
        title = title,
        infoText = infoText,
        cornerRadius = cornerRadius,
        backgroundColor = Color.subTwoVsColor(makeColor)
    ) {
        Text(text = value, style = valueStyle)
    }
}
