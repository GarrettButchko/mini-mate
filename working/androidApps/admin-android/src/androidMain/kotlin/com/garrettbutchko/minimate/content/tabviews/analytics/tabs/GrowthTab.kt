package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.PieChart
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.tabviews.analytics.ChartEmptyState
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.PlayerTrendChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.VisitorDonutChart
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.analytics.ChartTopic

private val ChartTopic.lineColor: Color
    get() = when (this) {
        ChartTopic.TOTAL     -> Color(0xFF34C759)
        ChartTopic.FIRST     -> Color(0xFF0088FF)
        ChartTopic.RETURNING -> Color(0xFFFF8D28)
    }

@Composable
internal fun GrowthTab(vm: AnalyticsViewModel) {
    val growthVM = vm.growthVM
    val loadingDocs by vm.loadingDocs.collectAsState()
    var chartTopic by remember { mutableStateOf(growthVM.growthChartTopic) }
    var showTopicMenu by remember { mutableStateOf(false) }

    val trendData = if (!loadingDocs) growthVM.getDataForGrowthTrend() else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionStatsCard(title = "Player Data", isBigTitle = true) {
            DataCard(
                data = growthVM.activeUsersPrime(),
                cornerRadius = 16.dp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(
                    data = growthVM.firstTimePrime(),
                    cornerRadius = 16.dp,
                    modifier = Modifier.weight(1f)
                )
                DataCard(
                    data = growthVM.returningPrime(),
                    cornerRadius = 16.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            val returning = growthVM.returningPercOfTotal()
            val firstTime = growthVM.firstTimePercOfTotal()
            if (returning == 0.0 && firstTime == 0.0) {
                ChartEmptyState(
                    message = "Visit data will appear here once players start coming to your course.",
                    icon = Icons.Default.PieChart
                )
            } else {
                VisitorDonutChart(
                    returningPerc = returning,
                    firstTimePerc = firstTime
                )
            }
        }
        SectionStatsCard(title = "Trend", isBigTitle = true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "Days vs:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) { // 1. Added fillMaxWidth here
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth() // 2. Added fillMaxWidth here so the clickable area spans the whole width
                            .clip(RoundedCornerShape(17.dp))
                            .clickable { showTopicMenu = true },
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(chartTopic.lineColor)
                            )
                            // 3. Added weight(1f) so the text pushes the dropdown arrow to the far right
                            Text(
                                text = chartTopic.title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showTopicMenu,
                        onDismissRequest = { showTopicMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Total") },
                            onClick = {
                                chartTopic = ChartTopic.TOTAL
                                growthVM.growthChartTopic = ChartTopic.TOTAL
                                showTopicMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("First-Time") },
                            onClick = {
                                chartTopic = ChartTopic.FIRST
                                growthVM.growthChartTopic = ChartTopic.FIRST
                                showTopicMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Returning") },
                            onClick = {
                                chartTopic = ChartTopic.RETURNING
                                growthVM.growthChartTopic = ChartTopic.RETURNING
                                showTopicMenu = false
                            }
                        )
                    }
                }
            }

                if (trendData.isEmpty() || trendData.all { it.count == 0 }) {
                    ChartEmptyState(
                        message = "Player trend data will appear here once players start visiting your course.",
                        icon = Icons.AutoMirrored.Filled.TrendingUp
                    )
                } else {
                    key(chartTopic) {
                        PlayerTrendChart(
                            data = trendData,
                            lineColor = chartTopic.lineColor
                        )
                    }
                }

        }
    }
}
