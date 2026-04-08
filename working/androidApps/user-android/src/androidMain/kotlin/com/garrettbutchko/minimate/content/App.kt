package com.garrettbutchko.minimate.content

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import org.koin.core.context.GlobalContext
import com.garrettbutchko.minimate.content.courseScreen.CourseScreen
import com.garrettbutchko.minimate.content.game.HostGameView
import com.garrettbutchko.minimate.content.game.JoinGameView
import com.garrettbutchko.minimate.content.game.ScoreCardGameView
import com.garrettbutchko.minimate.content.helper.InterstitialAdView
import com.garrettbutchko.minimate.content.main.MainContentView
import com.garrettbutchko.minimate.content.preViews.SignInView
import com.garrettbutchko.minimate.content.preViews.WelcomeView
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.userModels.UserModel
import com.garrettbutchko.minimate.helper.IAPManager

@Composable
fun App() {
    val context = LocalContext.current
    val appScope = rememberCoroutineScope()
    val koin = remember { GlobalContext.get() }

    val viewManager = remember { koin.get<ViewManager>() }
    val authModel = remember { koin.get<AuthViewModel>() }
    val gameModel = remember { koin.get<GameViewModel>() }
    val hostVM = remember { koin.get<HostViewModel>() }
    val joinVM = remember { koin.get<JoinViewModel>() }
    val gameManager = remember { koin.get<GameManager>() }
    val statsViewModel = remember { koin.get<StatsViewModel>() }
    val courseViewModel = remember { koin.get<CourseViewModel>() }
    val locationHandler = remember { koin.get<LocationFinding>() }
    val localGameRepo = remember { koin.get<LocalGameRepository>() }
    val iapManager = remember(context.applicationContext, appScope) {
        IAPManager(context.applicationContext, appScope)
    }

    val currentView by viewManager.currentView.collectAsState()
    val currentUserId by authModel.currentUserIdentifierFlow.collectAsState()
    val userModel by authModel.userModel.collectAsState()
    val isLoadingUser by authModel.isLoadingUser.collectAsState()

    var initialLoadComplete by remember { mutableStateOf(false) }
    var showHost by remember { mutableStateOf(true) }
    var showJoin by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(iapManager) {
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
            println("Failed to bootstrap user data on Android: ${e.message}")
            authModel.setUserModel(userModel)
        } finally {
            initialLoadComplete = true
            authModel.setLoading(false)
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
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
                        viewManager = viewManager,
                        authModel = authModel,
                        gameModel = gameModel,
                        gameManager = gameManager,
                        iapManager = iapManager,
                        locationHandler = locationHandler,
                        statsViewModel = statsViewModel,
                        courseViewModel = courseViewModel,
                        isLoadingUser = isLoadingUser,
                        initialLoadComplete = initialLoadComplete,
                        currentUserModel = userModel
                    )

                    ViewType.Welcome -> WelcomeView()

                    is ViewType.ScoreCard -> ScoreCardGameView(
                        isGuest = TODO(),
                        gameModel = TODO(),
                        authModel = TODO(),
                        viewManager = TODO(),
                        modifier = TODO()
                    )

                    is ViewType.Ad -> InterstitialAdView() {}

                    ViewType.SignIn -> SignInView(
                        isMiniMateApp = TODO(),
                        onSignInWithGoogle = TODO(),
                        onSignInWithApple = TODO(),
                        onCreateGuestGame = TODO(),
                        guestGameDate = TODO()
                    )

                    ViewType.Host -> HostGameView(
                        showHost = showHost,
                        onShowHostChange = { showHost = it },
                        hostVM = hostVM,
                        gameModel = gameModel,
                        locationHandler = locationHandler,
                        localGameRepo = localGameRepo,
                        onBack = { viewManager.navigateToWelcome() }
                    )

                    ViewType.Join -> JoinGameView(
                        showJoin = showJoin,
                        onShowJoinChange = { showJoin = it },
                        joinVM = joinVM,
                        viewManager = viewManager,
                        gameModel = gameModel,
                        onBack = { viewManager.navigateToMain(1) }
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
    iapManager: IAPManager,
    locationHandler: LocationFinding,
    statsViewModel: StatsViewModel,
    courseViewModel: CourseViewModel,
    isLoadingUser: Boolean,
    initialLoadComplete: Boolean,
    currentUserModel: UserModel?
) {
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
        gameManager.onAppear()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            statsViewModel.onAppear()
        }
        if (selectedTab == 2) {
            courseViewModel.onAppearance()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Stats") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.QueryStats,
                                    contentDescription = "Stats"
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Home") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Home"
                                )
                            }
                        )
                        if (hasConnection) {
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = { Text("Courses") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Directions,
                                        contentDescription = "Courses"
                                    )
                                }
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
                            StatsTabContent(
                                userGames = userGames,
                                isRefreshing = isRefreshing,
                                authModel = authModel,
                                viewManager = viewManager,
                                gameManager = gameManager
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
                                CourseScreen()
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
private fun StatsTabContent(
    userGames: List<Game>,
    isRefreshing: Boolean,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameManager: GameManager
) {
    val user by authModel.userModel.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Stats", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Refreshing: $isRefreshing")
        Text("Games in cache: ${userGames.size}")
        Text("Total games played: ${gameManager.totalGamesPlayed()}")
        Text("Total strokes: ${gameManager.totalStrokes()}")
        Text("Average strokes per game: ${gameManager.averageStrokesPerGame()}")
        Text("Average strokes per hole: ${gameManager.averageStrokesPerHole()}")
        Text("Best game: ${gameManager.bestGameStrokes()?.toString() ?: "N/A"}")
        Text("Worst game: ${gameManager.worstGameStrokes()?.toString() ?: "N/A"}")

        if (user == null) {
            Text("Stats will populate after the signed-in user loads.")
        }

        Button(onClick = { viewManager.navigateToMain(1) }) {
            Text("Back to Home")
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
