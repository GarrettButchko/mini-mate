package com.garrettbutchko.minimate.content.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.ProfileView
import com.garrettbutchko.minimate.content.TitleView
import com.garrettbutchko.minimate.content.helper.LogoDefault
import com.garrettbutchko.minimate.content.helper.modifiers.BarChartView
import com.garrettbutchko.minimate.helper.IAPManager
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.logic.MainViewBusinessLogic
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.courseColors
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.modifiers.subTwoVsColor
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.content.stats.SectionStatsCard
import com.garrettbutchko.minimate.content.stats.StatCard
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor



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
    val bgColor = colorResource(id = R.color.bg)
    val user by authModel.userModel.collectAsState()
    val course by gameModel.course.collectAsState()
    val userGames by gameManager.userGames.collectAsState()
    val hasLocationAccess by locationHandler.hasLocationAccess.collectAsState()

    var isOnlineMode by remember { mutableStateOf(false) }
    val showInfoState = remember { mutableStateOf(false) }
    val showProfileSheetState = remember { mutableStateOf(false) }
    val showProSheetState = remember { mutableStateOf(false) }
    var showPromoStage by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isRotating by remember { mutableStateOf(false) }
    var profileBotMessage by remember { mutableStateOf("") }
    var isProfileBotError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val disablePlaying = logic.isPlayDisabled(
        isPro = user?.isPro,
        gameCount = user?.gameIDs?.size ?: 0
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
            delay((logic.proPromotionDisplayDuration * 1000).toLong())
            showPromoStage = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
    ) {
        MainTopBar(
            greetingName = logic.getGreetingName(user?.name),
            courseName = course?.name,
            profilePhotoUrl = user?.photoURL,
            onProfileTap = {
                profileBotMessage = ""
                isProfileBotError = false
                showProfileSheetState.value = true
            },
            courseBallColors = course?.courseColors
        )

        MainActionCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
            isOnlineMode = isOnlineMode,
            disablePlaying = disablePlaying,
            hasConnection = NetworkChecker.shared.isConnected,
            infoText = logic.getInfoMessage(isOnlineMode = isOnlineMode),
            title = logic.getHeaderTitle(isOnlineMode = isOnlineMode),
            onBackToOffline = { isOnlineMode = false },
            onInfo = { showInfoState.value = true },
            onQuick = {
                if (disablePlaying) {
                    showProSheetState.value = true
                } else {
                    gameModel.createGame(online = false, guestData = null)
                    viewManager.navigateToHost()
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
                viewManager.navigateToHost()
            },
            onJoin = {
                gameModel.resetGame()
                viewManager.navigateToJoin()
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
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
                                    isSearching = true
                                    gameModel.hasLoaded = false
                                    runCatching { gameModel.findClosestLocationAndLoadCourse() }
                                    isSearching = false
                                }
                            },
                            onRetry = {
                                scope.launch {
                                    isRotating = true
                                    isSearching = true
                                    gameModel.hasLoaded = false
                                    runCatching { gameModel.findClosestLocationAndLoadCourse() }
                                    isSearching = false
                                    delay(700)
                                    isRotating = false
                                }
                            },
                            onExit = { gameModel.exit() }
                        )
                    }
                }

                item {
                    if (logic.shouldShowProStopper(user?.isPro, user?.gameIDs?.size ?: 0)) {
                        NoticeCard(
                            message = "You have reached the free limit. Upgrade to Pro to store more than 2 games."
                        )
                    }
                }

                item {
                    if (NetworkChecker.shared.isConnected && user?.isPro == false) {
                        NoticeCard(message = "Ads appear here for free users.")
                    }
                }

                item {
                    LastGameCard(
                        gameManager = gameManager,
                        tintColor = course?.scoreCardColor ?: course?.courseColors?.firstOrNull(),
                        onOpenStats = { viewManager.navigateToMain(0) }
                    )
                }
            }



            if (NetworkChecker.shared.isConnected && user?.isPro == false) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Button(onClick = {
                        if (!showPromoStage) {
                            showPromoStage = true
                        } else {
                            showProSheetState.value = true
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedVisibility(visible = showPromoStage, enter = fadeIn(), exit = fadeOut()) {
                                Text("Tap to buy Pro!", color = Color.White)
                            }
                            Text("✨")
                        }
                    }
                }
            }
        }
    }

    if (showInfoState.value) {
        AlertDialog(
            onDismissRequest = { showInfoState.value = false },
            confirmButton = {
                TextButton(onClick = { showInfoState.value = false }) { Text("OK") }
            },
            title = { Text("Info") },
            text = { Text(logic.getInfoMessage(isOnlineMode = isOnlineMode)) }
        )
    }

    if (showProfileSheetState.value) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheetState.value = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ProfileView(
                userProfileImage = user?.photoURL,
                userName = user?.name ?: "Player",
                userEmail = user?.email,
                userGoogleId = user?.googleId ?: "",
                userIsPro = user?.isPro ?: false,
                userAccountType = user?.accountType?.joinToString(",") ?: "",
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
                    showProfileSheetState.value = false
                    authModel.logout()
                    viewManager.navigateToWelcome()
                },
                onDeleteAccountGoogle = {
                    scope.launch {
                        authModel.reauthenticateWithGoogle().fold(
                            onSuccess = { credential ->
                                authModel.deleteAccount(credential).fold(
                                    onSuccess = {
                                        showProfileSheetState.value = false
                                        viewManager.navigateToWelcome()
                                    },
                                    onFailure = {
                                        profileBotMessage = it.message ?: "Unable to delete account."
                                        isProfileBotError = true
                                    }
                                )
                            },
                            onFailure = {
                                profileBotMessage = it.message ?: "Google re-authentication failed."
                                isProfileBotError = true
                            }
                        )
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
                        authModel.reauthenticateWithEmail(email, password).fold(
                            onSuccess = { credential ->
                                authModel.deleteAccount(credential).fold(
                                    onSuccess = {
                                        showProfileSheetState.value = false
                                        viewManager.navigateToWelcome()
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
                },
                onDismiss = {
                    showProfileSheetState.value = false
                }
            )
        }
    }

    if (showProSheetState.value) {
        ModalBottomSheet(onDismissRequest = { showProSheetState.value = false }) {
            ProView(
                showSheet = showProSheetState.value,
                onShowSheetChange = { showProSheetState.value = it },
                authModel = authModel,
                iapManager = iapManager,
                activity = contextAsActivityOrNull()
            )
        }
    }
}

@Composable
private fun MainTopBar(
    greetingName: String,
    courseName: String?,
    courseBallColors: List<Color>?,
    profilePhotoUrl: String?,
    onProfileTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.bg))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = colorResource(id = R.color.subTwoOpp))
                Text(greetingName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = colorResource(id = R.color.mainOpp))
                if (!courseName.isNullOrBlank()) {
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

        TitleView(colors = courseBallColors)
    }
}

