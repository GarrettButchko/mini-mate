package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HoleDifficultyChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HoleDifficultyParChart
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.viewModels.analytics.ExperienceViewModel
import com.garrettbutchko.minimate.viewModels.analytics.HoleDifficultyData
import com.garrettbutchko.minimate.viewModels.analytics.HoleHeatmapData

@Composable
fun ExperienceTab(
    vm: ExperienceViewModel,
    course: Course,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: Hole Difficulty
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hole Difficulty",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    // InfoButton omitted — no Android equivalent yet
                }

                HoleDifficultyCharts(vm = vm)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getEasiestHole(),
                        title = "Easiest",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getHardestHole(),
                        title = "Hardest",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }
            }
        }

        // SECTION: Par Performance
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Par Performance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getAvgRelativeToPar(),
                        title = "Avg vs Par",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getMostBeatenPar(),
                        title = "Best Hole",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }

                HoleDifficultyParCharts(vm = vm, course = course)
            }
        }

        // SECTION 2: Score Distribution
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Score Distribution",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getUnderParPercentage(),
                        title = "Under Par",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getOverParPercentage(),
                        title = "Over Par",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }

                DataCard(
                    data = vm.getHoleInOneCount(),
                    title = "Total Holes-in-One",
                    color = MaterialTheme.colorScheme.surface,
                    cornerRadius = 17.dp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HoleDifficultyCharts — async data loader around HoleDifficultyChart
// Mirrors SwiftUI's HoleDifficultyCharts struct (loads data via task)
// ---------------------------------------------------------------------------
@Composable
private fun HoleDifficultyCharts(vm: ExperienceViewModel) {
    var difficultyData by remember { mutableStateOf<List<HoleDifficultyData>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            difficultyData = vm.getHoleDifficultyData()
        } catch (_: Exception) {
            // Silently fail — empty state will render
        }
    }

    if (difficultyData.isNotEmpty()) {
        HoleDifficultyChart(difficultyData = difficultyData)
    } else {
        ExperienceEmptyState(message = "Wait for a few more holes to be submitted on this course to unlock hole difficulty stats.")
    }
}

// ---------------------------------------------------------------------------
// HoleDifficultyParCharts — async data loader around HoleDifficultyParChart
// Mirrors SwiftUI's HoleDifficultyParCharts struct
// ---------------------------------------------------------------------------
@Composable
private fun HoleDifficultyParCharts(vm: ExperienceViewModel, course: Course) {
    var heatmapData by remember { mutableStateOf<List<HoleHeatmapData>>(emptyList()) }

    LaunchedEffect(course.id) {
        try {
            heatmapData = vm.getHoleHeatmapForParData(course)
        } catch (_: Exception) {
            // Silently fail — empty state will render
        }
    }

    if (heatmapData.isNotEmpty()) {
        HoleDifficultyParChart(
            difficultyData = heatmapData,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        ExperienceEmptyState(message = "Wait for a few more holes to be submitted on this course to unlock par performance stats.")
    }
}

@Composable
private fun ExperienceEmptyState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Not enough data yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
