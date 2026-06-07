package com.garrettbutchko.minimate.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.collectAsState
import com.garrettbutchko.minimate.content.tabviews.MainView
import com.garrettbutchko.minimate.content.tabviews.analytics.AnalyticsView
import com.garrettbutchko.minimate.content.tabviews.settings.CourseSettingsView
import com.garrettbutchko.minimate.content.tabviews.settings.ColorPickerView
import com.garrettbutchko.minimate.content.preViews.WelcomeView
import com.garrettbutchko.minimate.content.preViews.SignInView
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.ColorAddTarget
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.reauthenticateWithGoogle
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.admin.R as AdminR

/**
 * ContentView — root composable
 *
 * iOS ContentView uses a ViewManager state machine (courseList / courseTab / welcome / signIn).
 * On Android the nav state is managed by the parent (MainActivity / App composable) which
 * passes the appropriate screen state here. This file handles the CourseTab portion only
 * (the tab bar with Stats / Home / Settings).
 *
 * iOS ViewManager switch → ViewState sealed class passed in.
 * iOS ColorPickerView overlay → rendered inside CourseTabView when showColor is true.
 * iOS EnvironmentObject → explicit ViewModel parameters.
 */

// ---------------------------------------------------------------------------
// ViewState — mirrors iOS onEnum(of: viewManager.currentView)
// ---------------------------------------------------------------------------
sealed class ViewState {
    object CourseList : ViewState()
    data class CourseTab(val initialTab: Int = 1) : ViewState()
    object Welcome : ViewState()
    object SignIn : ViewState()
}

