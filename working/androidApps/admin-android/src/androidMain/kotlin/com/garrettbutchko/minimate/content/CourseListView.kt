package com.garrettbutchko.minimate.content

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.garrettbutchko.minimate.utilities.AppLinks
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.AppTextFieldShape
import com.garrettbutchko.minimate.content.helper.appTextFieldColors
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
import com.garrettbutchko.minimate.content.helper.StatusMessage
import com.garrettbutchko.minimate.content.helper.TransientStatusOverlay
import com.garrettbutchko.minimate.content.helper.rememberTransientStatus
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.garrettbutchko.minimate.content.helper.AppTextFieldShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.shared.R.color as ResColor
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

/**
 * CourseListView
 *
 * The first screen after sign-in. Shows either:
 *  - multiCourse: list of managed courses + add button
 *  - firstCourse: onboarding screen to add the very first course
 *
 * iOS-to-Android mappings used here:
 *  - GeometryReader → not needed (Compose auto-sizes)
 *  - .contentMargins(.horizontal,16) → Modifier.padding(horizontal=16)
 *  - .animation(.bouncy) → AnimatedContent(fadeIn/fadeOut)
 *  - Timer / onReceive → LaunchedEffect with delay loop
 *  - SwiftUI Alert with TextField → AlertDialog + OutlinedTextField
 *  - iOS ProfileView sheet → stubbed (not yet ported)
 *  - iOS label { AsyncImage / logoOpp } → coil AsyncImage with fallback initial text
 *  - .cardShadow() → Surface tonalElevation
 *  - .sub / .mainOpp color tokens → surfaceVariant / onSurface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListView(
    vm: CourseListViewModel,
    analyticsVM: AnalyticsViewModel,
    photoURL: String?,
    hasAdminCourses: Boolean,
    onNavigateToCourseTab: (Int) -> Unit,
    onProfileTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userCourses by vm.userCourses.collectAsState()
    val showAddCourseAlert by vm.showAddCourseAlert.collectAsState()
    val loadingCourse by vm.loadingCourse.collectAsState()
    val didLeaveCourse by vm.didLeaveCourse.collectAsState()
    val leftCourseNotice = rememberTransientStatus()
    var showFirstCourseSheet by remember { mutableStateOf(false) }

    // hasCourse should be true if they HAVE admin courses, even if not fetched yet.
    // This allows MultiCourseView to show its loading spinner.
    val hasCourse = hasAdminCourses || userCourses.isNotEmpty()

    // Tick the VM timer every second (replaces iOS Timer.publish)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000.milliseconds)
            vm.tick()
        }
    }

    // Show a transient "Left Course" notice once after removing yourself from a
    // course (mirrors iOS CourseListView's didLeaveCourse overlay).
    LaunchedEffect(didLeaveCourse) {
        if (didLeaveCourse) {
            vm.clearDidLeaveCourse()
            leftCourseNotice.show(
                StatusMessage(
                    title = "Left Course",
                    subtitle = "You're no longer an admin of this course.",
                    icon = Icons.Filled.PersonRemove
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
            color = colorResource(id = ResColor.bg)
        ) {
            AnimatedContent(
                targetState = hasCourse,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "courseListTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) { hasC ->
                if (hasC) {
                    MultiCourseView(
                        vm = vm,
                        photoURL = photoURL,
                        onNavigateToCourseTab = onNavigateToCourseTab,
                        onProfileTap = onProfileTap,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    FirstCourseView(
                        vm = vm,
                        photoURL = photoURL,
                        onProfileTap = onProfileTap,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        LoadingOverlay(visible = loadingCourse, message = "Loading courses...")

        TransientStatusOverlay(leftCourseNotice)
    }

    // Add Course dialog (replaces iOS .alert with TextField)
    if (showAddCourseAlert) {
        AddCourseDialog(
            vm = vm,
            hasCourse = hasCourse,
            onFirstCourseAdded = { showFirstCourseSheet = true }
        )
    }

    if (showFirstCourseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFirstCourseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colorResource(id = ResColor.bg)
        ) {
            FirstCoursePreviewSheet(
                courseListVM = vm,
                analyticsVM = analyticsVM,
                onOpenDashboard = {
                    showFirstCourseSheet = false
                    onNavigateToCourseTab(1)
                },
                onBack = { showFirstCourseSheet = false }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// MultiCourseView
// ---------------------------------------------------------------------------
@Composable
private fun MultiCourseView(
    vm: CourseListViewModel,
    photoURL: String?,
    onNavigateToCourseTab: (Int) -> Unit,
    onProfileTap: () -> Unit,
    modifier: Modifier
) {
    val loadingCourse by vm.loadingCourse.collectAsState()
    val message by vm.message.collectAsState()
    val timeRemaining by vm.timeRemaining.collectAsState()
    val userCourses by vm.userCourses.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Courses",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                color = colorResource(id = ResColor.mainOpp)
            )

            // Refresh button
            IconButton(onClick = { vm.getCourses() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh courses",
                    tint = colorResource(id = ResColor.swiftBlue)
                )
            }

            // User avatar
            UserAvatar(photoURL = photoURL, onClick = onProfileTap)
        }

        // The list stays put while refreshing — LoadingOverlay covers it instead
        // of swapping the whole screen out for a spinner.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rate-limit message banner
            if (message != null) {
                RateLimitBanner(message = message!!, timeRemaining = timeRemaining)
            }

            // Course buttons
            userCourses.forEach { course ->
                CourseButtonView(
                    course = course,
                    onClick = {
                        vm.setCourse(course = course)
                        onNavigateToCourseTab(1)
                    }
                )
            }

            // Add new course button
            Button(
                onClick = { vm.setShowAddCourseAlert(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = ResColor.swiftBlue),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = timeRemaining <= 0
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add a new course", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Get password link button
        Box(modifier = Modifier.padding(bottom = 16.dp)) {
            GetPasswordButton()
        }
    }
}

// ---------------------------------------------------------------------------
// FirstCourseView
// ---------------------------------------------------------------------------
@Composable
private fun FirstCourseView(
    vm: CourseListViewModel,
    photoURL: String?,
    onProfileTap: () -> Unit,
    modifier: Modifier
) {
    val message by vm.message.collectAsState()
    val timeRemaining by vm.timeRemaining.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Top-right: retry + avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { vm.getCourses() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh courses",
                    tint = colorResource(id = ResColor.swiftBlue)
                )
            }
            UserAvatar(photoURL = photoURL, onClick = onProfileTap)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Headline
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add your first course",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(id = ResColor.mainOpp)
                )
                Text(
                    text = "Mini Manager lets you customize your scorecard, run leaderboards/tournaments, and view course analytics.",
                    color = colorResource(id = ResColor.subOpp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Feature list card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleShadow(),
                shape = RoundedCornerShape(25.dp),
                color = colorResource(id = ResColor.sub)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    FirstCourseFeature(icon = Icons.Default.Brush, label = "Customize your scorecard look", color = colorResource(id = ResColor.swiftRed))
                    FirstCourseFeature(icon = Icons.Default.BarChart, label = "Collect emails + see analytics", color = colorResource(id = ResColor.swiftGreen))
                    FirstCourseFeature(icon = Icons.Default.EmojiEvents, label = "Leaderboards and tournaments", color = colorResource(id = ResColor.swiftBlue))
                    FirstCourseFeature(icon = Icons.Default.Campaign, label = "Run promos with your own in-app ad", color = colorResource(id = ResColor.swiftOrange))
                }
            }

            // "And More!" card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleShadow(),
                shape = RoundedCornerShape(25.dp),
                color = colorResource(id = ResColor.sub)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = colorResource(id = ResColor.swiftBlue))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "And More!",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colorResource(id = ResColor.mainOpp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Need your course password?
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Need your course password?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colorResource(id = ResColor.mainOpp)
                )
                Text(
                    text = "Visit our website to request it and we'll email your course password to the address on file.",
                    color = colorResource(id = ResColor.subOpp)
                )
                GetPasswordButton()
            }

            // Add course button
            Button(
                onClick = { vm.setShowAddCourseAlert(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = ResColor.swiftBlue),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = timeRemaining <= 0
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add a course", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            }

            // Rate-limit message (if any)
            if (message != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleShadow(),
                    shape = RoundedCornerShape(25.dp),
                    color = colorResource(id = ResColor.sub)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = message!!,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colorResource(id = ResColor.mainOpp)
                        )
                        if (timeRemaining > 0) {
                            Text(
                                text = "Try again in ${ceil(timeRemaining).toInt().coerceAtLeast(0)} seconds",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorResource(id = ResColor.subOpp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

@Composable
private fun UserAvatar(photoURL: String?, onClick: () -> Unit) {
    if (photoURL != null) {
        AsyncImage(
            model = photoURL,
            contentDescription = "User photo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onClick() }
        )
    } else {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .subtleShadow(elevation = 4.dp, shape = CircleShape)
                .clickable { onClick() },
            shape = CircleShape,
            color = colorResource(id = ResColor.sub)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colorResource(id = ResColor.mainOpp)
                )
            }
        }
    }
}

@Composable
private fun RateLimitBanner(message: String, timeRemaining: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = if (timeRemaining > 0) colorResource(id = ResColor.swiftBlue).copy(alpha = 0.1f)
                else colorResource(id = ResColor.swiftRed).copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (timeRemaining > 0) colorResource(id = ResColor.swiftBlue) else colorResource(id = ResColor.swiftRed),
                textAlign = TextAlign.Center
            )
            if (timeRemaining > 0) {
                Text(
                    text = "Try again in ${ceil(timeRemaining).toInt().coerceAtLeast(0)} seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = ResColor.subOpp)
                )
            }
        }
    }
}

@Composable
private fun GetPasswordButton() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = colorResource(id = ResColor.sub),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, AppLinks.coursePasswordUrl.toUri())
            context.startActivity(intent)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = colorResource(id = ResColor.mainOpp)
            )
            Text(
                text = "Get another course password",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
                color = colorResource(id = ResColor.mainOpp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorResource(id = ResColor.mainOpp)
            )
        }
    }
}

@Composable
private fun FirstCourseFeature(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colorResource(id = ResColor.mainOpp)
        )
    }
}

// ---------------------------------------------------------------------------
// Add Course dialog (replaces iOS .alert with TextField)
// ---------------------------------------------------------------------------
@Composable
private fun AddCourseDialog(
    vm: CourseListViewModel,
    hasCourse: Boolean,
    onFirstCourseAdded: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AppAlertDialog(
        onDismissRequest = { vm.setShowAddCourseAlert(false) },
        title = "Add Course",
        message = "Enter course password to begin.",
        confirmText = "Add",
        onConfirm = {
            val wasFirstCourse = !hasCourse
            vm.tryPassword { success ->
                if (success) {
                    vm.setPassword("")
                    password = ""
                    vm.setShowAddCourseAlert(false)
                    // The very first course gets a guided tour of what it unlocked
                    if (wasFirstCourse) onFirstCourseAdded()
                }
            }
        },
        confirmEnabled = password.isNotEmpty(),
        dismissText = "Cancel",
        onDismiss = {
            vm.setPassword("")
            password = ""
            vm.setShowAddCourseAlert(false)
        },
        content = {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    vm.setPassword(it)
                },
                label = { Text("Password") },
                singleLine = true,
                shape = AppTextFieldShape,
                colors = appTextFieldColors()
            )
        }
    )
}


