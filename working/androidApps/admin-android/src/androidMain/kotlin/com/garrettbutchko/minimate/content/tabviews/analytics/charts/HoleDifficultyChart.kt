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
import androidx.compose.ui.text.font.FontWeight
import com.garrettbutchko.minimate.content.JetBrainsMono
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.viewModels.analytics.HoleDifficultyData

private val holeGreen = Color(0xFF34C759)

@Composable
fun HoleDifficultyChart(
    difficultyData: List<HoleDifficultyData>,
    modifier: Modifier = Modifier
) {
    val allStrokes = difficultyData.map { it.averageStrokes }
    val minStrokes = allStrokes.minOrNull() ?: 0.0
    val maxStrokes = allStrokes.maxOrNull() ?: 0.0
    val maxHole = difficultyData.maxOfOrNull { it.holeNumber } ?: 0

    Row(
        modifier = modifier.fillMaxWidth(),
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
                    color = colorForDifficulty(hole.averageStrokes, minStrokes, maxStrokes)
                ) {}

                Text(
                    text = "${hole.holeNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (shouldShowLabel(hole.holeNumber, maxHole))
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

private fun colorForDifficulty(strokes: Double, minStrokes: Double, maxStrokes: Double): Color {
    if (maxStrokes <= minStrokes) return holeGreen.copy(alpha = 0.5f)
    val normalized = (strokes - minStrokes) / (maxStrokes - minStrokes)
    val alpha = (0.16f + normalized * 0.84f).toFloat()
    return holeGreen.copy(alpha = alpha)
}

private fun shouldShowLabel(holeNumber: Int, maxHole: Int): Boolean {
    if (holeNumber == 1 || holeNumber == maxHole) return true
    return holeNumber % 3 == 0
}

@Composable
fun HoleHardnessRow(
    hole: HoleDifficultyData,
    minStrokes: Double,
    maxStrokes: Double,
    modifier: Modifier = Modifier
) {
    val hardnessColor = colorForHardness(hole.averageStrokes, minStrokes, maxStrokes)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = hardnessColor.copy(alpha = 0.1f)
            ) {}
            Text(
                text = "${hole.holeNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = hardnessColor
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Hole ${hole.holeNumber}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%.2f", hole.averageStrokes),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 14.sp)
            )
            Text(
                text = "avg strokes",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
            )
        }
    }
}

private fun colorForHardness(strokes: Double, minStrokes: Double, maxStrokes: Double): Color {
    if (maxStrokes <= minStrokes) return holeGreen
    val normalized = (strokes - minStrokes) / (maxStrokes - minStrokes)
    val alpha = (0.3f + normalized * 0.7f).toFloat()
    return holeGreen.copy(alpha = alpha)
}

@Composable
fun HoleHardnessPreviewList(
    difficultyData: List<HoleDifficultyData>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val minStrokes = difficultyData.minOfOrNull { it.averageStrokes } ?: 0.0
    val maxStrokes = difficultyData.maxOfOrNull { it.averageStrokes } ?: 1.0

    val hardestHoles = remember(difficultyData) {
        difficultyData.sortedByDescending { it.averageStrokes }
    }
    val displayList = if (isExpanded) difficultyData else hardestHoles.take(3)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        displayList.forEachIndexed { index, hole ->
            HoleHardnessRow(hole = hole, minStrokes = minStrokes, maxStrokes = maxStrokes)
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
