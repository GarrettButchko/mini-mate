package com.garrettbutchko.minimate.content

import co.touchlab.kermit.Logger

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.CourseViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.HostViewModel
import com.garrettbutchko.minimate.viewModels.StatsViewModel
import com.garrettbutchko.minimate.viewModels.JoinViewModel
import com.garrettbutchko.minimate.viewModels.signInWithGoogle
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import androidx.compose.ui.graphics.Color
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.content.courseScreen.CourseScreen
import com.garrettbutchko.minimate.content.game.HostGameView
import com.garrettbutchko.minimate.content.game.ScoreCardGameView
import com.garrettbutchko.minimate.content.helper.InterstitialAdView
import com.garrettbutchko.minimate.content.main.MainContentView
import com.garrettbutchko.minimate.content.preViews.SignInView
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.content.preViews.WelcomeView
import com.garrettbutchko.minimate.content.stats.StatsView
import com.garrettbutchko.minimate.generateUUID
import com.garrettbutchko.minimate.viewModels.GuestData
import com.garrettbutchko.minimate.helper.IAPManager

private data class AppDependencies(
    val viewManager: ViewManager,
    val authModel: AuthViewModel,
    val gameModel: GameViewModel,
    val hostVM: HostViewModel,
    val joinVM: JoinViewModel,
    val gameManager: GameManager,
    val statsViewModel: StatsViewModel,
    val courseViewModel: CourseViewModel,
    val locationHandler: LocationFinding,
    val localGameRepo: LocalGameRepository
)

