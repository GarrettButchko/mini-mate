package com.garrettbutchko.minimate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.ContentView
import com.garrettbutchko.minimate.content.ViewState
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.managers.ViewType
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.CourseListViewModel
import com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.signInWithGoogle
import com.garrettbutchko.minimate.content.MiniMateDarkColorScheme
import com.garrettbutchko.minimate.content.MiniMateLightColorScheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val viewManager: ViewManager = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    val courseListVM: CourseListViewModel = koinInject()
    val analyticsVM: AnalyticsViewModel = koinInject()
    val leaderboardVM: LeaderBoardViewModel = koinInject()
    val courseSettingsVM: CourseSettingsViewModel = koinInject()

    val context = LocalContext.current
    val webClientId = stringResource(com.garrettbutchko.minimate.admin.R.string.default_web_client_id)

    val viewType by viewManager.currentView.collectAsState()
    val currentUserId by authViewModel.currentUserIdentifierFlow.collectAsState()
    val userModel by authViewModel.userModel.collectAsState()
    val isLoadingUser by authViewModel.isLoadingUser.collectAsState()
    var initialLoadComplete by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        val userId = currentUserId
        if (userId == null) {
            authViewModel.setLoading(false)
            initialLoadComplete = true
            return@LaunchedEffect
        }

        authViewModel.setLoading(true)
        try {
            val loadedUser = authViewModel.userRepository.loadOrCreateUser(
                id = userId
            ) { refreshedModel ->
                authViewModel.setUserModel(refreshedModel)
            }
            authViewModel.setUserModel(loadedUser)
            
            // If the user has exactly one course, navigate directly to it.
            // If they have multiple, load the list.
            val adminCount = loadedUser.adminCourses.size
            if (adminCount > 1) {
                courseListVM.getCourses()
            } else if (adminCount == 1) {
                courseListVM.getCourse {
                    viewManager.navigateToCourseTab(tab = 1)
                }
            } else {
                courseListVM.setLoadingCourse(false)
            }
        } catch (e: Exception) {
            authViewModel.setUserModel(userModel)
        } finally {
            initialLoadComplete = true
            authViewModel.setLoading(false)
        }
    }

    val colorScheme = if (isSystemInDarkTheme()) MiniMateDarkColorScheme else MiniMateLightColorScheme
    
    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val viewState = when (val vt = viewType) {
                is ViewType.SignIn -> ViewState.SignIn
                is ViewType.Welcome -> ViewState.Welcome
                is ViewType.CourseList -> ViewState.CourseList
                is ViewType.CourseTab -> ViewState.CourseTab(vt.tab)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                ContentView(
                    viewState = viewState,
                    courseListVM = courseListVM,
                    analyticsVM = analyticsVM,
                    leaderboardVM = leaderboardVM,
                    courseSettingsVM = courseSettingsVM,
                    photoURL = userModel?.photoURL,
                    onNavigateToCourseTab = { tab -> viewManager.navigateToCourseTab(tab) },
                    onNavigateToCourseList = { viewManager.navigateToCourseList() },
                    onNavigateToWelcome = { viewManager.navigateToWelcome() },
                    authViewModel = authViewModel,
                    onSignInWithGoogle = {
                        authViewModel.signInWithGoogle(context, webClientId) { result, photoUrl ->
                            authViewModel.handleSignInResult(
                                result = result,
                                photoUrl = photoUrl,
                                onErrorMessage = { _, _ -> },
                                onClearGuestGame = { }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoadingUser && !initialLoadComplete && userModel == null) {
                    LoadingOverlay()
                }
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LogoLoadingView()
                Text("Loading your Mini Mate Manager data...")
            }
        }
    }
}
