package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.BusiestTimesChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.GameDurationTrendChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.GamesPerDayChart
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow

@Composable
internal fun OperationsTab(vm: AnalyticsViewModel) {
    val opsVM = vm.operationsVM
    val loadingDocs by vm.loadingDocs.collectAsState()

    val heatmapData = if (!loadingDocs) opsVM.prepareChartData() else emptyList()
    val gamesPerDayData = if (!loadingDocs) opsVM.getDataForGamesPerDay() else emptyList()
    val durationData = if (!loadingDocs) opsVM.getDataForDurationTrend() else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionStatsCard(title = "Games vs Days", isBigTitle = true) {
            // if need big title later
            //Text(
             //   text = "Games vs Days",
             //   style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            //)
            DataCard(
                data = opsVM.avgGamesPerDayPrime(),
                cornerRadius = 17.dp
            )
            GamesPerDayChart(data = gamesPerDayData)
        }


        DataCard(
            modifier = Modifier.subtleShadow(),
            data = opsVM.avgPlayersPerGamePrime(),
            cornerRadius = 25.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )


        SectionStatsCard(title = "Busiest Times", isBigTitle = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = opsVM.getBusiestHour(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = opsVM.getBusiestDay(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            BusiestTimesChart(data = heatmapData)
        }

        SectionStatsCard(title = "Game Duration", isBigTitle = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = opsVM.getAvgGameDuration(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = opsVM.getTotalPlayTime(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = opsVM.getFastestGameTime(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = opsVM.getSlowestGameTime(),
                    cornerRadius = 17.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            GameDurationTrendChart(data = durationData)
        }
    }
}