@Composable
private fun rememberAppDependencies(): AppDependencies {
    val koin = remember { GlobalContext.get() }
    return remember(koin) {
        AppDependencies(
            viewManager = koin.get(),
            authModel = koin.get(),
            gameModel = koin.get(),
            hostVM = koin.get(),
            joinVM = koin.get(),
            gameManager = koin.get(),
            statsViewModel = koin.get(),
            courseViewModel = koin.get(),
            locationHandler = koin.get(),
            localGameRepo = koin.get()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val defaultWebClientId = stringResource(R.string.default_web_client_id)
    val appScope = rememberCoroutineScope()
    val deps = rememberAppDependencies()

    val currentView by deps.viewManager.currentView.collectAsState()
    var guestGameDate by remember { mutableStateOf<String?>(null) }
    val guestGame = remember { mutableStateOf<Game?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, deps.locationHandler) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                deps.locationHandler.requestLocationAccess()
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        deps.locationHandler.requestLocationAccess()
    }

    LaunchedEffect(currentView) {
        if (currentView is ViewType.SignIn) {
            val game = deps.gameModel.fetchGuestGame()
            guestGame.value = game
            guestGameDate = game?.let { "Saved guest game found" }
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
                    is ViewType.Main -> MainTabView(
                        initialSelectedTab = targetView.tab,
                        viewManager = deps.viewManager,
                        authModel = deps.authModel,
                        gameModel = deps.gameModel,
                        gameManager = deps.gameManager,
                        localGameRepo = deps.localGameRepo,
                        locationHandler = deps.locationHandler,
                        statsViewModel = deps.statsViewModel,
                        courseViewModel = deps.courseViewModel
                    )

                    ViewType.Welcome -> WelcomeView()

                    is ViewType.ScoreCard -> ScoreCardGameView(
                        isGuest = targetView.isGuest,
                        gameModel = deps.gameModel,
                        authModel = deps.authModel,
                        viewManager = deps.viewManager,
                        modifier = Modifier
                    )

                    is ViewType.Ad -> InterstitialAdView(
                        adUnitID = "ca-app-pub-8261962597301587/3394145015"
                    ) {
                        if (targetView.isGuest) {
                            deps.viewManager.navigateToSignIn()
                        } else {
                            deps.viewManager.navigateToMain(1)
                        }
                    }

                    ViewType.SignIn -> SignInView(
                        isMiniMateApp = true,
                        authModel = deps.authModel,
                        onSignInWithGoogle = {
                            val clientId = defaultWebClientId
                            if (clientId.isBlank()) {
                                Logger.i("Missing default_web_client_id string; Google sign-in not configured.")
                            } else {
                                deps.authModel.signInWithGoogle(context, clientId) { result, photoUrl ->
                                    deps.authModel.handleSignInResult(
                                        result = result,
                                        photoUrl = photoUrl,
                                        onErrorMessage = { msg, _ -> Logger.i("Google sign-in error: ${msg ?: "Unknown error"}") },
                                        onClearGuestGame = {
                                            appScope.launch {
                                                deps.localGameRepo.deleteGuestGame()
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        onCreateGuestGame = { name, email ->
                            deps.gameModel.createGame(
                                online = false,
                                guestData = GuestData(
                                    id = generateUUID(),
                                    email = email.ifBlank { null },
                                    name = name
                                )
                            )
                            deps.viewManager.navigateToHost(isGuest = true)
                        },
                        guestGameDate = guestGameDate,
                        guestGame = guestGame.value
                    )

                    is ViewType.Host -> HostGameView(
                        gameModel = deps.gameModel,
                        locationHandler = deps.locationHandler,
                        onDismiss = {
                            appScope.launch {
                                deps.viewManager.navigateToSignIn() // Navigate back to main tab
                            }
                        },
                        isGuest = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTabView(
    initialSelectedTab: Int,
    viewManager: ViewManager,
    authModel: AuthViewModel,
    gameModel: GameViewModel,
    gameManager: GameManager,
    localGameRepo: LocalGameRepository,
    locationHandler: LocationFinding,
    statsViewModel: StatsViewModel,
    courseViewModel: CourseViewModel
) {
    val context = LocalContext.current
    val appScope = rememberCoroutineScope()
    val iapManager = remember(context.applicationContext, appScope) {
        IAPManager(context.applicationContext, appScope)
    }

    val currentUserId by authModel.currentUserIdentifierFlow.collectAsState()
    val currentUserModel by authModel.userModel.collectAsState()
    val isLoadingUser by authModel.isLoadingUser.collectAsState()
    val userGames by statsViewModel.allGames.collectAsState()
    val isRefreshing by statsViewModel.isRefreshing.collectAsState()
    val hasConnection = NetworkChecker.shared.isConnected

    var selectedTab by remember { mutableIntStateOf(initialSelectedTab) }
    var loadedTabs by remember { mutableStateOf(setOf(1)) }
    var initialLoadComplete by remember { mutableStateOf(false) }

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
        iapManager.onProStatusChecked = { hasPro ->
            if (hasPro) {
                val user = authModel.userModel.value
                if (user != null && !user.isPro) {
                    val upgraded = user.copy(isPro = true)
                    authModel.setUserModel(upgraded)
                    appScope.launch {
                        runCatching { authModel.userRepository.saveUnified(upgraded.googleId, upgraded) }
                    }
                }
            }
        }
        iapManager.onPurchaseSuccess = { purchase ->
            val user = authModel.userModel.value
            if (user != null && !user.isPro) {
                val upgraded = user.copy(isPro = true)
                authModel.setUserModel(upgraded)
                appScope.launch {
                    runCatching { authModel.userRepository.saveUnified(upgraded.googleId, upgraded) }
                }
            }
        }
        iapManager.initialize()
    }

    LaunchedEffect(currentUserId) {
        val userId = currentUserId

        if (userId == null) {
            authModel.setLoading(false)
            initialLoadComplete = true
            return@LaunchedEffect
        }

        authModel.setLoading(true)

        try {
            val loadedUser = authModel.userRepository.loadOrCreateUser(
                id = userId
            ) { refreshedModel ->
                authModel.setUserModel(refreshedModel)
            }

            authModel.setUserModel(loadedUser)
            gameManager.onAppear()
            localGameRepo.deleteAllUnusedGames()
        } catch (e: Exception) {
            Logger.i("Failed to bootstrap user data on Android: ${e.message}")
            authModel.setUserModel(currentUserModel)
        } finally {
            initialLoadComplete = true
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
                                authModel = authModel,
                                viewManager = viewManager,
                                gameModel = gameModel,
                                gameManager = gameManager,
                                iapManager = iapManager,
                                locationHandler = locationHandler
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

                    if (isLoadingUser && !initialLoadComplete && currentUserModel == null) {
                        LoadingOverlay()
                    }
                }
            }
        }

        if (isLoadingUser && !initialLoadComplete && currentUserModel == null) {
            LoadingOverlay()
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
            CircularProgressIndicator()
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
                CircularProgressIndicator()
                Text("Loading your Mini Mate data...")
            }
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
