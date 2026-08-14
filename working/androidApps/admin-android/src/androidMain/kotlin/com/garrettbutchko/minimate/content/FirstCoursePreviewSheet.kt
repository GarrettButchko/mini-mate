package com.garrettbutchko.minimate.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.color
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.skeleton
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.shared.R.color as ResColor
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.analytics.SectionHealthRating
import kotlin.math.roundToInt

/**
 * Shown once, right after a manager adds their very first course: a tour of what
 * the course now gets, what's already being tracked, and a way into the dashboard.
 */
@Composable
fun FirstCoursePreviewSheet(
    courseListVM: CourseListViewModel,
    analyticsVM: AnalyticsViewModel,
    onOpenDashboard: (Course) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userCourses by courseListVM.userCourses.collectAsState()
    val loadingCourse by courseListVM.loadingCourse.collectAsState()
    val healthReport by analyticsVM.healthReport.collectAsState()
    val isLoadingHealth by analyticsVM.isLoadingHealth.collectAsState()

    val course = userCourses.firstOrNull()

    LaunchedEffect(course?.id) {
        course?.let { analyticsVM.loadHealthData(course = it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = ResColor.bg))
    ) {
        if (course != null) {
            FirstCoursePreviewBody(
                course = course,
                healthSections = healthSections(
                    growth = healthReport?.growthHealth,
                    experience = healthReport?.experienceHealth,
                    operations = healthReport?.operationsHealth,
                    retention = healthReport?.retentionHealth
                ),
                isLoadingHealth = isLoadingHealth,
                onOpenDashboard = {
                    courseListVM.setCourse(course = course)
                    onOpenDashboard(course)
                },
                onBack = onBack
            )
        }

        LoadingOverlay(
            visible = loadingCourse || course == null,
            message = "Loading course details..."
        )
    }
}

// ---------------------------------------------------------------------------
// Layout
// ---------------------------------------------------------------------------

private data class HealthSection(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val rating: SectionHealthRating?
)

@Composable
private fun healthSections(
    growth: SectionHealthRating?,
    experience: SectionHealthRating?,
    operations: SectionHealthRating?,
    retention: SectionHealthRating?
): List<HealthSection> = listOf(
    HealthSection("Growth", colorResource(id = ResColor.swiftGreen), Icons.AutoMirrored.Filled.TrendingUp, growth),
    HealthSection("Experience", colorResource(id = ResColor.swiftRed), Icons.Default.Star, experience),
    HealthSection("Operations", colorResource(id = ResColor.swiftPurple), Icons.Default.Schedule, operations),
    HealthSection("Retention", colorResource(id = ResColor.swiftBlue), Icons.Default.Refresh, retention)
)

private data class TierFeature(
    val icon: ImageVector,
    val text: String,
    val tier: Int,
    val isSoon: Boolean = false
)

private val allFeatures = listOf(
    TierFeature(Icons.Default.Place, "Course listed on the Mini Mate map", tier = 1),
    TierFeature(Icons.Default.Tune, "Custom par info for every hole", tier = 1),
    TierFeature(Icons.Default.Link, "Social links on your course profile", tier = 1),
    TierFeature(Icons.Default.Palette, "Custom scorecard colors & accent", tier = 2),
    TierFeature(Icons.Default.Campaign, "Your ad in scorecards & app pages", tier = 2),
    TierFeature(Icons.Default.Image, "Your logo in the app home & scorecard", tier = 2),
    TierFeature(Icons.Default.EmojiEvents, "Managed course leaderboard", tier = 2),
    TierFeature(Icons.Default.Bolt, "Tournament mode access", tier = 2, isSoon = true),
    TierFeature(Icons.Default.CardGiftcard, "Mini Mate pitch tent & marketing kit", tier = 2),
    TierFeature(Icons.Default.Nfc, "NFC-enabled tent tag (Mini Mate branded)", tier = 2),
    TierFeature(Icons.AutoMirrored.Filled.TrendingUp, "Growth analytics dashboard", tier = 3),
    TierFeature(Icons.Default.Refresh, "Player retention tracking", tier = 3),
    TierFeature(Icons.Default.Star, "Experience score metrics", tier = 3),
    TierFeature(Icons.Default.Email, "Player email list from your course", tier = 3),
    TierFeature(Icons.Default.ArrowDownward, "CSV export — all data, any time", tier = 3),
    TierFeature(Icons.Default.Dashboard, "Beautiful analytics interface", tier = 3)
)

