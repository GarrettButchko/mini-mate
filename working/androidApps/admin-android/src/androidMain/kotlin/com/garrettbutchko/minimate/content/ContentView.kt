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
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.collectAsState
import com.garrettbutchko.minimate.admin.BuildConfig
import com.garrettbutchko.minimate.content.tabviews.MainView
import com.garrettbutchko.minimate.content.tabviews.analytics.AnalyticsView
import com.garrettbutchko.minimate.content.tabviews.settings.CourseSettingsView
import com.garrettbutchko.minimate.content.tabviews.settings.ColorPickerView
import com.garrettbutchko.minimate.content.preViews.WelcomeView
import com.garrettbutchko.minimate.content.preViews.SignInView
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.ColorAddTarget
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.modelKotlinExtensions.toColorName
import kotlinx.coroutines.flow.first
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
    photoURL: String?,
    onNavigateToCourseTab: (Int) -> Unit,
    onNavigateToCourseList: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithApple: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user by authViewModel.userModel.collectAsState()
    val defaultWebClientId = stringResource(AdminR.string.default_web_client_id)

    var showProfileSheet by remember { mutableStateOf(false) }

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
                        onProfileTap = { showProfileSheet = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ViewState.CourseTab -> {
                    CourseTabView(
                        initialTab = state.initialTab,
                        courseListVM = courseListVM,
                        photoURL = photoURL,
                        onNavigateToCourseList = onNavigateToCourseList,
                        onProfileTap = { showProfileSheet = true }
                    )
                }

                is ViewState.Welcome -> {
                    WelcomeView(
                        welcomeText = "Mini Mate Manager",
                        gradientColors = listOf(
                            Color(0xFF0088FF),
                            Color(0xFFCB30E0)
                        )
                    )
                }

                is ViewState.SignIn -> {
                    SignInView(
                        isMiniMateApp = false,
                        authModel = authViewModel,
                        onSignInWithGoogle = onSignInWithGoogle,
                        onSignInWithApple = onSignInWithApple,
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
                    defaultWebClientId = defaultWebClientId,
                    isDebug = BuildConfig.DEBUG,
                    userNameFallback = "Manager",
                    onDismiss = { showProfileSheet = false }
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
    photoURL: String?,
    onNavigateToCourseList: () -> Unit,
    onProfileTap: () -> Unit
) {
    val analyticsVM = LocalAnalyticsViewModel.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val showColor by courseListVM.showColor.collectAsState()
    val addTarget by courseListVM.addTarget.collectAsState()
    val selectedCourse by courseListVM.selectedCourse.collectAsState()

    // Mirror iOS .onAppear for VM.kotlin.start() and loadHealthData
    LaunchedEffect(Unit) {
        courseListVM.loadAdmins()
        courseListVM.loadingAdmins.first { !it }
        if (courseListVM.adminUsers.value.isEmpty()) {
            onNavigateToCourseList()
            return@LaunchedEffect
        }
        courseListVM.start()
        analyticsVM.loadHealthData(course = courseListVM.selectedCourse.value)
    }

    // Navigate back to course list if the user removes themselves as admin
    LaunchedEffect(selectedCourse) {
        if (selectedCourse == null) {
            onNavigateToCourseList()
        }
    }

    val scoreCardColor = selectedCourse?.scoreCardColorDT?.toColor()
    val subColor = Color.subVsColor(makeColor = scoreCardColor)

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
                        photoURL = photoURL,
                        onNavigateToCourseList = onNavigateToCourseList,
                        onProfileTap = onProfileTap,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> CourseSettingsView(
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

        // The tabs render against a course; until one resolves there's nothing
        // meaningful behind them, so cover rather than flash empty state.
        LoadingOverlay(visible = selectedCourse == null, message = "Loading course...")
    }
}
