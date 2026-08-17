package com.garrettbutchko.minimate.content.tabviews.settings

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.AppTextFieldShape
import com.garrettbutchko.minimate.content.helper.appTextFieldColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.helper.ButtonColors
import com.garrettbutchko.minimate.shared.R.color as ResColor
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
import com.garrettbutchko.minimate.content.helper.PhotoPicker
import com.garrettbutchko.minimate.content.helper.SectionCard
import com.garrettbutchko.minimate.content.helper.StepperControl
import com.garrettbutchko.minimate.viewModelsAndroidAddOns.uploadAdImageAndroid
import com.garrettbutchko.minimate.viewModelsAndroidAddOns.uploadLogoImageAndroid
import com.garrettbutchko.minimate.content.game.GameReviewView
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.courseModels.SocialLink
import com.garrettbutchko.minimate.dataModels.courseModels.SocialPlatform
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.holeModels.Hole
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.viewModels.ColorAddTarget
import com.garrettbutchko.minimate.viewModels.ColorDeleteTarget
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel
import com.garrettbutchko.minimate.generateUUID
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor

@Composable
private fun appSwitchColors(swiftBlue: Color, mainOpp: Color) = SwitchDefaults.colors(
    checkedTrackColor = swiftBlue,
    checkedThumbColor = Color.White,
    checkedBorderColor = Color.Transparent,
    uncheckedTrackColor = mainOpp.copy(alpha = 0.15f),
    uncheckedBorderColor = mainOpp.copy(alpha = 0.4f),
    uncheckedThumbColor = mainOpp.copy(alpha = 0.6f),
    disabledCheckedTrackColor = swiftBlue.copy(alpha = 0.38f),
    disabledCheckedThumbColor = Color.White.copy(alpha = 0.38f),
    disabledUncheckedTrackColor = mainOpp.copy(alpha = 0.06f),
    disabledUncheckedBorderColor = mainOpp.copy(alpha = 0.2f),
    disabledUncheckedThumbColor = mainOpp.copy(alpha = 0.3f),
)

// CourseSettingsViewModel is Koin `factory`-scoped (mirrors iOS, which creates a fresh
// instance whenever CourseTabView remounts, so transient UI state like password fields
// doesn't leak between visits). CourseSettingsView resolves it exactly once via
// koinInject() and shares that single instance with its section composables through this
// CompositionLocal — calling koinInject() again in each section would each yield a
// different factory instance and desync their state.
private val LocalCourseSettingsViewModel = staticCompositionLocalOf<CourseSettingsViewModel> {
    error("LocalCourseSettingsViewModel not provided — must be read within CourseSettingsView")
}

/**
 * CourseSettingsView
 *
 * Settings screen — mirrors iOS CourseSettingsView Form with tier-gated sections.
 * iOS Form → Column inside a verticalScroll (no direct Material3 Form equivalent).
 *
 * Note: Logo upload (iOS PhotoPicker) and Ad image upload are stubbed —
 * Android-side image picker integration needs to be wired separately.
 *
 * Note: iOS NumberPickerView (wheel-style number picker) → simple +/- buttons here
 * as Android has no built-in wheel picker in M3.
 */
