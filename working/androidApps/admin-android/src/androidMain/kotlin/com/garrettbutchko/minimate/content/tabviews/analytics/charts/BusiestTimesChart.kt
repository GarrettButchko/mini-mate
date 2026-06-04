package com.garrettbutchko.minimate.content.tabviews.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.viewModels.analytics.HourData

private val xAxisHours = listOf(0, 3, 7, 11, 15, 19, 23)
private val yAxisDays = listOf(1 to "Sun", 3 to "Tues", 5 to "Thurs", 7 to "Sat")

@Composable
fun BusiestTimesChart(
    data: List<HourData>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {
        if (data.all { it.count == 0 }) {
            BusiestTimesEmptyState()
        } else {
            BusiestTimesHeatmap(data = data)
        }
    }
}

@Composable
private fun BusiestTimesEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Not enough data yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "Busiest times data will appear here once players start visiting your course.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun BusiestTimesHeatmap(data: List<HourData>) {
    val maxCount = data.maxOf { it.count }.toFloat().coerceAtLeast(1f)
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val yLabelWidth = 36.dp.toPx()
        val xLabelHeight = 20.dp.toPx()
        val topPad = 4.dp.toPx()

        val chartLeft = yLabelWidth
        val chartTop = topPad
        val chartWidth = size.width - chartLeft
        val chartHeight = size.height - xLabelHeight - topPad

        val colWidth = chartWidth / 24f
        val rowHeight = chartHeight / 7f
        val cellPad = 0.15f

        for (item in data) {
            val intensity = (item.count.toFloat() / maxCount).coerceIn(0f, 1f)
            val alpha = 0.16f + intensity * 0.84f
            val x = chartLeft + item.hour * colWidth + colWidth * cellPad / 2f
            val y = chartTop + (item.weekday - 1) * rowHeight + rowHeight * cellPad / 2f
            drawRoundRect(
                color = Color(0xFF34C759).copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = Size(colWidth * (1f - cellPad), rowHeight * (1f - cellPad)),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }

        val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)

        for ((day, label) in yAxisDays) {
            val cy = chartTop + (day - 1) * rowHeight + rowHeight / 2f
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = yLabelWidth - measured.size.width - 4.dp.toPx(),
                    y = cy - measured.size.height / 2f
                )
            )
        }

        for (hour in xAxisHours) {
            val cx = chartLeft + hour * colWidth + colWidth / 2f
            val measured = textMeasurer.measure(formatHour(hour), labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = cx - measured.size.width / 2f,
                    y = size.height - xLabelHeight + 2.dp.toPx()
                )
            )
        }
    }
}

private fun formatHour(h: Int): String = when {
    h == 23 -> "12am"
    h == 0 -> "1am"
    h == 11 -> "12pm"
    h > 12 -> "${h - 12 + 1}pm"
    else -> "${h + 1}am"
}
