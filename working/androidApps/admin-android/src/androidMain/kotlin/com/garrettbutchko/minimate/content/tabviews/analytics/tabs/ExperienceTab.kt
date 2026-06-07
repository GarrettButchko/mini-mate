package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.SectionCard
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.tabviews.analytics.ChartEmptyState
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HoleDifficultyChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HoleDifficultyParChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HoleHardnessPreviewList
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HolePreviewList
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.analytics.HoleDifficultyData
import com.garrettbutchko.minimate.viewModels.analytics.HoleHeatmapData

@Composable
internal fun ExperienceTab(vm: AnalyticsViewModel, course: Course?) {
    val expVM = vm.experienceVM
    val loadingDocs by vm.loadingDocs.collectAsState()

    val difficultyData = if (!loadingDocs) expVM.getHoleDifficultyData() else emptyList()
    val heatmapData = if (!loadingDocs && course != null) expVM.getHoleHeatmapForParData(course) else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        SectionStatsCard(title = "Hole Difficulty", isBigTitle = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                if (difficultyData.isEmpty()) {
                    ChartEmptyState(
                        message = "Wait for a few more holes to be submitted to unlock hole difficulty stats.",
                        icon = Icons.Default.Star
                    )
                } else {
                    HoleDifficultyChart(difficultyData = difficultyData)
                    HoleHardnessPreviewList(difficultyData = difficultyData)
                }
            }


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = expVM.getEasiestHole(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = expVM.getHardestHole(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionStatsCard(title = "Par Performance", isBigTitle = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = expVM.getAvgRelativeToPar(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = expVM.getMostBeatenPar(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                if (heatmapData.isEmpty()) {
                    ChartEmptyState(
                        message = "Wait for a few more holes to be submitted to unlock par performance stats.",
                        icon = Icons.Default.Star
                    )
                } else {
                    HoleDifficultyParChart(difficultyData = heatmapData)
                    HolePreviewList(allHoles = heatmapData)
                }
            }
        }

        SectionStatsCard(title = "Score Distribution", isBigTitle = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = expVM.getUnderParPercentage(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = expVM.getOverParPercentage(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            DataCard(
                data = expVM.getHoleInOneCount(),
                cornerRadius = 17.dp
            )
        }
    }
}
