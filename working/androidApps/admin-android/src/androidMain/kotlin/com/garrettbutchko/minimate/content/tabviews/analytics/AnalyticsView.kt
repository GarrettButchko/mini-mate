package com.garrettbutchko.minimate.content.tabviews.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.toComposeColor
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.ExperienceTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.GrowthTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.OperationsTab
import com.garrettbutchko.minimate.content.tabviews.analytics.tabs.RetentionTab
import com.garrettbutchko.minimate.content.tabviews.analytics.topBar.AnalyticsRangeBar
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsSection
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SECTION_DAY_RANGE = "Day Range"

/**
 * AnalyticsView
 *
 * Mirrors iOS AnalyticsView — tier-gated analytics hub with:
 *  - Segmented picker: "Day Range" vs "Retention"
 *  - For Day Range: section selector (Growth / Operations / Experience) + range bar overlay
 *  - For Retention: RetentionTab full-screen
 *  - TierLockView for tier <= 2
 *
 * SwiftUI .skeleton → Modifier.skeleton() in SkeletonModifier.kt
 * iOS GeometryReader + topBarHeight → Compose top-bar padding via a measured Column
 */
@Composable
fun AnalyticsView(
    vm: AnalyticsViewModel,
    course: Course?,
    modifier: Modifier = Modifier
) {
    if (course == null) return

    if (course.tier <= 2) {
        TierLockView(modifier = modifier)
        return
    }

    var canRefresh by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pickedSection by vm.pickedSection.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header — title + refresh button + segmented picker
        AnalyticsHeader(
            vm = vm,
            isRefreshing = isRefreshing,
            canRefresh = canRefresh,
            onRefresh = {
                if (!canRefresh) return@AnalyticsHeader
                canRefresh = false
                isRefreshing = true
                scope.launch {
                    if (pickedSection == SECTION_DAY_RANGE) {
                        vm.refreshAnalytics(course = course)
                    } else {
                        vm.onAppearRetention(course = course)
                    }
                    delay(3_000)
                    canRefresh = true
                    isRefreshing = false
                }
            }
        )

        // Content
        if (pickedSection == SECTION_DAY_RANGE) {
            DayRangeContent(vm = vm, course = course)
        } else {
            RetentionTab(
                vm = vm.retentionVM,
                analyticsVM = vm,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun AnalyticsHeader(
    vm: AnalyticsViewModel,
    isRefreshing: Boolean,
    canRefresh: Boolean,
    onRefresh: () -> Unit
) {
    val pickedSection by vm.pickedSection.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Refresh button — replaces iOS "arrow.trianglehead.2.clockwise" rotate animation
            IconButton(onClick = onRefresh, enabled = canRefresh) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (canRefresh) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }

        // Segmented picker: "Day Range" vs "Retention"
        // iOS SegmentedPickerStyle → M3 SingleChoiceSegmentedButtonRow
        val sections = vm.pickerSections
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            sections.forEachIndexed { index, section ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
                    onClick = { vm.setPickedSection(section) },
                    selected = pickedSection == section,
                    label = { Text(section) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Day Range content — floating top bar + scrollable tab content
// ---------------------------------------------------------------------------
@Composable
private fun DayRangeContent(vm: AnalyticsViewModel, course: Course) {
    val selectedSection by vm.selectedSection.collectAsState()
    val range by vm.range.collectAsState()
    val loadingDocs by vm.loadingDocs.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content underneath
        val scrollState = rememberScrollState()

        // Reset scroll when section or range changes
        LaunchedEffect(selectedSection, range) {
            scrollState.animateScrollTo(0)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .padding(top = 8.dp)
        ) {
            // Spacer to push content below the floating top bar
            Spacer(modifier = Modifier.height(150.dp)) // approximate; refined at runtime

            if (loadingDocs) {
                AnalyticsLoadingSkeleton()
            } else {
                when (selectedSection) {
                    AnalyticsSection.GROWTH -> GrowthTab(vm = vm.growthVM)
                    AnalyticsSection.OPERATIONS -> OperationsTab(vm = vm.operationsVM)
                    AnalyticsSection.EXPERIENCE -> ExperienceTab(vm = vm.experienceVM, course = course)
                    else -> {}
                }
            }
        }

        // Floating top bar (section selector + range bar)
        // Equivalent to iOS topBar overlay with glass effect background
        Column(
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
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Section selector (Growth / Operations / Experience)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsSection.entries.forEach { section ->
                            val obj = vm.analyticsObjects[section.rawValue] ?: return@forEach
                            val isSelected = selectedSection == section
                            val themeColor = obj.color.toComposeColor() ?: MaterialTheme.colorScheme.onSurface

                            Surface(
                                onClick = { vm.setSelectedSection(section) },
                                shape = RoundedCornerShape(25.dp),
                                color = if (isSelected) themeColor.copy(alpha = 0.2f) else Color.Transparent,
                                modifier = Modifier
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Section icon — SF Symbol mapped to Material icon (approximate)
                                    Text(
                                        text = sectionEmoji(section),
                                        fontSize = 16.sp
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = section.rawValue,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = themeColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnalyticsRangeBar(vm = vm)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Emoji stand-ins for SF Symbol section icons (no direct Material icon mapping)
private fun sectionEmoji(section: AnalyticsSection): String = when (section) {
    AnalyticsSection.GROWTH -> "📈"
    AnalyticsSection.OPERATIONS -> "⚙️"
    AnalyticsSection.EXPERIENCE -> "⛳"
    else -> "📊"
}

// ---------------------------------------------------------------------------
// Loading skeleton — matches iOS LoadingSkeleton struct
// ---------------------------------------------------------------------------
@Composable
private fun AnalyticsLoadingSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {}
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {}
                    Surface(
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {}
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surface
            ) {}
        }
    }
}

// ---------------------------------------------------------------------------
// TierLockView — mirrors iOS TierLockView
// ---------------------------------------------------------------------------
@Composable
fun TierLockView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Icon stack — chart + lock
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF007AFF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF007AFF)
                )
                // Lock badge offset bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Advanced Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock deep insights into player retention, peak hours, and course performance with MiniMate Pro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Feature list
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                TierLockFeatureItem(title = "Day Range Analysis", description = "Track performance over custom time periods.")
                TierLockFeatureItem(title = "Retention Tracking", description = "See how many players return to your course.")
                TierLockFeatureItem(title = "Data Export", description = "Download CSV reports for your business records.")
            }

            // Upgrade CTA — stubbed
            Surface(
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF007AFF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Upgrade to Tier 3",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Compare Tiers in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TierLockFeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color(0xFF007AFF).copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✓", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
