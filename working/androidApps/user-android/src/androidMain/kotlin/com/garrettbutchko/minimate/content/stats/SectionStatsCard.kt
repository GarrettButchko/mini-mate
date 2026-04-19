package com.garrettbutchko.minimate.content.stats

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.shared.R

@Composable
fun SectionStatsCard(
    title: String,
    spacing: Dp,
    makeColor: Color? = null,
    content: @Composable () -> Unit
) {

    val myModifier = if (makeColor != null) {
        Modifier.fillMaxSize()
    } else {
        Modifier .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(25.dp)
            )
    }
        Column(
            modifier = myModifier
                .background(
                    color = Color.subVsColor(makeColor),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.mainOpp)
            )
            content()
        }
}