@Composable
fun CourseSettingsView(
    modifier: Modifier = Modifier
) {
    val courseListVM: CourseListViewModel = koinInject()
    val courseSettingsVM: CourseSettingsViewModel = koinInject()
    val course by courseListVM.selectedCourse.collectAsState()
    val removingAdmin by courseListVM.removingAdmin.collectAsState()
    val bgColor = colorResource(id = ResColor.bg)

    CompositionLocalProvider(LocalCourseSettingsViewModel provides courseSettingsVM) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                CourseSettingsHeaderView()

                course?.let { currentCourse ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val tier = currentCourse.tier

                        if (tier >= 1) {
                            SectionCard(title = "Course") { CourseSectionContent(course = currentCourse) }
                            SectionCard(title = "Admins") { AdminsSectionContent() }
                            SectionCard(title = "Password") { PasswordSectionContent(course = currentCourse) }
                            SectionCard(title = "Social Links") { SocialLinksSectionContent(course = currentCourse) }
                        }

                        if (tier >= 2) {
                            SectionCard(title = "Appearance") { AppearanceSectionContent(course = currentCourse) }
                            SectionCard(title = "Ad") { AdSectionContent(course = currentCourse) }
                            SectionCard(title = "Leaderboard") { LeaderboardSectionContent(course = currentCourse) }
                        }

                        ParConfigurationSectionContent(course = currentCourse)

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            LoadingOverlay(visible = removingAdmin, message = "Removing admin...")
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseSettingsHeaderView() {
    val vm = LocalCourseSettingsViewModel.current
    val courseListVM: CourseListViewModel = koinInject()
    val showReviewSheet by vm.showReviewSheet.collectAsState()
    val course by courseListVM.selectedCourse.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colorResource(id = ResColor.mainOpp),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { vm.setShowReviewSheet(true) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = colorResource(id = ResColor.swiftBlue),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("User View", color = colorResource(id = ResColor.swiftBlue))
            }
        }
    }

    if (showReviewSheet) {
        course?.let { currentCourse ->
            val holeCount = if (currentCourse.pars.isEmpty()) 18 else currentCourse.pars.size
            val holes1 = remember(holeCount) { (1..holeCount).map { n -> Hole(number = n, strokes = (1..6).random()) } }
            val holes2 = remember(holeCount) { (1..holeCount).map { n -> Hole(number = n, strokes = (1..6).random()) } }

            ModalBottomSheet(
                onDismissRequest = { vm.setShowReviewSheet(false) },
                sheetState = sheetState,
                containerColor = colorResource(id = ResColor.bg)
            ) {
                GameReviewView(
                    game = Game(
                        id = "EXAMPLE",
                        hostUserId = "admin",
                        completed = true,
                        numberOfHoles = holeCount,
                        started = true,
                        dismissed = false,
                        live = false,
                        courseID = currentCourse.id,
                        locationName = currentCourse.name,
                        players = listOf(
                            Player(id = "1", userId = "Example 1", name = "Garrett", holes = holes1),
                            Player(id = "2", userId = "Example 2", name = "Joey", holes = holes2)
                        )
                    ),
                    showBackToStatsButton = true,
                    isInCourseSettings = true,
                    onDismiss = { vm.setShowReviewSheet(false) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Course section
// ---------------------------------------------------------------------------
@Composable
private fun CourseSectionContent(course: Course) {
    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    InfoRow(label = "Id", value = course.id, mainOppColor = mainOpp, subOppColor = subOpp)
    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))
    InfoRow(label = "Name", value = course.name, mainOppColor = mainOpp, subOppColor = subOpp)
    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))
    InfoRow(label = "Tier", value = "${course.tier}", mainOppColor = mainOpp, subOppColor = subOpp)
}

// ---------------------------------------------------------------------------
// Admins section
// ---------------------------------------------------------------------------
@Composable
private fun AdminsSectionContent() {
    val courseListVM: CourseListViewModel = koinInject()
    val adminUsers by courseListVM.adminUsers.collectAsState()
    val loadingAdmins by courseListVM.loadingAdmins.collectAsState()
    var userIdToDelete by remember { mutableStateOf<String?>(null) }
    // Removed rows animate out before the VM drops them from the list
    var removingIds by remember { mutableStateOf(emptySet<String>()) }
    val scope = rememberCoroutineScope()

    // Keep the row hidden until the VM actually drops it from the list; only
    // then clear the id. Clearing early made the row flicker back in while the
    // removal network call was still in flight.
    LaunchedEffect(adminUsers) {
        val currentIds = adminUsers.map { it.googleId }.toSet()
        removingIds = removingIds.intersect(currentIds)
    }

    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    val swiftRed = colorResource(id = ResColor.swiftRed)

    if (loadingAdmins) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = colorResource(id = ResColor.swiftBlue))
        }
    } else {
        Column {
            adminUsers.forEachIndexed { index, user ->
                key(user.googleId) {
                    AnimatedVisibility(
                        visible = user.googleId !in removingIds,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val photoUrl = user.photoURL
                                if (!photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Admin avatar",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Admin avatar",
                                        tint = subOpp,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val isYou = user.email == courseListVM.currentUserEmail
                                    Text(
                                        text = user.name + if (isYou) " (you)" else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = mainOpp
                                    )
                                    Text(
                                        text = user.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subOpp
                                    )
                                }
                                IconButton(onClick = { userIdToDelete = user.googleId }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove admin",
                                        tint = swiftRed
                                    )
                                }
                            }
                            if (index < adminUsers.lastIndex) {
                                HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (userIdToDelete != null) {
        AppAlertDialog(
            onDismissRequest = { userIdToDelete = null },
            title = "Remove Admin?",
            message = "This will remove their admin access.",
            confirmText = "Remove",
            onConfirm = {
                val removedId = userIdToDelete!!
                removingIds = removingIds + removedId
                userIdToDelete = null
                scope.launch {
                    delay(300.milliseconds)
                    courseListVM.removeAdmin(removedId)
                }
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { userIdToDelete = null }
        )
    }
}

// ---------------------------------------------------------------------------
// Password section
// ---------------------------------------------------------------------------
@Composable
private fun PasswordSectionContent(course: Course) {
    val vm = LocalCourseSettingsViewModel.current
    val courseListVM: CourseListViewModel = koinInject()
    var showPassword by remember { mutableStateOf(false) }
    var showChangeDialog by remember { mutableStateOf(false) }
    var showCopyMenu by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val copyScope = rememberCoroutineScope()

    val newPassword by vm.newPassword.collectAsState()
    val confirmPassword by vm.confirmPassword.collectAsState()
    val isValidPassword = newPassword.isNotEmpty() && newPassword == confirmPassword

    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    val swiftBlue = colorResource(id = ResColor.swiftBlue)

    // Long-press the row for "Copy Password" — the same affordance as iOS's
    // context menu, so the password can be shared without revealing it first.
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showCopyMenu = true }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Password:", style = MaterialTheme.typography.bodyMedium, color = mainOpp)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (showPassword) course.password else "••••••••••",
                style = MaterialTheme.typography.bodyMedium,
                color = subOpp
            )
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPassword) "Hide" else "Show",
                    tint = swiftBlue
                )
            }
        }

        DropdownMenu(expanded = showCopyMenu, onDismissRequest = { showCopyMenu = false }) {
            DropdownMenuItem(
                text = { Text("Copy Password") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                    copyScope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("Course password", course.password))
                        )
                    }
                    showCopyMenu = false
                }
            )
        }
    }

    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

    Button(
        onClick = { showChangeDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = swiftBlue, contentColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Text("Change Password")
    }

    if (showChangeDialog) {
        AppAlertDialog(
            onDismissRequest = {
                vm.resetPasswordFields()
                showChangeDialog = false
            },
            title = "Change Password",
            message = "Enter and confirm your new password",
            confirmText = "Save",
            onConfirm = {
                vm.changePassword(course = course, userID = "admin", onCourseUpdated = { courseListVM.setCourse(it) })
                showChangeDialog = false
            },
            confirmEnabled = isValidPassword,
            dismissText = "Cancel",
            onDismiss = {
                vm.resetPasswordFields()
                showChangeDialog = false
            },
            content = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { vm.setNewPassword(it) },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = AppTextFieldShape,
                    colors = appTextFieldColors()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { vm.setConfirmPassword(it) },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = AppTextFieldShape,
                    colors = appTextFieldColors()
                )
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Social Links section
// ---------------------------------------------------------------------------
@Composable
private fun SocialLinksSectionContent(course: Course) {
    val courseListVM: CourseListViewModel = koinInject()
    val links = course.socialLinks
    var visibleIds by remember { mutableStateOf(links.map { it.id }.toSet()) }

    LaunchedEffect(links) {
        val currentIds = links.map { it.id }.toSet()
        if (visibleIds != currentIds) {
            visibleIds = currentIds
        }
    }

    Column(
        modifier = Modifier.animateContentSize()
    ) {
        links.forEachIndexed { index, link ->
            key(link.id) {
                AnimatedVisibility(
                    visible = link.id in visibleIds,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SocialLinkRow(
                        index = index,
                        platform = link.platform,
                        url = link.url,
                        onPlatformChange = { courseListVM.updateSocialPlatform(index, it) },
                        onUrlChange = { url -> courseListVM.updateSocialLinkUrl(index, url) },
                        onDelete = {
                            visibleIds = visibleIds - link.id
                            courseListVM.removeSocialLink(link.id, delayMs = 300)
                        }
                    )
                    if (index != (links.size - 1)) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (course.socialLinks.size < 5) {
            val swiftBlue = colorResource(id = ResColor.swiftBlue)
            Button(
                onClick = {
                    courseListVM.addSocialLink(SocialLink(id = generateUUID(), platform = SocialPlatform.DEFAULT, url = ""))
                },
                colors = ButtonDefaults.buttonColors(containerColor = swiftBlue, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Social Link")
            }
        }
    }
}

@Composable
private fun SocialLinkRow(
    index: Int,
    platform: SocialPlatform,
    url: String,
    onPlatformChange: (SocialPlatform) -> Unit,
    onUrlChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val platforms = SocialPlatform.entries

    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subColor = colorResource(id = ResColor.sub)
    val subThreeOpp = colorResource(id = ResColor.subThreeOpp)
    val swiftBlue = colorResource(id = ResColor.swiftBlue)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Platform ${index + 1}", style = MaterialTheme.typography.bodyMedium, color = mainOpp, modifier = Modifier.weight(1f))

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(platform.name.lowercase().replaceFirstChar { it.uppercase() }, color = swiftBlue)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = swiftBlue)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    platforms.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { onPlatformChange(p); expanded = false }
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete link", tint = colorResource(id = ResColor.swiftRed))
            }
        }

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            shape = AppTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = subColor,
                unfocusedContainerColor = subColor,
                focusedTextColor = mainOpp,
                unfocusedTextColor = mainOpp,
                focusedLabelColor = mainOpp,
                unfocusedLabelColor = subThreeOpp,
                cursorColor = swiftBlue,
                focusedBorderColor = swiftBlue,
                unfocusedBorderColor = subThreeOpp
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Appearance section
// ---------------------------------------------------------------------------
@Composable
private fun AppearanceSectionContent(course: Course) {
    val vm = LocalCourseSettingsViewModel.current
    val courseListVM: CourseListViewModel = koinInject()
    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    val context = LocalContext.current
    val showingLogoPicker by vm.showingPickerLogo.collectAsState()

    // ── Logo row ──────────────────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Logo:", style = MaterialTheme.typography.bodyMedium, color = mainOpp)
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tappable thumbnail / placeholder — matches iOS LogoSectionView tap target
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(mainOpp.copy(alpha = 0.08f))
                    .clickable { vm.setShowingPickerLogo(true) },
                contentAlignment = Alignment.Center,
            ) {
                if (course.logo != null) {
                    AsyncImage(
                        model = course.logo,
                        contentDescription = "Course logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add logo",
                        tint = subOpp,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            // Red X delete button — only when logo exists (mirrors iOS xmark.circle.fill)
            if (course.logo != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                        .clip(CircleShape)
                        .clickable { vm.deleteLogoImage(course) { courseListVM.setCourse(it) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove logo",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }

    // Photo picker — launches when showingLogoPicker becomes true
    PhotoPicker(
        show = showingLogoPicker,
        onImagePicked = { uri ->
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    vm.uploadLogoImageAndroid(bytes, course) { courseListVM.setCourse(it) }
                }
            }
        },
        onDismiss = { vm.setShowingPickerLogo(false) },
    )

    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

    // ── Scorecard Color row ───────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Scorecard Color:", style = MaterialTheme.typography.bodyMedium, color = mainOpp)
        Spacer(modifier = Modifier.weight(1f))
        ColorHolderView(
            color = course.scoreCardColorDT?.toColor(),
            showFunction = {
                courseListVM.setAddTarget(ColorAddTarget.SCORE_CARD_COLOR)
                courseListVM.setShowColor(true)
            },
            deleteFunction = { vm.deleteColor(ColorDeleteTarget.ScoreCardColor, course) { courseListVM.setCourse(it) } },
        )
    }

    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

    // ── Course Colors row ─────────────────────────────────────────────────────
    Column {
        Text("Course Colors", style = MaterialTheme.typography.bodyMedium, color = mainOpp)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = course.courseColorsDT ?: emptyList(),
                key = { _, dt -> dt },
            ) { index, dt ->
                var visible by remember(dt) { mutableStateOf(false) }
                LaunchedEffect(dt) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.animateItem(),
                ) {
                    ColorHolderView(
                        color = dt.toColor(),
                        showFunction = {},
                        deleteFunction = { vm.deleteColor(ColorDeleteTarget.CourseColor(index), course) { courseListVM.setCourse(it) } },
                    )
                }
            }
            item(key = "add_color") {
                ColorHolderView(
                    color = null,
                    showFunction = {
                        courseListVM.setAddTarget(ColorAddTarget.COURSE_COLOR)
                        courseListVM.setShowColor(true)
                    },
                    deleteFunction = {},
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ad section
// ---------------------------------------------------------------------------
@Composable
private fun AdSectionContent(course: Course) {
    val vm = LocalCourseSettingsViewModel.current
    val courseListVM: CourseListViewModel = koinInject()
    var adActive by remember(course.customAdActive) { mutableStateOf(course.customAdActive) }
    val showingAdPicker by vm.showingPickerAd.collectAsState()
    val context = LocalContext.current

    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    val subColor = colorResource(id = ResColor.sub)
    val subThreeOpp = colorResource(id = ResColor.subThreeOpp)
    val swiftBlue = colorResource(id = ResColor.swiftBlue)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Ad Active:", style = MaterialTheme.typography.bodyMedium, color = mainOpp, modifier = Modifier.weight(1f))
        Switch(
            checked = adActive,
            onCheckedChange = { newValue ->
                adActive = newValue
                courseListVM.updateCourseField { it.copy(customAdActive = newValue) }
            },
            colors = appSwitchColors(swiftBlue = swiftBlue, mainOpp = mainOpp)
        )
    }

    if (adActive) {
        var adTitle by remember(course.adTitle) { mutableStateOf(course.adTitle ?: "") }
        var adDesc by remember(course.adDescription) { mutableStateOf(course.adDescription ?: "") }
        var adLink by remember(course.adLink) { mutableStateOf(course.adLink ?: "") }

        OutlinedTextField(
            value = adTitle,
            onValueChange = { text ->
                adTitle = text.take(32)
                courseListVM.updateLimitedCourseField(text, 32, "adTitle") { c, v -> c.copy(adTitle = v) }
            },
            label = { Text("Ad Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = AppTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = subColor,
                unfocusedContainerColor = subColor,
                focusedTextColor = mainOpp,
                unfocusedTextColor = mainOpp,
                focusedLabelColor = mainOpp,
                unfocusedLabelColor = subThreeOpp,
                cursorColor = swiftBlue,
                focusedBorderColor = swiftBlue,
                unfocusedBorderColor = subThreeOpp
            )
        )
        OutlinedTextField(
            value = adDesc,
            onValueChange = { text ->
                adDesc = text.take(40)
                courseListVM.updateLimitedCourseField(text, 40, "adDescription") { c, v -> c.copy(adDescription = v) }
            },
            label = { Text("Ad Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = AppTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = subColor,
                unfocusedContainerColor = subColor,
                focusedTextColor = mainOpp,
                unfocusedTextColor = mainOpp,
                focusedLabelColor = mainOpp,
                unfocusedLabelColor = subThreeOpp,
                cursorColor = swiftBlue,
                focusedBorderColor = swiftBlue,
                unfocusedBorderColor = subThreeOpp
            )
        )
        OutlinedTextField(
            value = adLink,
            onValueChange = { text ->
                adLink = text
                courseListVM.updateOptionalCourseField(text, "adLink") { c, v -> c.copy(adLink = v) }
            },
            label = { Text("Ad Link (https://...)") },
            modifier = Modifier.fillMaxWidth(),
            shape = AppTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = subColor,
                unfocusedContainerColor = subColor,
                focusedTextColor = mainOpp,
                unfocusedTextColor = mainOpp,
                focusedLabelColor = mainOpp,
                unfocusedLabelColor = subThreeOpp,
                cursorColor = swiftBlue,
                focusedBorderColor = swiftBlue,
                unfocusedBorderColor = subThreeOpp
            )
        )
        // Ad image row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Ad Image:", style = MaterialTheme.typography.bodyMedium, color = mainOpp)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(mainOpp.copy(alpha = 0.08f))
                        .clickable { vm.setShowingPickerAd(true) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (course.adImage != null) {
                        AsyncImage(
                            model = course.adImage,
                            contentDescription = "Ad image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add ad image",
                            tint = subOpp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                if (course.adImage != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .clip(CircleShape)
                            .clickable { vm.deleteAdImage(course) { courseListVM.setCourse(it) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove ad image",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        PhotoPicker(
            show = showingAdPicker,
            onImagePicked = { uri ->
                if (uri != null) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        vm.uploadAdImageAndroid(bytes, course) { courseListVM.setCourse(it) }
                    }
                }
            },
            onDismiss = { vm.setShowingPickerAd(false) },
        )
    }
}

// ---------------------------------------------------------------------------
// Leaderboard section
// ---------------------------------------------------------------------------
@Composable
private fun LeaderboardSectionContent(course: Course) {
    val courseListVM: CourseListViewModel = koinInject()
    var lbActive by remember(course.leaderBoardActive) { mutableStateOf(course.leaderBoardActive) }
    val mainOpp = colorResource(id = ResColor.mainOpp)
    val subOpp = colorResource(id = ResColor.subOpp)
    val swiftBlue = colorResource(id = ResColor.swiftBlue)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Leaderboard Active:", style = MaterialTheme.typography.bodyMedium, color = mainOpp, modifier = Modifier.weight(1f))
        Switch(
            checked = lbActive,
            onCheckedChange = { newValue ->
                lbActive = newValue
                courseListVM.updateCourseField { it.copy(leaderBoardActive = newValue) }
            },
            enabled = course.customPar,
            colors = appSwitchColors(swiftBlue = swiftBlue, mainOpp = mainOpp)
        )
    }

    HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

    if (lbActive) {
        Text(
            text = "Players can now see this course's leaderboard and add their score to it if it follows the rules: no profanity, no incomplete games (no 0's)",
            style = MaterialTheme.typography.bodySmall,
            color = subOpp
        )
    } else {
        Text(
            text = "To activate this feature, you need 'Custom Pars' to be turned on.",
            style = MaterialTheme.typography.bodySmall,
            color = subOpp
        )
    }
}

// ---------------------------------------------------------------------------
// Par Configuration section
// ---------------------------------------------------------------------------

/**
 * Mirrors iOS ParConfigurationSectionView.
 *
 * Emits its own SectionCard groups so the call site must NOT wrap this in
 * an outer SectionCard:
 *   - "Par Configuration"  — always visible
 *   - "Par Preview"        — only when customPar && pars.isNotEmpty()
 *   - Show Configuration toggle section
 *   - Per-hole stepper section
 *
 * iOS toggle-on logic: set pars to 18 × 2 and customPar = true.
 * iOS toggle-off logic: show alert; on Reset → deleteLeaderboard() + updateCustomPar(false).
 */
@Composable
private fun ParConfigurationSectionContent(
    course: Course
) {
    val vm = LocalCourseSettingsViewModel.current
    val courseListVM: CourseListViewModel = koinInject()
    val showParConfig by vm.showParConfiguration.collectAsState()
    var showDisableAlert by remember { mutableStateOf(false) }

    val mainOpp  = colorResource(id = ResColor.mainOpp)
    val subOpp   = colorResource(id = ResColor.subOpp)
    val swiftRed = colorResource(id = ResColor.swiftRed)
    val swiftBlue = colorResource(id = ResColor.swiftBlue)
    val subThree = colorResource(id = ResColor.subThreeOpp)

    // ── "Par Configuration" section ──────────────────────────────────────────
    SectionCard(title = "Par Configuration") {

        // Custom Pars toggle
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Custom Pars",
                style = MaterialTheme.typography.bodyMedium,
                color = mainOpp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = course.customPar,
                onCheckedChange = { newValue ->
                    if (!newValue) {
                        // Mirror iOS: show confirmation alert before disabling
                        showDisableAlert = true
                    } else {
                        // Mirror iOS: initialize 18 holes all par-2, set customPar = true
                        courseListVM.updateCustomPar(true)
                    }
                },
                colors = appSwitchColors(swiftBlue = swiftBlue, mainOpp = mainOpp)
            )
        }

        HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

        // Contextual footnote — mirrors iOS footnote text exactly
        if (course.customPar) {
            Text(
                text = "Warning: Turning this off will delete your leaderboard if you have one.",
                style = MaterialTheme.typography.bodySmall,
                color = swiftRed
            )
        } else {
            Text(
                text = "Turning this on will show your course on the minimate map as a supported course and allow you to set custom pars for each hole. It will also enable the leaderboard feature, allowing players to submit their scores for this course.",
                style = MaterialTheme.typography.bodySmall,
                color = subOpp
            )
        }
    }

    // ── "Par Preview" + configuration sections — only when customPar && pars not empty ─
    val pars = course.pars
    if (course.customPar && pars.isNotEmpty()) {

        // Par Preview chips (horizontal scroll — mirrors iOS ScrollView(.horizontal))
        SectionCard(title = "Par Preview") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pars.forEachIndexed { index, par ->
                    Column(
                        modifier = Modifier
                            .size(width = 40.dp, height = 50.dp)
                            .background(subThree, RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "H${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "$par",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // "Show Configuration" toggle section
        SectionCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Show Configuration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mainOpp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = showParConfig,
                    onCheckedChange = { vm.setShowParConfiguration(it) },
                    colors = appSwitchColors(swiftBlue = swiftBlue, mainOpp = mainOpp)
                )
            }
        }

        // Per-hole configuration — only visible when showParConfig is true
        if (showParConfig) {
            SectionCard {
                // Number of holes stepper (mirrors iOS NumberPickerView min=9 max=21)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Number Of Holes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mainOpp,
                        modifier = Modifier.weight(2f)
                    )
                    StepperControl(
                        modifier = Modifier.weight(1.5f),
                        value = pars.size,
                        onDecrement = { if (pars.size > 9) courseListVM.updateNumHoles(pars.size - 1) },
                        onIncrement = { if (pars.size < 21) courseListVM.updateNumHoles(pars.size + 1) },
                        textColor = mainOpp,
                        buttonColors = ButtonColors.DIFFERENT(minus = colorResource(id = ResColor.swiftRed), plus = colorResource(id = ResColor.swiftGreen))
                    )
                }

                HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))

                // Per-hole par steppers (mirrors iOS ForEach over pars)
                pars.forEachIndexed { index, par ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hole ${index + 1}:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mainOpp,
                            modifier = Modifier.weight(2f)
                        )
                        StepperControl(
                            modifier = Modifier.weight(1.5f),
                            value = par,
                            onDecrement = { if (par > 0) courseListVM.updatePar(index = index, newPar = par - 1) },
                            onIncrement = { if (par < 10) courseListVM.updatePar(index = index, newPar = par + 1) },
                            textColor = mainOpp,
                            buttonColors = ButtonColors.DIFFERENT(minus = colorResource(id = ResColor.swiftRed), plus = colorResource(id = ResColor.swiftGreen))
                        )
                    }
                    if (index < pars.lastIndex) {
                        HorizontalDivider(color = mainOpp.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }

    // ── Disable-custom-pars confirmation dialog ───────────────────────────────
    // Mirrors iOS .alert("Disable Custom Pars?") with destructive Reset button.
    if (showDisableAlert) {
        AppAlertDialog(
            onDismissRequest = { showDisableAlert = false },
            title = "Disable Custom Pars?",
            message = "Turning this off will permanently delete your leaderboard and reset all hole pars to default. This cannot be undone.",
            confirmText = "Reset",
            onConfirm = {
                // Mirror iOS Reset button: delete leaderboard, then set customPar = false
                courseListVM.deleteLeaderboard()
                courseListVM.updateCustomPar(false)
                showDisableAlert = false
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { showDisableAlert = false }
        )
    }
}

