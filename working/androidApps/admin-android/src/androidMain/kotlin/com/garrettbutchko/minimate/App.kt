package com.garrettbutchko.minimate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import com.garrettbutchko.minimate.content.helper.LoadingOverlay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.signInWithGoogle
import com.garrettbutchko.minimate.viewModels.signInWithApple
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
            // Skip the round-trip when the sign-in pipeline already reconciled the
            // user this session — userModel alone can't tell us, since it may just
            // be hydrated from the local cache.
            val syncedUser = authViewModel.userModel.value.takeIf { authViewModel.hasSyncedUserThisSession }
            val loadedUser = syncedUser ?: authViewModel.userRepository.loadOrCreateUser(
                id = userId
            ) { refreshedModel ->
                authViewModel.setUserModel(refreshedModel)
            }.also {
                authViewModel.setUserModel(it)
                authViewModel.markUserSynced()
            }

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
                    photoURL = userModel?.photoURL,
                    onNavigateToCourseTab = { tab -> viewManager.navigateToCourseTab(tab) },
                    onNavigateToCourseList = { viewManager.navigateToCourseList() },
                    onNavigateToSignIn = { viewManager.navigateToSignIn() },
                    authViewModel = authViewModel,
                    onSignInWithGoogle = {
                        authViewModel.signInWithGoogle(context, webClientId) { result, photoUrl, email ->
                            authViewModel.handleSignInResult(
                                result = result,
                                photoUrl = photoUrl,
                                email = email,
                                onErrorMessage = { _, _ -> },
                                onClearGuestGame = { }
                            )
                        }
                    },
                    onSignInWithApple = {
                        authViewModel.signInWithApple(context) { result, name, email, appleId ->
                            authViewModel.handleAppleSignInResult(
                                result = result,
                                name = name,
                                email = email,
                                appleId = appleId,
                                onErrorMessage = { _, _ -> },
                                onClearGuestGame = { }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // The SignIn view shows its own "Signing you in..." overlay, so
                // skip the global one while it owns the screen.
                LoadingOverlay(
                    visible = isLoadingUser && !initialLoadComplete && userModel == null &&
                        viewType !is ViewType.SignIn,
                    message = "Loading your Mini Mate Manager data..."
                )
            }
        }
    }
}
