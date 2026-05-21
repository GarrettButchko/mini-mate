package com.garrettbutchko.minimate.content.tabviews.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class VisitorSegment(val name: String, val count: Int, val color: Color)

@Composable
fun VisitorDonutChart(
    returningPerc: Double,
    firstTimePerc: Double,
    modifier: Modifier = Modifier
) {
    val returningInt = (returningPerc * 100).toInt()
    val firstTimeInt = (firstTimePerc * 100).toInt()

    val segments = listOf(
        VisitorSegment("Returning", returningInt, Color(0xFFFF8D28)),
        VisitorSegment("First-Time", firstTimeInt, Color(0xFF0088FF))
    )

    val higherSegment = when {
        returningInt > firstTimeInt -> segments[0]
        firstTimeInt > returningInt -> segments[1]
        else -> VisitorSegment("Equal", returningInt, segments[0].color)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Visitor Composition",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutCanvas(segments = segments)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "~${higherSegment.count}%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = higherSegment.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                segments.forEach { segment ->
                    DonutLegendItem(title = segment.name, color = segment.color)
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(segments: List<VisitorSegment>) {
    val total = segments.sumOf { it.count }.toFloat().coerceAtLeast(1f)

    Canvas(modifier = Modifier.size(200.dp)) {
        val strokeWidth = size.width * 0.15f
        val gapAngle = 4f
        var startAngle = -90f

        for (segment in segments) {
            val sweepAngle = (segment.count / total) * 360f - gapAngle
            drawArc(
                color = segment.color,
                startAngle = startAngle + gapAngle / 2f,
                sweepAngle = sweepAngle.coerceAtLeast(0f),
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle + gapAngle
        }
    }
}

@Composable
private fun DonutLegendItem(title: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}