@Composable
private fun FirstCoursePreviewBody(
    course: Course,
    healthSections: List<HealthSection>,
    isLoadingHealth: Boolean,
    onOpenDashboard: () -> Unit,
    onBack: () -> Unit
) {
    val bg = colorResource(id = ResColor.bg)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 94.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { TierCard(tier = course.tier, numHoles = course.numHoles, leaderBoardActive = course.leaderBoardActive) }
            item { HealthPreview(sections = healthSections, isLoadingHealth = isLoadingHealth) }
            item { FeaturesPreview(tier = course.tier) }
        }

        // Header and CTAs float over the scroll, fading it out behind them
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(bg, Color.Transparent)))
                .padding(horizontal = 20.dp)
                .padding(top = 30.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = ResColor.mainOpp)
            )
            Text(
                text = "Course ID: ${course.id}",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = ResColor.subTwoOpp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, bg)))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = ResColor.swiftBlue),
                    contentColor = Color.White
                )
            ) {
                Text("Open Dashboard", fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = onBack) {
                Text("Back to Courses", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TierCard(tier: Int, numHoles: Int, leaderBoardActive: Boolean) {
    SheetCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = tierIcon(tier),
                contentDescription = null,
                tint = colorResource(id = ResColor.swiftBlue)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${tierName(tier)} Plan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = ResColor.mainOpp)
                )
                Text(
                    text = "$numHoles holes" + if (leaderBoardActive) " · Leaderboard active" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = ResColor.subTwoOpp)
                )
            }

            Text(
                text = tierPrice(tier),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = ResColor.subTwoOpp)
            )
        }
    }
}

@Composable
private fun HealthPreview(sections: List<HealthSection>, isLoadingHealth: Boolean) {
    SheetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Already Being Tracked",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = ResColor.mainOpp)
            )

            sections.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { section ->
                        HealthCard(
                            section = section,
                            isLoadingHealth = isLoadingHealth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthCard(
    section: HealthSection,
    isLoadingHealth: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(13.dp)

    Column(
        modifier = modifier
            .background(colorResource(id = ResColor.subTwo), shape)
            .border(1.dp, section.color.copy(alpha = 0.3f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = colorResource(id = ResColor.subTwoOpp),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = section.name,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = ResColor.mainOpp)
            )
        }

        val rating = section.rating
        AnimatedVisibility(
            visible = rating != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Column {
                Text(
                    text = rating?.grade?.rawValue.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = rating?.grade?.color ?: Color.Transparent
                )
                Text(
                    text = "Score: ${rating?.score?.roundToInt() ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = ResColor.subTwoOpp)
                )
            }
        }

        if (rating == null) {
            Column {
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = ResColor.subTwoOpp).copy(alpha = 0.3f),
                    modifier = Modifier.skeleton(active = isLoadingHealth)
                )
                Text(
                    text = "Score: —",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = ResColor.subTwoOpp),
                    modifier = Modifier.skeleton(active = isLoadingHealth)
                )
            }
        }
    }
}

@Composable
private fun FeaturesPreview(tier: Int) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayFeatures = if (isExpanded) allFeatures else allFeatures.take(4)

    SheetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "What's Included",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = ResColor.mainOpp)
            )

            displayFeatures.forEach { feature ->
                FeatureRow(feature = feature, included = feature.tier <= tier)
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) "Show Less" else "View All Features",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(feature: TierFeature, included: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (included) feature.icon else Icons.Default.Lock,
            contentDescription = null,
            tint = if (included) {
                colorResource(id = ResColor.swiftBlue)
            } else {
                colorResource(id = ResColor.subTwoOpp).copy(alpha = 0.4f)
            },
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = feature.text,
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(id = if (included) ResColor.mainOpp else ResColor.subTwoOpp),
            modifier = Modifier.weight(1f, fill = false)
        )

        if (feature.isSoon) {
            val green = colorResource(id = ResColor.swiftGreen)
            Text(
                text = "SOON",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = green,
                modifier = Modifier
                    .background(green.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SheetCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(),
        shape = RoundedCornerShape(25.dp),
        color = colorResource(id = ResColor.sub)
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Tier helpers
// ---------------------------------------------------------------------------

private fun tierName(tier: Int): String = when (tier) {
    2 -> "Partner"
    3 -> "Analytics"
    else -> "Listed"
}

private fun tierPrice(tier: Int): String = when (tier) {
    2 -> "$29 / mo"
    3 -> "$69 / mo"
    else -> "Free"
}

private fun tierIcon(tier: Int): ImageVector = when (tier) {
    2 -> Icons.Default.Apartment
    3 -> Icons.Default.BarChart
    else -> Icons.Default.Place
}
