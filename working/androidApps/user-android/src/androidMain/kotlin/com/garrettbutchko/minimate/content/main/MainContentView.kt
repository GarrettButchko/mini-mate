package com.garrettbutchko.minimate.content.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.content.helper.StatusMessage
import com.garrettbutchko.minimate.content.helper.TransientStatusSignal
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import com.garrettbutchko.minimate.shared.R.color as ResColor
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.garrettbutchko.minimate.user.BuildConfig
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.ProfileView
import com.garrettbutchko.minimate.content.TitleView
import com.garrettbutchko.minimate.content.game.GameReviewView
import com.garrettbutchko.minimate.content.game.HostGameView
import com.garrettbutchko.minimate.content.game.JoinGameView
import com.garrettbutchko.minimate.content.helper.CourseBannerAdView
import com.garrettbutchko.minimate.content.helper.rememberCourseBannerAdVisible
import com.garrettbutchko.minimate.content.helper.InfoButton
import com.garrettbutchko.minimate.content.helper.LogoDefault
import com.garrettbutchko.minimate.content.helper.BarChartView
import com.garrettbutchko.minimate.helper.IAPManager
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.logic.MainViewBusinessLogic
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.courseColors
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.reauthenticateWithApple
import com.garrettbutchko.minimate.viewModels.reauthenticateWithGoogle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.modifiers.subTwoVsColor
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.stats.StatCard
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.extensions.formatted
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import kotlin.time.Duration.Companion.milliseconds


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainContentView(
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameModel: GameViewModel,
    gameManager: GameManager,
    iapManager: IAPManager,
    locationHandler: LocationFinding
) {
    val logic = remember { MainViewBusinessLogic() }
    val context = LocalContext.current
    val defaultWebClientId = stringResource(com.garrettbutchko.minimate.user.R.string.default_web_client_id)
    val user by authModel.userModel.collectAsState()
    val isGuest = user == null
    val course by gameModel.course.collectAsState()
    val userGames by gameManager.userGames.collectAsState()
    val hasLocationAccess by locationHandler.hasLocationAccess.collectAsState()

    var isOnlineMode by remember { mutableStateOf(false) }
    val showProfileSheetState = remember { mutableStateOf(false) }
    val showProSheetState = remember { mutableStateOf(false) }
    var showPromoStage by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isRotating by remember { mutableStateOf(false) }
    var profileBotMessage by remember { mutableStateOf("") }
    var isProfileBotError by remember { mutableStateOf(false) }
    val logoutTint = colorResource(id = ResColor.swiftBlue)

    var reviewGame by remember { mutableStateOf<Game?>(null) }

    val hostSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showHostSheet by remember { mutableStateOf(false) }

    val joinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showJoinSheet by remember { mutableStateOf(false) }

    var onBannerLoaded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val disablePlaying = logic.isPlayDisabled(
        isPro = user?.isPro,
        gameCount = user?.gameIDs?.size ?: 0
    )

    val isBannerAdVisible = rememberCourseBannerAdVisible(
        isGuest = isGuest,
        isPro = user?.isPro,
        isConnected = NetworkChecker.shared.isConnected
    )

    LaunchedEffect(Unit) {
        gameManager.onAppear()
        if (NetworkChecker.shared.isConnected) {
            runCatching { gameModel.setUp() }
        }
    }

    LaunchedEffect(user) {
        gameManager.onAppear()
    }

    LaunchedEffect(userGames.size) {
        if (userGames.isEmpty()) {
            isOnlineMode = false
        }
    }

    LaunchedEffect(showPromoStage) {
        if (showPromoStage) {
            delay((logic.proPromotionDisplayDuration * 1000).toLong().milliseconds)
            showPromoStage = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        MainTopBar(
            greetingName = logic.getGreetingName(user?.name),
            courseLogoUrl = course?.logo,
            profilePhotoUrl = user?.photoURL,
            onProfileTap = {
                profileBotMessage = ""
                isProfileBotError = false
                showProfileSheetState.value = true
            },
            courseBallColors = course?.courseColors
        )



        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 132.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (NetworkChecker.shared.isConnected) {
                        LocationCard(
                            hasLocationAccess = hasLocationAccess,
                            courseName = course?.name,
                            isSearching = isSearching,
                            isRotating = isRotating,
                            onSearchNearby = {
                                scope.launch {
                                    runCatching {
                                        gameModel.searchNearby(
                                            isLoading1 = { isSearching = it },
                                            isLoading2 = { isSearching = it }
                                        )
                                    }
                                }
                            },
                            onRetry = {
                                scope.launch {
                                    runCatching {
                                        gameModel.retry(
                                            firstRotate = { isRotating = it },
                                            secondRotate = { isRotating = it },
                                            isLoading1 = { isSearching = it },
                                            isLoading2 = { isSearching = it }
                                        )
                                    }
                                }
                            },
                            onExit = {
                                isSearching = false
                                isRotating = false
                                gameModel.exit()
                            },
                            makeColor = course?.scoreCardColor
                        )
                    }
                }
                if (logic.shouldShowProStopper(user?.isPro, user?.gameIDs?.size ?: 0)) {
                    item {
                        NoticeCard(
                            message = "You have reached the free limit. Upgrade to Pro to store more than 2 games.",
                            makeColor = course?.scoreCardColor
                        )
                    }
                }


                item {
                    val showCustomAd = course?.customAdActive == true
                    if (showCustomAd || isBannerAdVisible) {
                        CourseBannerAdView(
                            showColor = true,
                            adUnitID = "ca-app-pub-8261962597301587/9726040339",
                            isGuest = isGuest,
                            isPro = user?.isPro,
                            isConnected = NetworkChecker.shared.isConnected,
                            onBannerAdLoaded = { onBannerLoaded = it }
                        )
                        if (showCustomAd || onBannerLoaded) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    LastGameCard(
                        gameManager = gameManager,
                        tintColor = course?.scoreCardColor,
                        onOpenStats = { reviewGame = gameManager.latestGame() }
                    )
                }
            }

            reviewGame?.let { game ->
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                val scope = rememberCoroutineScope()

                ModalBottomSheet(
                    onDismissRequest = {
                        scope.launch {
                            sheetState.hide()
                            reviewGame = null
                        }
                    },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GameReviewView(
                            game = game,
                            showBackToStatsButton = true,
                            onDismiss = {
                                scope.launch {
                                    sheetState.hide()
                                    reviewGame = null
                                }
                            }
                        )
                    }
                }
            }

            if (showHostSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showHostSheet = false },
                    sheetState = hostSheetState,
                    // ModalBottomSheet insets its content by safeDrawing top+bottom
                    // by default, reserving a strip at each edge the content (and
                    // so the scrim living inside it) never reaches. Zero it out so
                    // HostGameView runs edge-to-edge, matching iOS's
                    // .ignoresSafeArea() on its dark overlays.
                    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    HostGameView(
                        onDismiss = { showHostSheet = false },
                        isGuest = false,
                        gameModel = gameModel,
                        locationHandler = locationHandler
                    )
                }
            }

            if (showJoinSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showJoinSheet = false },
                    sheetState = joinSheetState,
                    // Same reasoning as the host sheet above.
                    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    JoinGameView(
                        showJoin = showJoinSheet,
                        onShowJoinChange = { showJoinSheet = it },
                        viewManager = viewManager,
                        gameModel = gameModel,
                        authModel = authModel
                    )
                }
            }

            MainActionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                isOnlineMode = isOnlineMode,
                hasConnection = NetworkChecker.shared.isConnected,
                disablePlaying = disablePlaying,
                infoText = logic.getInfoMessage(isOnlineMode = isOnlineMode),
                title = logic.getHeaderTitle(isOnlineMode = isOnlineMode),
                onBackToOffline = { isOnlineMode = false },
                onQuick = {
                    if (disablePlaying) {
                        showProSheetState.value = true
                    } else {
                        gameModel.createGame(online = false, guestData = null)
                        showHostSheet = true
                    }
                },
                onConnect = {
                    if (disablePlaying) {
                        showProSheetState.value = true
                    } else {
                        isOnlineMode = true
                    }
                },
                onHost = {
                    gameModel.createGame(online = true, guestData = null)
                    showHostSheet = true
                },
                onJoin = {
                    gameModel.resetGame()
                    showJoinSheet = true
                },
                makeColor = course?.scoreCardColor
            )

            if (NetworkChecker.shared.isConnected && user?.isPro == false) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .subtleShadow(elevation = 10.dp)
                            .background(
                                Color(0xFF7B1FA2).copy(alpha = 0.78f),
                                RoundedCornerShape(25.dp)
                            )
                            .clip(RoundedCornerShape(25.dp))
                            .clickable {
                                if (!showPromoStage) {
                                    showPromoStage = true
                                } else {
                                    showProSheetState.value = true
                                }
                            }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimatedVisibility(
                                visible = showPromoStage,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                            ) {
                                Text(
                                    "Tap to buy Pro!",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text("✨")
                        }
                    }
                }
            }
        }
    }

    if (showProfileSheetState.value) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheetState.value = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ProfileView(
                userProfileImage = user?.photoURL,
                userName = user?.name ?: "Player",
                userEmail = user?.email,
                userGoogleId = user?.googleId ?: "",
                userIsPro = user?.isPro ?: false,
                userAccountType = user?.accountType?.joinToString(",") ?: "",
                userBallColor = user?.ballColorDT.toColor(),
                isDebug = BuildConfig.DEBUG,
                botMessage = profileBotMessage,
                isBotMessageError = isProfileBotError,
                onPhotoChange = {
                    profileBotMessage = "Photo updates are not available here yet."
                    isProfileBotError = true
                },
                onGetPro = {
                    showProfileSheetState.value = false
                    showProSheetState.value = true
                },
                onEditName = { newName ->
                    val currentUser = user ?: return@ProfileView
                    scope.launch {
                        val updated = currentUser.copy(name = newName)
                        authModel.setUserModel(updated)
                        runCatching {
                            authModel.userRepository.saveUnified(updated.googleId, updated)
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
                        runCatching { authModel.authRepository.sendPasswordReset(email) }
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
                    // Posted before navigating away — SignInView picks this up via
                    // ConsumeTransientStatusSignal() once it lands there.
                    TransientStatusSignal.pending = StatusMessage(
                        title = "Logged Out",
                        subtitle = "You've been signed out.",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        tint = logoutTint
                    )
                    showProfileSheetState.value = false
                    authModel.logout()
                    viewManager.navigateToSignIn()
                },
                onDeleteAccountGoogle = {
                    scope.launch {
                        authModel.reauthenticateWithGoogle(context, defaultWebClientId, loginHint = user?.email) { res ->
                            res.fold(
                                onSuccess = { credential ->
                                    scope.launch {
                                        authModel.deleteAccount(credential).fold(
                                            onSuccess = {
                                                announceAccountDeleted()
                                                showProfileSheetState.value = false
                                                viewManager.navigateToSignIn()
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
                    scope.launch {
                        authModel.reauthenticateWithApple(context) { res ->
                            res.fold(
                                onSuccess = { credential ->
                                    scope.launch {
                                        authModel.deleteAccount(credential).fold(
                                            onSuccess = {
                                                announceAccountDeleted()
                                                showProfileSheetState.value = false
                                                viewManager.navigateToSignIn()
                                            },
                                            onFailure = {
                                                profileBotMessage = it.message ?: "Unable to delete account."
                                                isProfileBotError = true
                                            }
                                        )
                                    }
                                },
                                onFailure = {
                                    profileBotMessage = it.message ?: "Apple re-authentication failed."
                                    isProfileBotError = true
                                }
                            )
                        }
                    }
                },
                onDeleteAccountEmail = { password ->
                    val email = user?.email
                    if (email.isNullOrBlank()) {
                        profileBotMessage = "No email found for this account."
                        isProfileBotError = true
                        return@ProfileView
                    }

                    scope.launch {
                        authModel.reauthenticateWithEmail(email, password).fold(
                            onSuccess = { credential ->
                                authModel.deleteAccount(credential).fold(
                                    onSuccess = {
                                        announceAccountDeleted()
                                        showProfileSheetState.value = false
                                        viewManager.navigateToSignIn()
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

    if (showProSheetState.value) {
        ModalBottomSheet(
            onDismissRequest = { showProSheetState.value = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ProView(
                iapManager = iapManager,
                onShowSheetChange = { showProSheetState.value = it },
                activity = contextAsActivityOrNull()
            )
        }
    }
}


@Composable
private fun MainTopBar(
    greetingName: String,
    courseLogoUrl: String?,
    courseBallColors: List<Color>?,
    profilePhotoUrl: String?,
    onProfileTap: () -> Unit
) {
    val density = LocalDensity.current
    var titleSize by remember { mutableStateOf(IntSize.Zero) }
    val titleHeight = with(density) { titleSize.height.toDp() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier.onSizeChanged { titleSize = it },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
                    Text(greetingName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                if (!courseLogoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(if (titleHeight > 0.dp) titleHeight else 44.dp)
                            .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f))
                    )
                    AsyncImage(
                        model = courseLogoUrl,
                        contentDescription = "Course logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(if (titleHeight > 0.dp) titleHeight else 44.dp)
                    )
                }
            }

            IconButton(onClick = onProfileTap) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        if (!profilePhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profilePhotoUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                }
            }
        }

        TitleView(colors = courseBallColors, modifier = Modifier.height(150.dp))
    }
}

@Composable
private fun MainActionCard(
    modifier: Modifier,
    isOnlineMode: Boolean,
    hasConnection: Boolean,
    disablePlaying: Boolean,
    makeColor: Color?,
    title: String,
    infoText: String,
    onBackToOffline: () -> Unit,
    onQuick: () -> Unit,
    onConnect: () -> Unit,
    onHost: () -> Unit,
    onJoin: () -> Unit
) {
    val myModifier = if (makeColor != null) {
        modifier
            .padding(bottom = 16.dp).fillMaxWidth()
    } else {
        modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .subtleShadow()
    }

    Column(
        modifier = myModifier
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                shape = RoundedCornerShape(25.dp)
            )
            .background(
                color = Color.subVsColor(makeColor = makeColor),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(14.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(30.dp)) {
                Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    val alpha: Float by animateFloatAsState(
                        targetValue = if (isOnlineMode) 1f else 0f,
                        animationSpec = tween(durationMillis = 300), label = "backButtonAlpha"
                    )
                    val scale: Float by animateFloatAsState(
                        targetValue = if (isOnlineMode) 1f else 0f,
                        animationSpec = tween(durationMillis = 300), label = "backButtonScale"
                    )

                    if (isOnlineMode || alpha > 0.01f) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(30.dp)
                                .alpha(alpha)
                                .scale(scale)
                        ) {
                            IconButton(onClick = onBackToOffline, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = title,
                    transitionSpec = {
                        (slideInVertically { -it / 2 } + fadeIn() + scaleIn()).togetherWith(
                            slideOutVertically { it / 2 } + fadeOut() + scaleOut()
                        )
                    },
                    label = "modeTitle"
                ) { currentTitle ->
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            InfoButton(infoText = infoText)
        }

        AnimatedContent(
            targetState = isOnlineMode,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it / 2 } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it / 2 } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { -it / 2 } + fadeIn()).togetherWith(
                        slideOutHorizontally { it / 2 } + fadeOut()
                    )
                }
            },
            label = "modeButtons"
        ) { online ->
            if (online) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledActionButton(
                        title = "Host",
                        enabled = true,
                        dimmed = disablePlaying,
                        color = Color(0xFF7E57C2),
                        onClick = onHost,
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.weight(1f)
                    )
                    FilledActionButton(
                        title = "Join",
                        enabled = true,
                        color = Color(0xFFF57C00),
                        onClick = onJoin,
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledActionButton(
                        title = "Quick",
                        enabled = true,
                        dimmed = disablePlaying,
                        color = Color(0xFF1E88E5),
                        onClick = onQuick,
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )

                    FilledActionButton(
                        title = "Connect",
                        enabled = hasConnection,
                        dimmed = disablePlaying,
                        color = Color(0xFF43A047),
                        onClick = onConnect,
                        icon = Icons.Default.Public,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

}

@Composable
private fun FilledActionButton(
    title: String,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dimmed: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier
            .height(50.dp)
            .alpha(if (dimmed || !enabled) 0.5f else 1f)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(title, color = Color.White)
        }
    }
}

@Composable
private fun LocationCard(
    hasLocationAccess: Boolean,
    courseName: String?,
    isSearching: Boolean,
    isRotating: Boolean,
    onSearchNearby: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
    makeColor: Color?
) {
    val context = LocalContext.current
    val retryRotation by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "retryRotation"
    )

    val myModifier = if (makeColor != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .subtleShadow()
    }

    Column(
        myModifier
            .background(
                color = Color.subVsColor(makeColor),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedContent(
            targetState = hasLocationAccess,
            transitionSpec = {
                (slideInVertically { it / 4 } + fadeIn()).togetherWith(
                    slideOutVertically { -it / 4 } + fadeOut()
                )
            },
            label = "locationAccessState"
        ) { hasAccess ->
            if (hasAccess) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Location:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AnimatedContent(
                            targetState = courseName ?: "No Location",
                            transitionSpec = {
                                (slideInVertically { it / 2 } + fadeIn()).togetherWith(
                                    slideOutVertically { -it / 2 } + fadeOut()
                                )
                            },
                            label = "courseNameState"
                        ) { currentCourseName ->
                            Text(
                                text = currentCourseName,
                                color = MaterialTheme.colorScheme.onSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        AnimatedContent(
                            targetState = courseName == null,
                            transitionSpec = {
                                (slideInHorizontally { it / 2 } + fadeIn()).togetherWith(
                                    slideOutHorizontally { it / 2 } + fadeOut()
                                )
                            },
                            label = "locationActions"
                        ) { shouldShowSearch ->
                            if (shouldShowSearch) {
                                Button(
                                    onClick = onSearchNearby,
                                    enabled = !isSearching,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    AnimatedContent(
                                        targetState = isSearching,
                                        transitionSpec = {
                                            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                                        },
                                        label = "searchLoading"
                                    ) { loading ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (loading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Text("Searching...", color = Color.White)
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                                                Text("Search Nearby", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    IconButton(onClick = onRetry, enabled = !isSearching) {
                                        Surface(
                                            modifier = Modifier.size(50.dp),
                                            shape = CircleShape,
                                            color = Color(0xFF1E88E5)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Retry",
                                                    tint = Color.White,
                                                    modifier = Modifier.rotate(retryRotation)
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = onExit) {
                                        Surface(
                                            modifier = Modifier.size(50.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFE53935)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear location", tint = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = "Location access required",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        "Location Access Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Mini Mate needs your location to find nearby courses and update your position.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .width(200.dp)
                    ) {
                        Text("Open Settings", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

}

@Composable
private fun LastGameCard(
    gameManager: GameManager,
    tintColor: Color?,
    onOpenStats: () -> Unit
) {
    val logic = remember { MainViewBusinessLogic() }
    val gameState = gameManager.userGames.collectAsState().value
    val latestGame = remember(gameState) { gameManager.latestGame() }
    val winner = remember(gameState) { gameManager.winnerOfLatestGame() }
    val holes = remember(gameState) { gameManager.usersHolesOfLatestGame() }
    val strokes = remember(gameState) { gameManager.usersScoreOfLatestGame() }

    AnimatedContent(
        targetState = latestGame,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 }).togetherWith(fadeOut())
        },
        label = "LastGameAnimation"
    ) { targetLatestGame ->
        if (targetLatestGame != null) {
            val subtitle = targetLatestGame.locationName ?: targetLatestGame.date.formatted()
            Box(modifier = Modifier.clickable(onClick = onOpenStats).padding(bottom = 14.dp)) {
                SectionStatsCard(
                    title = "Last Game",
                    subtitle = subtitle,
                    makeColor = tintColor,
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (targetLatestGame.players.size > 1 && winner != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.subTwoVsColor(tintColor))
                                        .padding(8.dp)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PhotoIconView(
                                        photoURL = winner.photoURL,
                                        name = logic.formatWinnerName(winner.name),
                                        ballColor = winner.ballColor,
                                        imageSize = 30.dp,
                                        backgroundColor = Color(0xFFFFCC00),
                                        showName = true
                                    )
                                }
                            }

                            StatCard(
                                title = "Your Strokes",
                                value = strokes.toString(),
                                makeColor = tintColor,
                                cornerRadius = 12.dp,
                                infoText = "The number of strokes you had last game.",
                                modifier = Modifier.weight(0.6f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.subTwoVsColor(tintColor))
                                .height(150.dp)
                        ) {
                            BarChartView(
                                data = holes,
                                title = "Your Recap"
                            )
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoDefault(topPadding = 0.dp)
            }
        }
    }
}

@Composable
private fun NoticeCard(message: String, makeColor: Color?) {
    val cardModifier = if (makeColor != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth().subtleShadow()
    }
    Column(
        modifier = cardModifier
            .background(Color.subVsColor(makeColor = makeColor), RoundedCornerShape(25.dp))
            .padding(14.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun contextAsActivityOrNull(): Activity? {
    return LocalActivity.current
}

/**
 * `deleteAccount` only invokes the onSuccess branch on the success path
 * (failures only set botMessage), so any call here means deletion succeeded.
 * Posted via TransientStatusSignal before navigating away, so SignInView
 * shows it once it lands there (same pattern as the logout notice).
 */
private fun announceAccountDeleted() {
    TransientStatusSignal.pending = StatusMessage(
        title = "Account Deleted",
        subtitle = "We're sorry to see you go.",
        icon = Icons.Default.Delete
    )
}
