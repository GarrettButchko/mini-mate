package com.garrettbutchko.minimate.content.tabviews.analytics.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.tabviews.analytics.ChartEmptyState
import com.garrettbutchko.minimate.viewModels.analytics.PlayerActivity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
fun PlayerTrendChart(
    data: List<PlayerActivity>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    if (data.isEmpty() || data.all { it.count == 0 }) {
        ChartEmptyState(
            message = "Player trend data will appear here once players start visiting your course.",
            icon = Icons.AutoMirrored.Filled.TrendingUp
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            PlayerTrendContent(data = data, lineColor = lineColor, drawProgress = drawProgress.value)
        }
    }
}

@Composable
private fun PlayerTrendContent(data: List<PlayerActivity>, lineColor: Color, drawProgress: Float = 1f) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        val yLabelWidth = 28.dp.toPx()
        val xLabelHeight = 18.dp.toPx()
        val topPad = 8.dp.toPx()

        val chartWidth = size.width - yLabelWidth
        val chartHeight = size.height - xLabelHeight - topPad

        val maxY = data.maxOf { it.count }.toFloat().coerceAtLeast(1f)
        val yStepCount = 5

        val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)

        for (i in 0..yStepCount) {
            val value = (maxY * i / yStepCount).toInt()
            val y = topPad + chartHeight - (i.toFloat() / yStepCount) * chartHeight

            drawLine(
                color = gridColor,
                start = Offset(yLabelWidth, y),
                end = Offset(yLabelWidth + chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )

            val measured = textMeasurer.measure(value.toString(), labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = yLabelWidth - measured.size.width - 4.dp.toPx(),
                    y = y - measured.size.height / 2f
                )
            )
        }

        val points = data.mapIndexed { index, item ->
            val x = if (data.size == 1) yLabelWidth + chartWidth / 2f
            else yLabelWidth + index * chartWidth / (data.size - 1).toFloat()
            val y = topPad + chartHeight - (item.count / maxY) * chartHeight
            Offset(x, y)
        }

        val areaPath = Path().apply {
            moveTo(points.first().x, topPad + chartHeight)
            lineTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
            lineTo(points.last().x, topPad + chartHeight)
            close()
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }

        // Clip area and line to a left-expanding rect driven by drawProgress (0→1).
        // This produces a left-to-right reveal that mirrors how iOS Charts animate in.
        val revealRight = yLabelWidth + chartWidth * drawProgress
        clipRect(left = 0f, top = 0f, right = revealRight, bottom = size.height) {
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.4f), Color.Transparent),
                    startY = topPad,
                    endY = topPad + chartHeight
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        val xLabelIndices = getPlayerTrendXLabelIndices(data.size)
        for (idx in xLabelIndices) {
            val item = data[idx]
            val label = formatMonthDayLabel(item.date.date.month.number, item.date.date.day)
            val x = if (data.size == 1) yLabelWidth + chartWidth / 2f
            else yLabelWidth + idx * chartWidth / (data.size - 1).toFloat()
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (x - measured.size.width / 2f).coerceIn(
                        yLabelWidth,
                        yLabelWidth + chartWidth - measured.size.width
                    ),
                    y = size.height - xLabelHeight + 2.dp.toPx()
                )
            )
        }
    }
}

private fun getPlayerTrendXLabelIndices(count: Int): List<Int> {
    if (count <= 5) return (0 until count).toList()
    val step = (count - 1) / 4.0
    return listOf(
        step.toInt(),
        (step * 2).toInt(),
        (step * 3).toInt(),
        (step * 4).toInt()
    ).distinct()
}

private fun formatMonthDayLabel(month: Int, day: Int): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${months.getOrElse(month - 1) { "?" }} $day"
}
