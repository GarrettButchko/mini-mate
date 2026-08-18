package com.garrettbutchko.minimate.content

import co.touchlab.kermit.Logger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.garrettbutchko.minimate.user.R
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.managers.ViewType
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.repositories.gameRepos.LocalGameRepository
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.AuthFlowCallbacks
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.CourseViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.StatsViewModel
import com.garrettbutchko.minimate.viewModels.signInWithGoogleAndHandleResult
import com.garrettbutchko.minimate.viewModels.signInWithAppleAndHandleResult
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.Color
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.content.courseScreen.CourseScreen
import com.garrettbutchko.minimate.content.game.HostGameView
import com.garrettbutchko.minimate.content.game.ScoreCardGameView
import com.garrettbutchko.minimate.content.helper.InterstitialAdView
import com.garrettbutchko.minimate.content.main.MainContentView
import com.garrettbutchko.minimate.content.preViews.LocationPermissionView
import com.garrettbutchko.minimate.content.preViews.SignInView
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.content.preViews.WelcomeView
import com.garrettbutchko.minimate.content.stats.StatsView
import com.garrettbutchko.minimate.generateUUID
import com.garrettbutchko.minimate.viewModels.GuestData
import com.garrettbutchko.minimate.helper.IAPManager
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val defaultWebClientId = stringResource(R.string.default_web_client_id)
    val appScope = rememberCoroutineScope()

    val viewManager: ViewManager = koinInject()
    val authModel: AuthViewModel = koinInject()
    val gameModel: GameViewModel = koinInject()
    val locationHandler: LocationFinding = koinInject()
    val localGameRepo: LocalGameRepository = koinInject()

    val currentView by viewManager.currentView.collectAsState()
    var guestGameDate by remember { mutableStateOf<String?>(null) }
    val guestGame = remember { mutableStateOf<Game?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var showLocationExplainer by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        locationHandler.requestLocationAccess()
    }

    DisposableEffect(lifecycleOwner, locationHandler) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationHandler.requestLocationAccess()
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        locationHandler.requestLocationAccess()
    }

    // Preserves the previous per-call-site Logger.i() error logging for sign-in
    // failures, now centralized to one place since AuthViewModel surfaces them via
    // authEvent instead of an onErrorMessage lambda per call.
    val authEvent by authModel.authEvent.collectAsState()
    LaunchedEffect(authEvent) {
        val event = authEvent
        if (event != null) {
            Logger.i("Sign-in status: ${event.message ?: "Unknown error"}")
            authModel.consumeAuthEvent()
        }
    }

    LaunchedEffect(currentView) {
        if (currentView is ViewType.SignIn) {
            val game = gameModel.fetchGuestGame()
            guestGame.value = game
            guestGameDate = game?.let { "Saved guest game found" }

            // Explain why we need location before the system dialog appears —
            // once only, and only while the user still has a decision to make.
            if (!context.hasSeenLocationExplainer() && !context.hasLocationPermission()) {
                showLocationExplainer = true
            }
        }
    }

    val colorScheme = if (isSystemInDarkTheme()) MiniMateDarkColorScheme else MiniMateLightColorScheme
    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    when (targetState) {
                        is ViewType.Main -> {
                            (fadeIn(tween(220)) + scaleIn(initialScale = 0.97f, animationSpec = tween(220)))
                                .togetherWith(fadeOut(tween(180)))
                        }

                        else -> {
                            fadeIn(tween(180)).togetherWith(fadeOut(tween(180)))
                        }
                    }
                },
                label = "user-root-navigation"
            ) { targetView ->
                when (targetView) {
                    is ViewType.Main -> {
                        MainTabView(initialSelectedTab = targetView.tab)
                    }

                    ViewType.Welcome -> WelcomeView()

                    is ViewType.ScoreCard -> ScoreCardGameView(
                        isGuest = targetView.isGuest,
                        gameModel = gameModel,
                        authModel = authModel,
                        viewManager = viewManager,
                        modifier = Modifier
                    )

                    is ViewType.Ad -> Box(modifier = Modifier.fillMaxSize()) {
                        InterstitialAdView(
                            adUnitID = "ca-app-pub-8261962597301587/3394145015"
                        ) {
                            if (targetView.isGuest) {
                                viewManager.navigateToSignIn()
                            } else {
                                viewManager.navigateToMain(1)
                            }
                        }
                    }

                    ViewType.SignIn -> SignInView(
                        isMiniMateApp = true,
                        authModel = authModel,
                        onSignInWithGoogle = {
                            if (defaultWebClientId.isBlank()) {
                                Logger.i("Missing default_web_client_id string; Google sign-in not configured.")
                            } else {
                                authModel.signInWithGoogleAndHandleResult(
                                    context = context,
                                    serverClientId = defaultWebClientId,
                                    callbacks = AuthFlowCallbacks(
                                        onClearGuestGame = {
                                            appScope.launch {
                                                localGameRepo.deleteGuestGame()
                                            }
                                        }
                                    )
                                )
                            }
                        },
                        onSignInWithApple = {
                            authModel.signInWithAppleAndHandleResult(
                                context = context,
                                guestGame = guestGame.value,
                                callbacks = AuthFlowCallbacks(
                                    onClearGuestGame = {
                                        appScope.launch {
                                            localGameRepo.deleteGuestGame()
                                        }
                                    }
                                )
                            )
                        },
                        onCreateGuestGame = { name, email ->
                            gameModel.createGame(
                                online = false,
                                guestData = GuestData(
                                    id = generateUUID(),
                                    email = email.ifBlank { null },
                                    name = name
                                )
                            )
                            viewManager.navigateToHost(isGuest = true)
                        },
                        guestGameDate = guestGameDate,
                        guestGame = guestGame.value
                    )

                    is ViewType.Host -> HostGameView(
                        gameModel = gameModel,
                        locationHandler = locationHandler,
                        onDismiss = {
                            appScope.launch {
                                viewManager.navigateToSignIn() // Navigate back to main tab
                            }
                        },
                        isGuest = true
                    )
                }
            }

            if (showLocationExplainer) {
                ModalBottomSheet(
                    onDismissRequest = {
                        context.markLocationExplainerSeen()
                        showLocationExplainer = false
                    },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    LocationPermissionView(
                        onAllow = {
                            context.markLocationExplainerSeen()
                            showLocationExplainer = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onSkip = {
                            context.markLocationExplainerSeen()
                            showLocationExplainer = false
                        }
                    )
                }
            }
        }
    }
}