// ---------------------------------------------------------------------------
// ContentView root
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentView(
    viewState: ViewState,
    authViewModel: AuthViewModel,
    courseListVM: CourseListViewModel,
    analyticsVM: AnalyticsViewModel,
    leaderboardVM: LeaderBoardViewModel,
    courseSettingsVM: CourseSettingsViewModel,
    photoURL: String?,
    onNavigateToCourseTab: (Int) -> Unit,
    onNavigateToCourseList: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by authViewModel.userModel.collectAsState()
    val defaultWebClientId = stringResource(AdminR.string.default_web_client_id)

    var showProfileSheet by remember { mutableStateOf(false) }
    var profileBotMessage by remember { mutableStateOf("") }
    var isProfileBotError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedContent(
            targetState = viewState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "rootNavTransition",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                is ViewState.CourseList -> {
                    CourseListView(
                        vm = courseListVM,
                        photoURL = photoURL,
                        hasAdminCourses = !user?.adminCourses.isNullOrEmpty(),
                        onNavigateToCourseTab = onNavigateToCourseTab,
                        onProfileTap = {
                            profileBotMessage = ""
                            isProfileBotError = false
                            showProfileSheet = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ViewState.CourseTab -> {
                    CourseTabView(
                        initialTab = state.initialTab,
                        courseListVM = courseListVM,
                        analyticsVM = analyticsVM,
                        leaderboardVM = leaderboardVM,
                        courseSettingsVM = courseSettingsVM,
                        photoURL = photoURL,
                        onNavigateToCourseList = onNavigateToCourseList,
                        onProfileTap = {
                            profileBotMessage = ""
                            isProfileBotError = false
                            showProfileSheet = true
                        }
                    )
                }

                is ViewState.Welcome -> {
                    WelcomeView(
                        welcomeText = "Mini Mate Manager",
                        gradientColors = listOf(
                            Color(0xFF1F4073),
                            Color(0xFF2E7361)
                        )
                    )
                }

                is ViewState.SignIn -> {
                    SignInView(
                        isMiniMateApp = false,
                        authModel = authViewModel,
                        onSignInWithGoogle = onSignInWithGoogle,
                        onCreateGuestGame = { _, _ -> /* Manager app doesn't use guest games */ }
                    )
                }
            }
        }

        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                ProfileView(
                    isMiniMateApp = false,
                    userProfileImage = user?.photoURL,
                    userName = user?.name ?: "Manager",
                    userEmail = user?.email,
                    userGoogleId = user?.googleId ?: "",
                    userIsPro = user?.isPro ?: false,
                    userAccountType = user?.accountType?.joinToString(",") ?: "",
                    userBallColor = user?.ballColorDT.toColor(),
                    isDebug = false, // TODO: Use BuildConfig.DEBUG when available
                    botMessage = profileBotMessage,
                    isBotMessageError = isProfileBotError,
                    onPhotoChange = {
                        profileBotMessage = "Photo updates are not available here yet."
                        isProfileBotError = true
                    },
                    onGetPro = { /* Admin app doesn't have Pro yet? */ },
                    onEditName = { newName ->
                        val currentUser = user ?: return@ProfileView
                        scope.launch {
                            val updated = currentUser.copy(name = newName)
                            authViewModel.setUserModel(updated)
                            runCatching {
                                authViewModel.userRepository.saveUnified(updated.googleId, updated)
                            }.onSuccess {
                                profileBotMessage = "Name updated."
                                isProfileBotError = false
                            }.onFailure {
                                profileBotMessage = it.message ?: "Unable to update name."
                                isProfileBotError = true
                            }
                        }
                    },
                    onPasswordReset = {
                        val email = user?.email
                        if (email.isNullOrBlank()) {
                            profileBotMessage = "No email found for this account."
                            isProfileBotError = true
                            return@ProfileView
                        }
                        scope.launch {
                            runCatching { authViewModel.authRepository.sendPasswordReset(email) }
                                .getOrElse { Result.failure(it) }
                                .fold(
                                    onSuccess = {
                                        profileBotMessage = "Password reset email sent."
                                        isProfileBotError = false
                                    },
                                    onFailure = {
                                        profileBotMessage = it.message ?: "Could not send reset email."
                                        isProfileBotError = true
                                    }
                                )
                        }
                    },
                    onLogout = {
                        showProfileSheet = false
                        authViewModel.logout()
                        onNavigateToWelcome()
                    },
                    onDeleteAccountGoogle = {
                        scope.launch {
                            authViewModel.reauthenticateWithGoogle(context, defaultWebClientId, loginHint = user?.email) { res ->
                                res.fold(
                                    onSuccess = { credential ->
                                        scope.launch {
                                            authViewModel.deleteAccount(credential).fold(
                                                onSuccess = {
                                                    showProfileSheet = false
                                                    onNavigateToWelcome()
                                                },
                                                onFailure = {
                                                    profileBotMessage = it.message ?: "Unable to delete account."
                                                    isProfileBotError = true
                                                }
                                            )
                                        }
                                    },
                                    onFailure = {
                                        profileBotMessage = it.message ?: "Google re-authentication failed."
                                        isProfileBotError = true
                                    }
                                )
                            }
                        }
                    },
                    onDeleteAccountApple = {
                        profileBotMessage = "Apple account deletion is not available here yet."
                        isProfileBotError = true
                    },
                    onDeleteAccountEmail = { password ->
                        val email = user?.email
                        if (email.isNullOrBlank()) {
                            profileBotMessage = "No email found for this account."
                            isProfileBotError = true
                            return@ProfileView
                        }
                        scope.launch {
                            authViewModel.reauthenticateWithEmail(email, password).fold(
                                onSuccess = { credential ->
                                    authViewModel.deleteAccount(credential).fold(
                                        onSuccess = {
                                            showProfileSheet = false
                                            onNavigateToWelcome()
                                        },
                                        onFailure = {
                                            profileBotMessage = it.message ?: "Unable to delete account."
                                            isProfileBotError = true
                                        }
                                    )
                                },
                                onFailure = {
                                    profileBotMessage = it.message ?: "Re-authentication failed."
                                    isProfileBotError = true
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CourseTabView — the 3-tab bottom-nav view
// Mirrors iOS TabView with Stats (0), Home (1), Settings (2)
// ---------------------------------------------------------------------------
@Composable
private fun CourseTabView(
    initialTab: Int,
    courseListVM: CourseListViewModel,
    analyticsVM: AnalyticsViewModel,
    leaderboardVM: LeaderBoardViewModel,
    courseSettingsVM: CourseSettingsViewModel,
    photoURL: String?,
    onNavigateToCourseList: () -> Unit,
    onProfileTap: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val showColor by courseListVM.showColor.collectAsState()
    val addTarget by courseListVM.addTarget.collectAsState()
    val selectedCourse by courseListVM.selectedCourse.collectAsState()

    // Mirror iOS .onAppear for VM.kotlin.start() and loadHealthData
    LaunchedEffect(Unit) {
        courseListVM.start()
        analyticsVM.loadHealthData(course = courseListVM.selectedCourse.value)
    }

    val scoreCardColor = selectedCourse?.scoreCardColorDT?.toColor()
    val subColor = androidx.compose.ui.graphics.Color.subVsColor(makeColor = scoreCardColor)

    val customNavColors = NavigationBarItemDefaults.colors(
        indicatorColor = subColor,
        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Stats", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Stats", color = MaterialTheme.colorScheme.onSurface) },
                        colors = customNavColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Home", color = MaterialTheme.colorScheme.onSurface) },
                        colors = customNavColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface) },
                        label = { Text("Settings", color = MaterialTheme.colorScheme.onSurface) },
                        colors = customNavColors
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabTransition",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    0 -> AnalyticsView(
                        vm = analyticsVM,
                        course = selectedCourse,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> MainView(
                        course = selectedCourse,
                        analyticsVM = analyticsVM,
                        leaderboardVM = leaderboardVM,
                        photoURL = photoURL,
                        onNavigateToCourseList = onNavigateToCourseList,
                        onProfileTap = onProfileTap,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> CourseSettingsView(
                        courseListVM = courseListVM,
                        courseSettingsVM = courseSettingsVM,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> {}
                }
            }
        }

        // Color Picker Overlay
        ColorPickerView(
            visible = showColor,
            disabledColors = selectedCourse?.courseColorsDT?.mapNotNull { it.toColor() }?.toSet() ?: emptySet(),
            onColorSelected = { color ->
                val colorStr = color.toColorName()
                courseListVM.updateCourseField { course ->
                    when (addTarget) {
                        ColorAddTarget.SCORE_CARD_COLOR -> course.copy(scoreCardColorDT = colorStr)
                        ColorAddTarget.COURSE_COLOR -> course.copy(courseColorsDT = (course.courseColorsDT ?: emptyList()) + colorStr)
                        null -> course
                    }
                }
            },
            onDismiss = { courseListVM.setShowColor(false) },
            notScoreCard = addTarget != ColorAddTarget.SCORE_CARD_COLOR
        )
    }
}

private fun Color.toColorName(): String = when (this) {
    Color(0xFFFF383C) -> "red"
    Color(0xFFFF8D28) -> "orange"
    Color(0xFFFFCC00) -> "yellow"
    Color(0xFF34C759) -> "green"
    Color(0xFF00C8B3) -> "mint"
    Color(0xFF00C3D0) -> "teal"
    Color(0xFF00C0E8) -> "cyan"
    Color(0xFF0088FF) -> "blue"
    Color(0xFF6155F5) -> "indigo"
    Color(0xFFCB30E0) -> "purple"
    Color(0xFFFF2D55) -> "pink"
    Color(0xFFAC7F5E) -> "brown"
    else -> String.format("#%08X", toArgb())
}
