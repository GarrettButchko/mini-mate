package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.dataModels.holeModels.Hole
import com.garrettbutchko.minimate.shared.R
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.bar.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.bar.render.yaxis.IYAxisDrawer
import me.bytebeats.views.charts.bar.render.bar.IBarDrawer
import me.bytebeats.views.charts.toLegacyInt
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun BarChartView(
    data: List<Hole>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val maxStrokes = data.maxOfOrNull { it.strokes }?.toFloat() ?: 5f
    val chartData = BarChartData(
        bars = data.map { hole ->

            var label = ""
            if (hole.number % 3 == 0) {
                label = "H${hole.number}"
            }

            BarChartData.Bar(
                label = label,
                value = hole.strokes.toFloat(),
                color = strokeColor(hole.strokes)
            )
        },
        padBy = 0f,
        maxBarValue = maxOf(maxStrokes, 1f)
    )

    Column(
        modifier = modifier
            .padding(top = 26.dp)
            .padding(bottom = 10.dp)
            .padding(end = 15.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val mainOppColor = colorResource(id = R.color.mainOpp)
        val axisColor = mainOppColor.copy(alpha = 0.8f)
        val gridColor = mainOppColor.copy(alpha = 0.2f)

        BarChart(
            barChartData = chartData,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 8.dp),
            xAxisDrawer = SimpleXAxisDrawer(axisLineColor = gridColor),
            yAxisDrawer = IntegerYAxisDrawer(
                axisLineColor = gridColor,
                labelTextColor = axisColor,
                gridLineColor = gridColor
            ),
            barDrawer = RoundedBarDrawer(),
            labelDrawer = SimpleLabelDrawer(
                drawLocation = SimpleLabelDrawer.DrawLocation.XAxis,
                labelTextColor = axisColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(color = colorResource(id = R.color.subTwoOpp)),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }
}

private class IntegerYAxisDrawer(
    val labelTextSize: TextUnit = 12.sp,
    val labelTextColor: Color = Color.Black,
    val axisLineColor: Color = Color.Black,
    val gridLineColor: Color = Color.LightGray,
    val labelValueFormatter: (Float) -> String = { it.roundToInt().toString() }
) : IYAxisDrawer {
    private val mAxisLinePaint = Paint().apply {
        isAntiAlias = true
        color = axisLineColor
        style = PaintingStyle.Stroke
    }

    private val mGridLinePaint = Paint().apply {
        isAntiAlias = true
        color = gridLineColor
        style = PaintingStyle.Stroke
    }

    private val mTextPaint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    // Cached from the previous drawAxisLabels call so grid lines can be drawn
    // in drawAxisLine, which runs before bars in the library's draw order.
    private var cachedMin = 0f
    private var cachedMax = 1f
    private var cachedArea: Rect? = null
    private var cachedCanvasWidth = 0f

    override fun drawAxisLine(drawScope: DrawScope, canvas: Canvas, drawableArea: Rect) {
        with(drawScope) {
            // Draw grid lines behind bars using values cached from the previous frame.
            val area = cachedArea
            if (area != null) {
                val range = cachedMax - cachedMin
                if (range > 0f) {
                    val start = floor(cachedMin.toDouble()).toInt()
                    val end = ceil(cachedMax.toDouble()).toInt()
                    for (value in start..end) {
                        val v = value.toFloat()
                        if (v !in cachedMin..cachedMax) continue
                        val ratio = (v - cachedMin) / range
                        val y = area.bottom - ratio * area.height
                        canvas.drawLine(
                            p1 = Offset(x = area.right, y = y),
                            p2 = Offset(x = cachedCanvasWidth, y = y),
                            paint = mGridLinePaint.apply { strokeWidth = 1.dp.toPx() }
                        )
                    }
                }
            }

            val lineThickness = 0.dp.toPx()
            val x = drawableArea.right - lineThickness / 2f
            canvas.drawLine(
                p1 = Offset(x = x, y = drawableArea.top),
                p2 = Offset(x = x, y = drawableArea.bottom),
                paint = mAxisLinePaint.apply { strokeWidth = lineThickness }
            )
        }
    }

    override fun drawAxisLabels(
        drawScope: DrawScope,
        canvas: Canvas,
        drawableArea: Rect,
        minValue: Float,
        maxValue: Float
    ) {
        with(drawScope) {
            // Cache for the next frame's drawAxisLine call.
            cachedMin = minValue
            cachedMax = maxValue
            cachedArea = drawableArea
            cachedCanvasWidth = size.width

            val labelPaint = mTextPaint.apply {
                color = labelTextColor.toLegacyInt()
                textSize = labelTextSize.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            val range = maxValue - minValue
            if (range <= 0f) return

            val startValue = floor(minValue.toDouble()).toInt()
            val endValue = ceil(maxValue.toDouble()).toInt()

            for (value in startValue..endValue) {
                val valueFloat = value.toFloat()
                if (valueFloat !in minValue..maxValue) continue

                val ratio = (valueFloat - minValue) / range
                val y = drawableArea.bottom - ratio * drawableArea.height

                val label = labelValueFormatter(value.toFloat())
                val x = drawableArea.right - 4.dp.toPx()

                val fontMetrics = labelPaint.fontMetrics
                val textY = y + (fontMetrics.descent - fontMetrics.ascent) / 2f - fontMetrics.descent

                canvas.nativeCanvas.drawText(label, x, textY, labelPaint)
            }
        }
    }
}

private class RoundedBarDrawer(private val cornerRadius: Float = 12f) : IBarDrawer {
    private val mBarPaint = Paint().apply { isAntiAlias = true }
    private val mPath = Path()

    override fun drawBar(
        drawScope: DrawScope,
        canvas: Canvas,
        barArea: Rect,
        bar: BarChartData.Bar
    ) {
        val r = minOf(cornerRadius, barArea.width / 2f)
        mPath.reset()
        mPath.addRoundRect(
            RoundRect(
                rect = barArea,
                topLeft = CornerRadius(r, r),
                topRight = CornerRadius(r, r),
                bottomLeft = CornerRadius(0f, 0f),
                bottomRight = CornerRadius(0f, 0f)
            )
        )
        canvas.drawPath(mPath, mBarPaint.apply { color = bar.color })
    }
}

private fun strokeColor(value: Int): Color {
    val cappedMax = maxOf(1, 10)
    val t = (value.toFloat() / cappedMax.toFloat()).coerceIn(0f, 1f)
    return if (t <= 0.5f) {
        Color(red = (t * 2f).coerceIn(0f, 1f), green = 1f, blue = 0f)
    } else {
        Color(red = 1f, green = (1f - ((t - 0.5f) * 2f)).coerceIn(0f, 1f), blue = 0f)
    }
}