@Composable
private fun MainActionCard(
    modifier: Modifier,
    isOnlineMode: Boolean,
    disablePlaying: Boolean,
    hasConnection: Boolean,
    title: String,
    infoText: String,
    onBackToOffline: () -> Unit,
    onInfo: () -> Unit,
    onQuick: () -> Unit,
    onConnect: () -> Unit,
    onHost: () -> Unit,
    onJoin: () -> Unit
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOnlineMode) {
                    IconButton(onClick = onBackToOffline) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = onInfo) {
                    Icon(Icons.Default.Info, contentDescription = infoText, tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (isOnlineMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledActionButton(title = "Host", enabled = true, color = Color(0xFF7E57C2), onClick = onHost, modifier = Modifier.weight(1f))
                    FilledActionButton(title = "Join", enabled = true, color = Color(0xFFF57C00), onClick = onJoin, modifier = Modifier.weight(1f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledActionButton(
                        title = "Quick",
                        enabled = !disablePlaying,
                        color = Color(0xFF1E88E5),
                        onClick = onQuick,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasConnection) {
                        FilledActionButton(
                            title = "Connect",
                            enabled = !disablePlaying,
                            color = Color(0xFF43A047),
                            onClick = onConnect,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        FilledActionButton(
                            title = "Connect",
                            enabled = false,
                            color = Color(0xFF43A047),
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(50.dp)
    ) {
        Text(title, color = Color.White)
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
    onExit: () -> Unit
) {
    val context = LocalContext.current
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(25.dp)
                )
                .background(
                    color = colorResource(id = R.color.sub),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hasLocationAccess) {
                Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(courseName ?: "No location selected", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (courseName == null) {
                        Button(onClick = onSearchNearby, enabled = !isSearching) {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isSearching) "Searching..." else "Search Nearby")
                        }
                    } else {
                        IconButton(onClick = onRetry, enabled = !isSearching) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.rotate(if (isRotating) 360f else 0f))
                        }
                        IconButton(onClick = onExit) {
                            Icon(Icons.Default.Public, contentDescription = "Clear location")
                        }
                    }
                }
            } else {
                Text("Location access required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Mini Mate needs your location to find nearby courses.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Open Settings",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                )
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

    if (latestGame != null) {
        val cardHeight = 130.dp

        Box(modifier = Modifier.clickable(onClick = onOpenStats)) {
            SectionStatsCard(
                title = "Last Game",
                spacing = 12.dp,
                makeColor = tintColor,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (latestGame.players.size > 1 && winner != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cardHeight)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.subVsColor(tintColor))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PhotoIconView(
                                    photoURL = winner.photoURL,
                                    name = logic.formatWinnerName(winner.name),
                                    ballColor = winner.ballColor,
                                    imageSize = 30.dp,
                                    backgroundColor = Color.Yellow
                                )
                            }
                        }

                        StatCard(
                            title = "Your Strokes",
                            value = strokes.toString(),
                            makeColor = tintColor,
                            cornerRadius = 12.dp,
                            cardHeight = cardHeight,
                            infoText = "The number of strokes you had last game.",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.subTwoVsColor(tintColor))
                    ) {
                        BarChartView(
                            data = holes,
                            title = "Recap of Game"
                        )
                    }
                }
            )
        }
    } else {
        LogoDefault(topPadding = 0.dp)
    }

}

@Composable
private fun NoticeCard(message: String) {
    Card {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun contextAsActivityOrNull(): Activity? {
    return LocalActivity.current
}
