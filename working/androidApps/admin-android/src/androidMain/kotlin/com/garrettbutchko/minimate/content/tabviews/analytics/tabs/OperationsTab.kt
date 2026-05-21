package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.BusiestTimesChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.GameDurationTrendChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.GamesPerDayChart
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.viewModels.analytics.GameDurationActivity
import com.garrettbutchko.minimate.viewModels.analytics.HourData
import com.garrettbutchko.minimate.viewModels.analytics.OperationsViewModel
import com.garrettbutchko.minimate.viewModels.analytics.PlayerActivity

@Composable
fun OperationsTab(
    vm: OperationsViewModel,
    modifier: Modifier = Modifier,
) {
    var gamesPerDayData by remember { mutableStateOf<List<PlayerActivity>>(emptyList()) }
    var busiestTimesData by remember { mutableStateOf<List<HourData>>(emptyList()) }
    var durationTrendData by remember { mutableStateOf<List<GameDurationActivity>>(emptyList()) }

    LaunchedEffect(vm) {
        gamesPerDayData = vm.getDataForGamesPerDay()
        busiestTimesData = vm.prepareChartData()
        durationTrendData = vm.getDataForDurationTrend()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION: Games vs Days
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
                    text = "Games vs Days",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                DataCard(
                    data = vm.avgGamesPerDayPrime(),
                    title = "Avg Games Per Day",
                    color = MaterialTheme.colorScheme.surface,
                    cornerRadius = 17.dp
                )

                GamesPerDayChart(data = gamesPerDayData)
            }
        }

        // Standalone Avg Players Per Game card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            DataCard(
                data = vm.avgPlayersPerGamePrime(),
                title = "Avg Players Per Game",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                cornerRadius = 25.dp
            )
        }

        // SECTION: Busiest Times
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
                    text = "Busiest Times",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getBusiestHour(),
                        title = "Busiest Hour",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getBusiestDay(),
                        title = "Busiest Day",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }

                BusiestTimesChart(data = busiestTimesData)
            }
        }

        // SECTION: Game Duration & Engagement
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
                    text = "Game Duration",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getAvgGameDuration(),
                        title = "Avg Duration",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getTotalPlayTime(),
                        title = "Total Time",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.getFastestGameTime(),
                        title = "Fastest Game",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                    DataCard(
                        data = vm.getSlowestGameTime(),
                        title = "Longest Game",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 17.dp
                    )
                }

                GameDurationTrendChart(data = durationTrendData)
            }
        }
    }
}
