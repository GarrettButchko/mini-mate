package com.garrettbutchko.minimate.content.helper.modifiers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.holeModels.Hole
import com.garrettbutchko.minimate.shared.R
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.bar.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.bar.render.yaxis.SimpleYAxisDrawer
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer

@Composable
fun BarChartView(
    data: List<Hole>,
    title: String,
    modifier: Modifier = Modifier,
    paddingReview: Boolean = false
) {
    val chartData = BarChartData(
        bars = data.mapIndexed { index, hole ->
            BarChartData.Bar(
                label = if (index % 3 == 0) "H${hole.number}" else "",
                value = hole.strokes.toFloat(),
                color = strokeColor(hole.strokes)
            )
        }
    )

    Column(
        modifier = modifier
            .padding(top = 26.dp)
            .padding(bottom = if (paddingReview) 5.dp else 10.dp)
            .padding(horizontal = 16.dp)
            .padding(end = if (paddingReview) 12.dp else 15.dp)
            .fillMaxWidth()
    ) {
        val mainOppColor = colorResource(id = R.color.mainOpp)
        val axisColor = mainOppColor.copy(alpha = 0.8f)
        val gridColor = mainOppColor.copy(alpha = 0.2f)

        BarChart(
            barChartData = chartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(start = 8.dp),
            xAxisDrawer = SimpleXAxisDrawer(axisLineColor = gridColor),
            yAxisDrawer = SimpleYAxisDrawer(axisLineColor = gridColor, labelTextColor = axisColor),
            barDrawer = SimpleBarDrawer(),
            labelDrawer = SimpleLabelDrawer(labelTextColor = axisColor)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(color = colorResource(id = R.color.subThreeOpp)),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
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
