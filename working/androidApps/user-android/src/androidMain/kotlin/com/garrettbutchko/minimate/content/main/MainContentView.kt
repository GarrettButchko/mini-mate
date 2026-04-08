package com.garrettbutchko.minimate.content.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.helper.IAPManager
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.logic.MainViewBusinessLogic
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val user by authModel.userModel.collectAsState()
    val course by gameModel.course.collectAsState()
    val userGames by gameManager.userGames.collectAsState()
    val hasLocationAccess by locationHandler.hasLocationAccess.collectAsState()

    var isOnlineMode by remember { mutableStateOf(false) }
    val showInfoState = remember { mutableStateOf(false) }
    val showProSheetState = remember { mutableStateOf(false) }
    var showPromoStage by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isRotating by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopBar(
            greetingName = logic.getGreetingName(user?.name),
            courseName = course?.name,
            onProfileTap = { showInfoState.value = true }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                        onOpenStats = { viewManager.navigateToMain(0) }
                    )
                }

                item { Spacer(modifier = Modifier.height(88.dp)) }
            }

            MainActionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
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
    onProfileTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(greetingName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
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
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            }
        }

        Card {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("Mini Mate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
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
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))) {
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

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
}

@Composable
private fun LastGameCard(
    gameManager: GameManager,
    onOpenStats: () -> Unit
) {
    val latestGame = remember(gameManager.userGames.collectAsState().value) { gameManager.latestGame() }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Last Game", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            if (latestGame == null) {
                Text("No games yet. Start your first round.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val winner = gameManager.winnerOfLatestGame()
                val holes = gameManager.usersHolesOfLatestGame()
                val average = if (holes.isNotEmpty()) holes.sumOf { it.strokes }.toFloat() / holes.size else 0f

                if (latestGame.players.size > 1 && winner != null) {
                    Text("Winner: ${winner.name}", style = MaterialTheme.typography.bodyMedium)
                }
                Text("Your Strokes: ${gameManager.usersScoreOfLatestGame()}")
                Text("Avg per hole: ${"%.2f".format(average)}")

                HorizontalDivider()

                Button(onClick = onOpenStats, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Stats")
                }
            }
        }
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




