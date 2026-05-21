package com.garrettbutchko.minimate.content.tabviews.analytics.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.viewModels.analytics.HoleHeatmapData

private fun colorForParDifficulty(relativeToPar: Double): Color = when {
    relativeToPar <= -1.5 -> Color(0xFFBF5AF2).copy(alpha = 0.8f)
    relativeToPar <= -0.5 -> Color(0xFF0088FF).copy(alpha = 0.8f)
    relativeToPar < 0.5  -> Color(0xFF34C759).copy(alpha = 0.8f)
    relativeToPar < 1.5  -> Color(0xFFFF9500).copy(alpha = 0.8f)
    else                 -> Color(0xFFFF3B30).copy(alpha = 0.8f)
}

@Composable
fun HoleDifficultyParChart(
    difficultyData: List<HoleHeatmapData>,
    modifier: Modifier = Modifier
) {
    val maxHole = difficultyData.maxOfOrNull { it.holeNumber } ?: 0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hole Difficulty (vs Par) Map",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Purple (Under Par) | Blue (Little Under Par) | Green (Par) | Orange (Just Over Par) | Red (Over Par)",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            difficultyData.forEach { hole ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(7.dp),
                        color = colorForParDifficulty(hole.relativeToPar)
                    ) {}

                    Text(
                        text = "${hole.holeNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (shouldShowParLabel(hole.holeNumber, maxHole))
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            Color.Transparent,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun shouldShowParLabel(holeNumber: Int, maxHole: Int): Boolean {
    return holeNumber == 1 || holeNumber == maxHole || holeNumber % 3 == 0
}

@Composable
fun HolePreviewList(
    allHoles: List<HoleHeatmapData>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val outliers = remember(allHoles) {
        allHoles.sortedByDescending { Math.abs(it.relativeToPar) }
    }
    val displayList = if (isExpanded) {
        remember(allHoles) { allHoles.sortedBy { it.holeNumber } }
    } else {
        outliers.take(3)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        displayList.forEachIndexed { index, hole ->
            HoleStatusRow(data = hole)
            if (index < displayList.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            }
        }

        TextButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = if (isExpanded) "Show Less" else "View All Hole Data",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
fun HoleStatusRow(
    data: HoleHeatmapData,
    modifier: Modifier = Modifier
) {
    val diffColor = colorForParDifficulty(data.relativeToPar)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = diffColor.copy(alpha = 0.15f)
            ) {}
            Text(
                text = "${data.holeNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = diffColor
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Hole ${data.holeNumber}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Par ${data.holePar}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.End) {
            val relStr = if (data.relativeToPar >= 0)
                "+${String.format("%.1f", data.relativeToPar)}"
            else
                String.format("%.1f", data.relativeToPar)

            Text(
                text = relStr,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = diffColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "vs Par",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
