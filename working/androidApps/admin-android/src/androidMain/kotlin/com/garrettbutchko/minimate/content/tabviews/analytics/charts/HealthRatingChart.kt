package com.garrettbutchko.minimate.content.tabviews.analytics.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.helper.modifiers.subTwoVsColor
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsSection
import com.garrettbutchko.minimate.viewModels.analytics.CourseHealthReport
import com.garrettbutchko.minimate.viewModels.analytics.HealthGrade
import com.garrettbutchko.minimate.viewModels.analytics.Insight
import com.garrettbutchko.minimate.viewModels.analytics.InsightType
import com.garrettbutchko.minimate.viewModels.analytics.SectionHealthRating

private val HealthGrade.color: Color
    get() = when (this) {
        HealthGrade.EXCELLENT, HealthGrade.GREAT -> Color(0xFF34C759)
        HealthGrade.GOOD, HealthGrade.SATISFACTORY -> Color(0xFF0088FF)
        HealthGrade.FAIR -> Color(0xFFFFCC00)
        HealthGrade.NEEDS_IMPROVEMENT -> Color(0xFFFF9500)
        HealthGrade.POOR -> Color(0xFFFF6B00)
        HealthGrade.CRITICAL -> Color(0xFFFF3B30)
    }

private val InsightType.color: Color
    get() = when (this) {
        InsightType.GOOD -> Color(0xFF34C759)
        InsightType.AVERAGE -> Color(0xFF0088FF)
        InsightType.WARNING -> Color(0xFFFF9500)
        InsightType.CRITICAL -> Color(0xFFFF3B30)
    }

private val InsightType.icon: ImageVector
    get() = when (this) {
        InsightType.GOOD -> Icons.Default.CheckCircle
        InsightType.AVERAGE -> Icons.Default.Info
        InsightType.WARNING -> Icons.Default.Warning
        InsightType.CRITICAL -> Icons.Default.Cancel
    }

private val AnalyticsSection.color: Color
    get() = when (this) {
        AnalyticsSection.GROWTH -> Color(0xFF34C759)
        AnalyticsSection.OPERATIONS -> Color(0xFF0088FF)
        AnalyticsSection.EXPERIENCE -> Color(0xFFBF5AF2)
        AnalyticsSection.RETENTION -> Color(0xFFFF9500)
    }