private const val LOCATION_PREFS = "minimate_location_prefs"
private const val SEEN_LOCATION_EXPLAINER_KEY = "hasSeenLocationExplainer"

private fun Context.hasSeenLocationExplainer(): Boolean =
    getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE)
        .getBoolean(SEEN_LOCATION_EXPLAINER_KEY, false)

private fun Context.markLocationExplainerSeen() {
    getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE)
        .edit {
            putBoolean(SEEN_LOCATION_EXPLAINER_KEY, true)
        }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
private fun MainTabView(
    initialSelectedTab: Int
) {
    val viewManager: ViewManager = koinInject()
    val authModel: AuthViewModel = koinInject()
    val gameModel: GameViewModel = koinInject()
    val gameManager: GameManager = koinInject()
    val localGameRepo: LocalGameRepository = koinInject()
    val locationHandler: LocationFinding = koinInject()
    val statsViewModel: StatsViewModel = koinInject()
    val courseViewModel: CourseViewModel = koinInject()

    val context = LocalContext.current
    val appScope = rememberCoroutineScope()
    val iapManager = remember(context.applicationContext, appScope) {
        IAPManager(context.applicationContext, appScope)
    }

    val currentUserId by authModel.currentUserIdentifierFlow.collectAsState()
    val userGames by statsViewModel.allGames.collectAsState()
    val isRefreshing by statsViewModel.isRefreshing.collectAsState()
    val hasConnection = NetworkChecker.shared.isConnected

    var selectedTab by remember { mutableIntStateOf(initialSelectedTab) }
    var loadedTabs by remember { mutableStateOf(setOf(1)) }
    LaunchedEffect(initialSelectedTab) {
        selectedTab = initialSelectedTab
    }

    LaunchedEffect(selectedTab) {
        loadedTabs = loadedTabs + selectedTab
    }

    LaunchedEffect(hasConnection) {
        if (!hasConnection && selectedTab == 2) {
            selectedTab = 1
        }
    }

    LaunchedEffect(Unit) {
        locationHandler.requestLocationAccess()
        gameManager.onAppear()
    }

    LaunchedEffect(iapManager) {
        iapManager.initialize {
            iapManager.checkIsPurchasedPro(authModel)
        }
    }

    LaunchedEffect(currentUserId) {
        val userId = currentUserId

        if (userId == null) {
            authModel.setLoading(false)
            return@LaunchedEffect
        }

        authModel.setLoading(true)

        try {
            authModel.bootstrapUserSession(userId) ?: return@LaunchedEffect

            gameManager.onAppear()
            localGameRepo.deleteAllUnusedGames()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.i("Failed to bootstrap user data on Android: ${e.message}")
        } finally {
            authModel.setLoading(false)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            statsViewModel.onAppear()
        }
        if (selectedTab == 2) {
            courseViewModel.onAppearance()
        }
    }

    val selectedCourse by courseViewModel.selectedCourse.collectAsState()
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
        Column(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Stats", color = MaterialTheme.colorScheme.onSurface) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.QueryStats,
                                    contentDescription = "Stats",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = customNavColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Home", color = MaterialTheme.colorScheme.onSurface) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Home",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = customNavColors
                        )
                        if (hasConnection) {
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = { Text("Courses", color = MaterialTheme.colorScheme.onSurface) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Directions,
                                        contentDescription = "Courses",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = customNavColors
                            )
                        }
                    }
                }
            ) { innerPadding ->
                // Use innerPadding to ensure content doesn't get cut off by the bar
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (selectedTab) {
                        0 -> LazyTabContent(
                            0,
                            loadedTabs,
                            onLoaded = { loadedTabs = loadedTabs + it }) {
                            StatsView(
                                userGames = userGames,
                                isRefreshing = isRefreshing,
                                authModel = authModel,
                                viewManager = viewManager,
                                gameManager = gameManager,
                                statsViewModel = statsViewModel
                            )
                        }

                        1 -> LazyTabContent(
                            1,
                            loadedTabs,
                            onLoaded = { loadedTabs = loadedTabs + it }) {
                            MainContentView(
                                iapManager = iapManager
                            )
                        }

                        2 -> if (hasConnection) {
                            LazyTabContent(
                                2,
                                loadedTabs,
                                onLoaded = { loadedTabs = loadedTabs + it }) {
                                CourseScreen(courseVM = courseViewModel)
                            }
                        } else {
                            MissingTabConnectionMessage()
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun LazyTabContent(
    tabIndex: Int,
    loadedTabs: Set<Int>,
    onLoaded: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    if (loadedTabs.contains(tabIndex)) {
        content()
    } else {
        LaunchedEffect(Unit) {
            onLoaded(tabIndex)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LogoLoadingView()
        }
    }
}






@Composable
private fun MissingTabConnectionMessage() {
    CenteredInfoCard(
        title = "Courses Unavailable",
        body = "The Courses tab is hidden when the Android network checker reports no connection.",
        primaryActionText = null,
        onPrimaryAction = {}
    )
}

@Composable
private fun CenteredInfoCard(
    title: String,
    body: String,
    primaryActionText: String?,
    onPrimaryAction: () -> Unit,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodyMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (primaryActionText != null) {
                        Button(onClick = onPrimaryAction) {
                            Text(primaryActionText)
                        }
                    }
                    if (secondaryActionText != null && onSecondaryAction != null) {
                        Button(onClick = onSecondaryAction) {
                            Text(secondaryActionText)
                        }
                    }
                }
            }
        }
    }
}
