package com.garrettbutchko.minimate.content.tabviews.analytics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.skeleton
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.ExperienceTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.GrowthTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.OperationsTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.RetentionView
import com.garrettbutchko.minimate.content.tabviews.analytics.topBar.AnalyticsRangeBar
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.repositories.analytics.AnalyticsRange
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsObject
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsSection
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.admin.BuildConfig
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun analyticsObjectColor(key: String): Color = when (key) {
    "GREEN"  -> Color(0xFF34C759)
    "PURPLE" -> Color(0xFFCB30E0)
    "PINK"   -> Color(0xFFFF2D55)
    "BLUE"   -> Color(0xFF0088FF)
    "ORANGE" -> Color(0xFFFF8D28)
    "RED"    -> Color(0xFFFF3B30)
    "YELLOW" -> Color(0xFFFFCC00)
    else     -> Color(0xFF34C759)
}

private fun analyticsObjectIcon(name: String): ImageVector = when {
    name.contains("uptrend") -> Icons.AutoMirrored.Filled.TrendingUp
    name.contains("clock")   -> Icons.Default.Schedule
    name.contains("star")    -> Icons.Default.Star
    else                     -> Icons.AutoMirrored.Filled.TrendingUp
}

// ---------------------------------------------------------------------------
// AnalyticsView — top-level entry point
// ---------------------------------------------------------------------------
@Composable
fun AnalyticsView(
    vm: AnalyticsViewModel,
    course: Course?,
    modifier: Modifier = Modifier
) {
    if (course == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    if (course.tier <= 2) {
        TierLockView(modifier = modifier)
        return
    }

    val pickedSection by vm.pickedSection.collectAsState()
    val selectedSection by vm.selectedSection.collectAsState()
    val range by vm.range.collectAsState()
    val loadingDocs by vm.loadingDocs.collectAsState()

    LaunchedEffect(course.id) {
        vm.onAppearDailyAnalytics(course)
        vm.onAppearRetention(course)
    }

    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val topBarHeightDp: Dp = with(density) { topBarHeightPx.toDp() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnalyticsHeaderSection(
            vm = vm,
            pickedSection = pickedSection,
            course = course
        )

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = pickedSection,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "analyticsPickedSection"
            ) { section ->
                when (section) {
                    "Day Range" -> {
                        val scrollState = rememberScrollState()

                        LaunchedEffect(selectedSection) { scrollState.animateScrollTo(0) }
                        LaunchedEffect(range) { scrollState.animateScrollTo(0) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(
                                    top = topBarHeightDp,
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp
                                )
                        ) {
                            AnimatedContent(
                                targetState = loadingDocs,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "analyticsLoadingTransition"
                            ) { isLoading ->
                                if (isLoading) {
                                    AnalyticsLoadingSkeleton()
                                } else {
                                    AnimatedContent(
                                        targetState = selectedSection,
                                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                                        label = "analyticsSectionContent"
                                    ) { sec ->
                                        when (sec) {
                                            AnalyticsSection.GROWTH ->
                                                GrowthTab(vm = vm)
                                            AnalyticsSection.OPERATIONS ->
                                                OperationsTab(vm = vm)
                                            AnalyticsSection.EXPERIENCE ->
                                                ExperienceTab(vm = vm, course = course)
                                            else -> Box(Modifier.fillMaxWidth())
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    topBarHeightPx = coords.size.height
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.background,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0f)
                                            )
                                        )
                                    )
                            ) {
                                AnalyticsTopBar(
                                    vm = vm,
                                    selectedSection = selectedSection,
                                    range = range,
                                    course = course,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }

                    else -> RetentionView(vm = vm)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header — title + refresh + debug menu + segmented picker
// ---------------------------------------------------------------------------
@Composable
private fun AnalyticsHeaderSection(
    vm: AnalyticsViewModel,
    pickedSection: String,
    course: Course?
) {
    val scope = rememberCoroutineScope()
    var isRotating by remember { mutableStateOf(false) }
    var canRefresh by remember { mutableStateOf(true) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "refreshRotation"
    )

    var showDebugMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (!canRefresh) return@IconButton
                    canRefresh = false
                    isRotating = true
                    if (pickedSection == "Day Range") {
                        vm.refreshAnalytics(course)
                    } else {
                        vm.onAppearRetention(course)
                    }
                    scope.launch {
                        delay(1.seconds)
                        isRotating = false
                        delay(2.seconds)
                        canRefresh = true
                    }
                },
                enabled = canRefresh,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = if (canRefresh) 1f else 0.45f),
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            if (BuildConfig.DEBUG) {
                Box {
                    IconButton(
                        onClick = { showDebugMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Debug",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showDebugMenu,
                        onDismissRequest = { showDebugMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload Daily Docs") },
                            onClick = { showDebugMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Upload 100 Test Emails") },
                            onClick = { showDebugMenu = false }
                        )
                    }
                }
            }
        }

        val pickerSections = vm.pickerSections
        @OptIn(ExperimentalMaterial3Api::class)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
                .subtleShadow(shape = RoundedCornerShape(50))
        ) {
            pickerSections.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = label == pickedSection,
                    onClick = { vm.setPickedSection(label) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = pickerSections.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondary,
                        activeContentColor = MaterialTheme.colorScheme.onSecondary,
                        activeBorderColor = MaterialTheme.colorScheme.secondary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                        inactiveBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                    label = { Text(label) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Floating TopBar — section buttons + range bar
// ---------------------------------------------------------------------------
@Composable
private fun AnalyticsTopBar(
    vm: AnalyticsViewModel,
    selectedSection: AnalyticsSection,
    range: AnalyticsRange,
    course: Course?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.subtleShadow(),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Note: Kept the retention filter, remove it if you want 1:1 matching with Swift's allCases
                AnalyticsSection.entries
                    .filter { it != AnalyticsSection.RETENTION }
                    .forEach { section ->
                        val obj = vm.analyticsObjects[section.rawValue] ?: return@forEach
                        val isSelected = selectedSection == section

                        // 1. Animate the weight target based on selection
                        val animatedWeight by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.25f, // Adjust 0.4f to control unselected width
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "weightAnimation"
                        )

                        // 2. Pass the animated weight directly to the modifier
                        SectionButton(
                            section = section,
                            obj = obj,
                            isSelected = isSelected,
                            onClick = { vm.setSelectedSection(section) },
                            modifier = Modifier.weight(animatedWeight)
                        )
                    }
            }

            AnalyticsRangeBar(
                vm = vm,
                range = range,
                course = course,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// SectionButton — pill chip, icon always visible, label only when selected
// ---------------------------------------------------------------------------
@Composable
private fun SectionButton(
    section: AnalyticsSection,
    obj: AnalyticsObject,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColor = analyticsObjectColor(obj.color)
    val icon = analyticsObjectIcon(obj.icon)

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) themeColor.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = spring(stiffness = 400f),
        label = "sectionBtnBg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(bgColor)
            .then(
                if (!isSelected) Modifier.border(
                    width = 2.dp, // Adjusted closer to your SwiftUI stroke look
                    color = themeColor.copy(alpha = 0.15f), // Matches SwiftUI opacity blend (0.3 * 0.5)
                    shape = RoundedCornerShape(25.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = section.rawValue,
            tint = themeColor,
            modifier = Modifier.size(16.dp)
        )

        // Changed to expandHorizontally to match SwiftUI's physical row-widening effect
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterStart)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = section.rawValue,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared helpers — accessible to tabs and helperviews
// ---------------------------------------------------------------------------
@Composable
internal fun AnalyticsLoadingSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth().subtleShadow(),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .skeleton(active = true),
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.secondary
                ) {}
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .skeleton(active = true),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ) {}
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .skeleton(active = true),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ) {}
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .skeleton(active = true),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {}
        }
    }
}

@Composable
internal fun ChartEmptyState(
    message: String,
    icon: ImageVector? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Not enough data yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// TierLockView
// ---------------------------------------------------------------------------
@Composable
fun TierLockView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Advanced Analytics",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Unlock deep insights into player retention, peak hours, and course performance with MiniMate Pro.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            FeatureItem(
                icon = Icons.Default.Schedule,
                title = "Day Range Analysis",
                description = "Track performance over custom time periods."
            )
            FeatureItem(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = "Retention Tracking",
                description = "See how many players return to your course."
            )
            FeatureItem(
                icon = Icons.Default.Star,
                title = "Data Export",
                description = "Download CSV reports for your business records."
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Upgrade to Tier 3",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Compare Tiers in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