@Composable
fun HealthRatingChart(
    healthReport: CourseHealthReport,
    modifier: Modifier = Modifier,
    makeColor: Color? = null
) {
    val scoreAnimatable = remember { Animatable(0f) }
    LaunchedEffect(healthReport.overallScore) {
        scoreAnimatable.snapTo(0f)
        scoreAnimatable.animateTo(
            targetValue = healthReport.overallScore.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }
    val animatedScore = scoreAnimatable.value

    val trackColor = Color.subTwoVsColor(makeColor = makeColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(Color.subVsColor(makeColor = makeColor))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Course Health Rating",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier.size(width = 160.dp, height = 110.dp),
                contentAlignment = Alignment.Center
            ) {
                GaugeCanvas(
                    score = animatedScore,
                    trackColor = trackColor
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = healthReport.overallGrade.color,
                        lineHeight = 40.sp
                    )
                    Text(
                        text = healthReport.overallGrade.rawValue,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = healthReport.overallGrade.color
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(
                        title = "Growth",
                        score = healthReport.growthHealth.score,
                        grade = healthReport.growthHealth.grade,
                        makeColor = makeColor
                    )
                    MiniStatCard(
                        title = "Operations",
                        score = healthReport.operationsHealth.score,
                        grade = healthReport.operationsHealth.grade,
                        makeColor = makeColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(
                        title = "Retention",
                        score = healthReport.retentionHealth.score,
                        grade = healthReport.retentionHealth.grade,
                        makeColor = makeColor
                    )
                    MiniStatCard(
                        title = "Experience",
                        score = healthReport.experienceHealth.score,
                        grade = healthReport.experienceHealth.grade,
                        makeColor = makeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun GaugeCanvas(score: Float, trackColor: Color) {
    val gradientColors = when {
        score >= 85f -> listOf(Color(0xFF34C759), Color(0xFF34C759).copy(alpha = 0.7f))
        score >= 70f -> listOf(Color(0xFF0088FF), Color(0xFF64D2FF))
        score >= 50f -> listOf(Color(0xFFFF9500), Color(0xFFFFCC00))
        else -> listOf(Color(0xFFFF3B30), Color(0xFFFF9500))
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 12.dp.toPx()
        val cx = size.width / 2f
        // Mirror iOS: arc ZStack is offset(y: 150) inside a 160×110 frame.
        // The shape draws with center at (midX, minY), so after the offset the center
        // sits 150/110 * canvasHeight below the canvas top — i.e. 40dp below the bottom.
        val cy = size.height * (150f / 110f)
        // Mirror iOS: radius = min(rect.width, rect.height * 2)
        val r = minOf(size.width, size.height * 2f)

        val startAngle = 200f
        val totalSweep = 140f
        val activeSweep = totalSweep * (score.coerceIn(0f, 100f) / 100f)

        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            brush = Brush.horizontalGradient(gradientColors),
            startAngle = startAngle,
            sweepAngle = activeSweep,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MiniStatCard(title: String, score: Double, grade: HealthGrade, makeColor: Color?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.subTwoVsColor(makeColor = makeColor))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${score.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = grade.color
            )
            Surface(
                shape = CircleShape,
                color = grade.color
            ) {
                Text(
                    text = grade.rawValue,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// --- Section Health Detail View ---

@Composable
fun SectionHealthDetailView(
    healthReport: CourseHealthReport,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Scrollable content — top padding leaves room for the floating header
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 88.dp, start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthSectionCard(rating = healthReport.growthHealth)
            HealthSectionCard(rating = healthReport.operationsHealth)
            HealthSectionCard(rating = healthReport.experienceHealth)
            HealthSectionCard(rating = healthReport.retentionHealth)

            if (healthReport.topInsights.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.subTwoVsColor(makeColor = null))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Key Insights",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    healthReport.topInsights.take(5).forEach { (insight, section) ->
                        InsightRowView(insight = insight, section = section)
                    }
                }
            }
        }

        // Top fade gradient — sits behind the floating header, matching iOS mask effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health Breakdown",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Bottom fade gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
        )
    }
}

@Composable
private fun HealthSectionCard(rating: SectionHealthRating) {
    val subTwoColor = Color.subTwoVsColor(makeColor = null)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(subTwoColor, RoundedCornerShape(12.dp))
            .border(1.dp, rating.section.color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionCardHeader(rating = rating)
        SectionCardProgressBar(score = rating.score, gradeColor = rating.grade.color)
        if (rating.insights.isNotEmpty()) {
            SectionCardInsightsList(insights = rating.insights)
        }
    }
}

@Composable
private fun InsightRowView(insight: Insight, section: AnalyticsSection) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = insight.insightType.icon,
            contentDescription = null,
            tint = insight.insightType.color,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = section.rawValue.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = section.color,
                modifier = Modifier
                    .border(1.dp, section.color, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCardHeader(rating: SectionHealthRating) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rating.section.rawValue,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${rating.score.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = rating.grade.color
            )
            Surface(
                shape = CircleShape,
                color = rating.grade.color
            ) {
                Text(
                    text = rating.grade.rawValue,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCardProgressBar(score: Double, gradeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = (score / 100).toFloat().coerceIn(0f, 1f)),
            shape = RoundedCornerShape(4.dp),
            color = gradeColor
        ) {}
    }
}

@Composable
private fun SectionCardInsightsList(insights: List<Insight>) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        insights.take(2).forEach { insight ->
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = insight.insightType.icon,
                    contentDescription = null,
                    tint = insight.insightType.color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}
