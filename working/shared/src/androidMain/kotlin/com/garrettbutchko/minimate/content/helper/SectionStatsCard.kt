package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow

@Composable
fun SectionStatsCard(
    title: String,
    isBigTitle: Boolean = false,
    makeColor: Color? = null,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val subColor = Color.subVsColor(makeColor)

    val myModifier = if (makeColor != null) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxWidth()
            .subtleShadow()
    }
    Column(
        modifier = myModifier
            .background(
                color = subColor,
                shape = RoundedCornerShape(25.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Text(
            text = title,
            style = if (isBigTitle) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

