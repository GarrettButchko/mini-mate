package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.PlayerTrendChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.VisitorDonutChart
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.viewModels.analytics.ChartTopic
import com.garrettbutchko.minimate.viewModels.analytics.GrowthViewModel
import com.garrettbutchko.minimate.viewModels.analytics.PlayerActivity

@Composable
fun GrowthTab(
    vm: GrowthViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTopic by remember { mutableStateOf(value = vm.growthChartTopic) }
    var trendData by remember { mutableStateOf<List<PlayerActivity>>(value = emptyList()) }

    LaunchedEffect(selectedTopic) {
        vm.growthChartTopic = selectedTopic
        trendData = try { vm.getDataForGrowthTrend() } catch (_: Exception) { emptyList() }
    }

    val topicColor = when (selectedTopic) {
        ChartTopic.FIRST -> Color(0xFF34C759)
        ChartTopic.RETURNING -> Color(0xFF007AFF)
        ChartTopic.TOTAL -> Color(0xFF8E8E93)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // SECTION: Player Data
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Player Data",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                DataCard(
                    data = vm.activeUsersPrime(),
                    title = "Total Visits",
                    color = MaterialTheme.colorScheme.surface,
                    cornerRadius = 16.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataCard(
                        data = vm.firstTimePrime(),
                        title = "First Time",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 16.dp
                    )
                    DataCard(
                        data = vm.returningPrime(),
                        title = "Returning",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        cornerRadius = 16.dp
                    )
                }

                // Donut or empty state
                if ((vm.returningPercOfTotal() == 0.0) && (vm.firstTimePercOfTotal() == 0.0)) {
                    GrowthEmptyStateCard(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        message = "Not enough data yet",
                        subMessage = "Visit data will appear here once players start coming to your course."
                    )
                } else {
                    VisitorDonutChart(
                        returningPerc = vm.returningPercOfTotal(),
                        firstTimePerc = vm.firstTimePercOfTotal()
                    )
                }
            }
        }

        // SECTION: Trend
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Trend",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // "Days vs:" label + topic dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Days vs:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    ChartTopicDropdown(
                        selected = selectedTopic,
                    ) { selectedTopic = it }
                }

                if (trendData.isEmpty()) {
                    GrowthEmptyStateCard(
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant ) },
                        message = "Not enough data yet",
                        subMessage = "Player trend data will appear here once players start visiting your course."
                    )
                } else {
                    PlayerTrendChart(data = trendData, lineColor = topicColor)
                }
            }
        }
    }
}

@Composable
private fun ChartTopicDropdown(
    selected: ChartTopic,
    onSelect: (ChartTopic) -> Unit,
) {
    var expanded by remember { mutableStateOf(value = false) }

    val dotColor = when (selected) {
        ChartTopic.FIRST -> Color(0xFF34C759)
        ChartTopic.RETURNING -> Color(0xFF007AFF)
        ChartTopic.TOTAL -> Color(0xFF8E8E93)
    }

    Box {
        Surface(
            shape = RoundedCornerShape(17.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { expanded = true }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(dotColor, shape = RoundedCornerShape(50))
                    )
                    Text(
                        text = selected.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Total") },
                onClick = { onSelect(ChartTopic.TOTAL); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("First-Time") },
                onClick = { onSelect(ChartTopic.FIRST); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Returning") },
                onClick = { onSelect(ChartTopic.RETURNING); expanded = false }
            )
        }
    }
}

@Composable
private fun GrowthEmptyStateCard(
    icon: @Composable () -> Unit,
    message: String,
    subMessage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon()
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
