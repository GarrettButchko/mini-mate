package com.garrettbutchko.minimate.content

import android.app.Activity
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.garrettbutchko.minimate.content.game.RecapGameView
import com.garrettbutchko.minimate.content.game.ScoreCardGameView
import com.garrettbutchko.minimate.content.main.ProView
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
                        statsViewModel = statsViewModel,
                        courseViewModel = courseViewModel,
                        isLoadingUser = isLoadingUser,
                        initialLoadComplete = initialLoadComplete,
                        currentUserModel = userModel
                    )

                    ViewType.Welcome -> WelcomeScreen(
                        onSignIn = { viewManager.navigateToSignIn() }
                    )

                    is ViewType.ScoreCard -> ScoreCardScreen(
                        isGuest = targetView.isGuest,
                        gameModel = gameModel,
                        authModel = authModel,
                        viewManager = viewManager
                    )

                    is ViewType.Ad -> AdScreen(
                        isGuest = targetView.isGuest,
                        onContinue = {
                            if (targetView.isGuest) {
                                viewManager.navigateToSignIn()
                            } else {
                                viewManager.navigateToMain(1)
                            }
                        }
                    )

                    ViewType.SignIn -> SignInScreen(
                        currentUserId = currentUserId,
                        onBack = { viewManager.navigateToWelcome() },
                        onContinue = { viewManager.navigateAfterSignIn() }
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
    statsViewModel: StatsViewModel,
    courseViewModel: CourseViewModel,
    isLoadingUser: Boolean,
    initialLoadComplete: Boolean,
    currentUserModel: com.garrettbutchko.minimate.dataModels.userModels.UserModel?
) {
    val currentGame by gameModel.game.collectAsState()
    val currentCourse by gameModel.course.collectAsState()
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
                            HomeTabContent(
                                currentGame = currentGame,
                                currentCourse = currentCourse,
                                authModel = authModel,
                                viewManager = viewManager,
                                gameManager = gameManager,
                                iapManager = iapManager
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
private fun WelcomeScreen(onSignIn: () -> Unit) {
    CenteredInfoCard(
        title = "Welcome",
        body = "Sign in to continue to your Home tab and load your games.",
        primaryActionText = "Go to Sign In",
        onPrimaryAction = onSignIn
    )
}

@Composable
private fun SignInScreen(
    currentUserId: String?,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    CenteredInfoCard(
        title = "Sign In",
        body = if (currentUserId == null) {
            "The Android shell is wired to the shared auth model. Add your sign-in form here."
        } else {
            "Signed in as $currentUserId. Continue to the Home tab to load the user data."
        },
        primaryActionText = "Continue",
        onPrimaryAction = onContinue,
        secondaryActionText = "Back",
        onSecondaryAction = onBack
    )
}

@Composable
private fun ScoreCardScreen(
    isGuest: Boolean,
    gameModel: GameViewModel,
    authModel: AuthViewModel,
    viewManager: ViewManager
) {
    ScoreCardGameView(
        isGuest = isGuest,
        gameModel = gameModel,
        authModel = authModel,
        viewManager = viewManager
    )
}

@Composable
private fun AdScreen(
    isGuest: Boolean,
    onContinue: () -> Unit
) {
    CenteredInfoCard(
        title = "Interstitial Ad",
        body = if (isGuest) {
            "Guest flow: continue to sign in after the ad placeholder."
        } else {
            "Signed-in flow: continue back to the Home tab after the ad placeholder."
        },
        primaryActionText = "Continue",
        onPrimaryAction = onContinue
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeTabContent(
    currentGame: com.garrettbutchko.minimate.dataModels.gameModels.Game,
    currentCourse: com.garrettbutchko.minimate.dataModels.courseModels.Course?,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameManager: GameManager,
    iapManager: IAPManager
) {
    val user by authModel.userModel.collectAsState()
    var showProSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Home", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("User: ${user?.name ?: "Not loaded yet"}")
        Text("Games loaded: ${gameManager.totalGamesPlayed()}")
        Text("Current game ID: ${currentGame.id.ifBlank { "No active game" }}")
        Text("Current course: ${currentCourse?.name ?: "No course selected"}")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewManager.navigateToScoreCard() }) {
                Text("Score Card")
            }
            Button(onClick = { viewManager.navigateToAd(isGuest = false) }) {
                Text("Ad Flow")
            }
            Button(onClick = { viewManager.navigateToHost() }) {
                Text("Host")
            }
            Button(onClick = { viewManager.navigateToJoin() }) {
                Text("Join")
            }
        }

        if (NetworkChecker.shared.isConnected && user?.isPro == false) {
            Button(onClick = { showProSheet = true }) {
                Text("Upgrade to Pro")
            }
        }
    }

    if (showProSheet) {
        ModalBottomSheet(onDismissRequest = { showProSheet = false }) {
            ProView(
                showSheet = showProSheet,
                onShowSheetChange = { showProSheet = it },
                authModel = authModel,
                iapManager = iapManager,
                activity = contextAsActivityOrNull()
            )
        }
    }
}

@Composable
private fun contextAsActivityOrNull(): Activity? {
    return LocalContext.current as? Activity
}

@Composable
private fun StatsTabContent(
    userGames: List<com.garrettbutchko.minimate.dataModels.gameModels.Game>,
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
