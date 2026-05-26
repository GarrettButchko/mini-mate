package com.garrettbutchko.minimate.content.tabviews

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.tabviews.leaderboard.LeaderBoardView
import com.garrettbutchko.minimate.content.tabviews.tournament.TournamentView
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.toComposeColor
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.garrettbutchko.minimate.content.TitleView
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R.color as ResColor
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.HealthRatingChart
import com.garrettbutchko.minimate.content.tabviews.analytics.charts.SectionHealthDetailView
import com.garrettbutchko.minimate.modelKotlinExtensions.courseColors

/**
 * MainView
 *
 * The "Home" tab of the admin app — shows:
 * - Back button (navigate to course list)
 * - Course name + logo header
 * - HealthRatingChart (tier-gated, tier >= 3)
 * - Leaderboard + Tournament action buttons
 *
 * iOS-specific omissions:
 *  - TitleView (gradient banner) → replaced with a color bar built from course colors
 *  - MarqueeText → Text with maxLines=1 + overflow=Ellipsis (per project convention)
 *  - SectionHealthDetailView sheet → stubbed (not yet ported)
 *  - HealthRatingChart → rendered directly (already converted)
 *  - iOS .ifAvailableGlassEffect → Surface with surfaceVariant
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    course: Course?,
    analyticsVM: AnalyticsViewModel,
    leaderboardVM: LeaderBoardViewModel,
    photoURL: String?,
    onNavigateToCourseList: () -> Unit,
    onProfileTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLeaderboardSheet by remember { mutableStateOf(false) }
    var showTournamentSheet by remember { mutableStateOf(false) }
    val leaderboardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tournamentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scoreCardColor = course?.scoreCardColorDT?.toComposeColor()
    val subColor = Color.subVsColor(makeColor = scoreCardColor)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        // Header row: back button + course name + logo + avatar
        MainViewHeader(
            course = course,
            photoURL = photoURL,
            onBack = onNavigateToCourseList,
            onProfileTap = onProfileTap
        )

        TitleView(colors = course?.courseColors, isManager = true)

        // Health chart + action buttons area
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 108.dp) // space for floating buttons bar
                    .padding(bottom = 16.dp)
            ) {
                HealthSection(course = course, analyticsVM = analyticsVM)
            }

            // Floating buttons bar (Leaderboard + Tournament)
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    color = subColor,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MainViewButton(
                            title = "Leaderboard",
                            icon = Icons.Default.SportsScore,
                            color = Color(0xFF34C759), // green
                            modifier = Modifier.weight(1f),
                            onClick = { showLeaderboardSheet = true }
                        )
                        MainViewButton(
                            title = "Tournament",
                            icon = Icons.Default.EmojiEvents,
                            color = Color(0xFFFF9800), // orange
                            modifier = Modifier.weight(1f),
                            onClick = { showTournamentSheet = true }
                        )
                    }
                }
            }
        }
    }

    // Leaderboard bottom sheet
    if (showLeaderboardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLeaderboardSheet = false },
            sheetState = leaderboardSheetState,
            containerColor = colorResource(id = ResColor.bg)
        ) {
            LeaderBoardView(vm = leaderboardVM, course = course)
        }
    }

    // Tournament bottom sheet
    if (showTournamentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTournamentSheet = false },
            sheetState = tournamentSheetState,
            containerColor = colorResource(id = ResColor.bg)
        ) {
            TournamentView(modifier = Modifier.height(500.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun MainViewHeader(
    course: Course?,
    photoURL: String?,
    onBack: () -> Unit,
    onProfileTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to courses",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Course name + caption
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Course Dashboard For,",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // MarqueeText → Text with overflow (project convention)
            Text(
                text = course?.name ?: "No Course Selected",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Course logo (if available)
        if (course?.logo != null) {
            AsyncImage(
                model = course.logo,
                contentDescription = "Course logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        // User avatar — placeholder circle if no URL
        IconButton(onClick = onProfileTap) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoURL != null) {
                        AsyncImage(
                            model = photoURL,
                            contentDescription = "User avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Health section
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthSection(course: Course?, analyticsVM: AnalyticsViewModel) {
    val isTierGated = (course?.tier ?: 0) < 3
    val isLoadingHealth by analyticsVM.isLoadingHealth.collectAsState()
    val healthReport by analyticsVM.healthReport.collectAsState()
    val scoreCardColor = course?.scoreCardColorDT?.toComposeColor()
    val cardBackground = Color.subVsColor(makeColor = scoreCardColor)
    var showHealthSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Content — blurred when tier-gated, matching iOS .blur(radius: 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .then(if (isTierGated) Modifier.blur(6.dp) else Modifier)
        ) {
            when {
                isLoadingHealth -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardBackground, RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Analyzing course health...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                healthReport != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTierGated) { showHealthSheet = true }
                    ) {
                        HealthRatingChart(
                            healthReport = healthReport!!,
                            makeColor = scoreCardColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardBackground, RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Health Data Available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Analytics data is being collected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tier gate — no background, floats over blurred content matching iOS
        if (isTierGated) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text("Upgrade to Tier 3", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Unlock Course Health Analytics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showHealthSheet && healthReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showHealthSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            SectionHealthDetailView(healthReport = healthReport!!)
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable action button
// ---------------------------------------------------------------------------
@Composable
private fun MainViewButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        enabled = !disabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
