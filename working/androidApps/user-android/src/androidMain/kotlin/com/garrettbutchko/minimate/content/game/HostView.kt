package com.garrettbutchko.minimate.content.game

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.helper.AddLocalPlayerView
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.GameCodeTiles
import com.garrettbutchko.minimate.content.helper.LobbyChurnNotifier
import com.garrettbutchko.minimate.content.helper.NumberPickerView
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.PopupCard
import com.garrettbutchko.minimate.content.helper.SectionCard
import com.garrettbutchko.minimate.content.helper.StatusMessage
import com.garrettbutchko.minimate.content.helper.StatusOverlayScaffold
import com.garrettbutchko.minimate.content.helper.TransientStatusOverlay
import com.garrettbutchko.minimate.content.helper.rememberTransientStatus
import com.garrettbutchko.minimate.di.KoinHelper
import com.garrettbutchko.minimate.di.KoinHelperParent
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.logic.HostViewBusinessLogic
import com.garrettbutchko.minimate.repositories.gameRepos.LocalGameRepository
import com.garrettbutchko.minimate.content.helper.WaitingIndicatorView
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.HostViewModel
import com.garrettbutchko.minimate.viewModels.toImageBitmap
import kotlinx.coroutines.launch

@Composable
fun HostGameView(
    gameModel: GameViewModel,
    locationHandler: LocationFinding,
    onDismiss: () -> Unit,
    isGuest: Boolean = false
) {
    val localGameRepo: LocalGameRepository = KoinHelperParent.getLocalGameRepo()


    val hostVM = remember { KoinHelper.getHostViewModel() }
    val logic = remember { HostViewBusinessLogic() }
    val game by gameModel.game.collectAsState()
    val course by gameModel.course.collectAsState()
    val onlineGame by gameModel.onlineGame.collectAsState()

    val hasLocationAccess by locationHandler.hasLocationAccess.collectAsState()
    val showLocationButton by hostVM.showLocationButton.collectAsState()
    val isRotating by hostVM.isRotating.collectAsState()

    val showAddLocalPlayer by hostVM.showAddLocalPlayer.collectAsState()
    val showDeleteAlert by hostVM.showDeleteAlert.collectAsState()
    val playerToDelete by hostVM.playerToDelete.collectAsState()
    val qrCodeBytes by hostVM.qrCodeImage.collectAsState()
    val scope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()

    var showGameCode by remember { mutableStateOf(false) }
    val lobbyNotice = rememberTransientStatus()

    // Remote players only — local "Add Player" guests are managed by the host
    // directly, and the host is always present.
    LobbyChurnNotifier(
        players = game.players,
        enabled = onlineGame,
        possessive = "your game",
        ignoredIds = setOf(game.hostUserId),
        ignoreLocalGuests = true,
        onNotice = { lobbyNotice.show(it) }
    )


    LaunchedEffect(Unit) {
        hostVM.resetTimer()
        hostVM.setUp()
        hostVM.startTimer { hostVisible -> if (!hostVisible) onDismiss() }
    }

    DisposableEffect(Unit) {
        onDispose {
            hostVM.stopTimer()
            hostVM.resetTimer()

            if (logic.shouldDismissGameOnDisappear(
                    isStarted = game.started,
                    isDismissed = game.dismissed,
                    showHost = false
                )
            ) {
                gameModel.dismissGame()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { hostVM.resetTimer() }
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(top = 100.dp, bottom = 80.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlayersSection(
                hostVM = hostVM,
                gameModel = gameModel,
                onlineGame = onlineGame,
                logic = logic
            )

            GameInfoSection(
                hostVM = hostVM,
                gameModel = gameModel,
                hasLocationAccess = hasLocationAccess,
                showLocationButton = showLocationButton,
                isConnected = NetworkChecker.shared.isConnected,
                isRotating = isRotating,
                qrCodeBytes = qrCodeBytes,
                mainOppColor = MaterialTheme.colorScheme.onSurface,
                isGuest = isGuest,
                onShowGameCode = {
                    hostVM.resetTimer()
                    showGameCode = true
                }
            )
        }

        // Top header with gradient
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        )
                    )
                )
                .padding(top = 30.dp)
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
            ) {
                if (isGuest) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(25.dp))
                            .clickable {
                                logic.handleGuestBackAction(
                                    dismissGame = { gameModel.dismissGame() },
                                    navigateToSignIn = { hostVM.viewManager.navigateToSignIn() }
                                )
                                onDismiss()
                            }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = logic.getHeaderTitle(isOnline = onlineGame),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(64.dp))
            }
        }

        // Bottom section with Start Game button and gradient
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(16.dp)
                .padding(bottom = if (isGuest) 40.dp else 0.dp)
        ) {
            Button(
                onClick = {
                    logic.handleStartGame(
                        isGuest = isGuest,
                        deleteGuestGame = {
                            scope.launch {
                                localGameRepo.deleteGuestGame()
                            }
                        },
                        performStart = {
                            hostVM.startGame(
                                onHostHidden = { onDismiss() },
                                isGuest = isGuest
                            )
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Game", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        GameCodeOverlay(
            visible = showGameCode,
            code = game.id,
            // While the code popup is up the notice rides along inside it, so it
            // doesn't cover the code the host is holding up for everyone to read.
            notice = lobbyNotice.message.takeIf { showGameCode },
            onDismiss = {
                hostVM.resetTimer()
                showGameCode = false
            }
        )

        TransientStatusOverlay(lobbyNotice, visible = lobbyNotice.message != null && !showGameCode)
    }

    if (showAddLocalPlayer) {
        AddLocalPlayerView(
            hasCourseSelected = course != null,
            onAddPlayer = { name, email, color ->
                hostVM.addPlayer(name, email, color)
                hostVM.setShowAddLocalPlayer(false)
            },
            onDismiss = { hostVM.setShowAddLocalPlayer(false) }
        )
    }

    if (showDeleteAlert) {
        AppAlertDialog(
            onDismissRequest = { hostVM.setShowDeleteAlert(false) },
            title = "Delete Player?",
            message = "Are you sure?",
            confirmText = "Delete",
            onConfirm = {
                logic.handleDeletePlayer(playerToDelete) { id ->
                    gameModel.removePlayer(id)
                }
                hostVM.setShowDeleteAlert(false)
                hostVM.setPlayerToDelete(null)
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { hostVM.setShowDeleteAlert(false) }
        )
    }
}

@Composable
private fun PlayersSection(
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    onlineGame: Boolean,
    logic: HostViewBusinessLogic
) {
    val game by gameModel.game.collectAsState()

    SectionCard(title = logic.getPlayersHeaderText(playerCount = game.players.size)) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            game.players.forEach { player ->
                HostPlayerIconView(
                    player = player,
                    isRemovable = player.userId.length == 6,
                    onTap = {
                        hostVM.setPlayerToDelete(player.userId)
                        hostVM.resetTimer()
                        hostVM.setShowDeleteAlert(true)
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable {
                        hostVM.setShowAddLocalPlayer(true)
                        hostVM.resetTimer()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Player",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(0.5.dp))
                Text(
                    text = "Add Player",
                    style = LocalTextStyle.current.copy(
                        fontSize = with(LocalDensity.current) { 9.dp.toSp() },
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onlineGame) {
                WaitingIndicatorView(
                    label = "Searching..."
                )
            }
        }
    }
}

@Composable
private fun GameInfoSection(
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    hasLocationAccess: Boolean,
    showLocationButton: Boolean,
    isConnected: Boolean,
    isRotating: Boolean,
    qrCodeBytes: ByteArray?,
    mainOppColor: Color,
    isGuest: Boolean = false,
    onShowGameCode: () -> Unit
) {
    val game by gameModel.game.collectAsState()
    val course by gameModel.course.collectAsState()
    val onlineGame by gameModel.onlineGame.collectAsState()
    val timeRemaining by hostVM.timeRemaining.collectAsState()
    val timerSeconds = timeRemaining.toInt()
    val timerString = "${timerSeconds / 60}:${(timerSeconds % 60).let { if (it < 10) "0$it" else "$it" }}"

    SectionCard(title = "Game Info") {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onlineGame) {
                // The code itself lives behind "Show Game Code" below, where it's
                // big enough to read from across a table.
                InfoRow(
                    label = "Game Code",
                    value = "",
                    mainOppColor = mainOppColor,
                    subOppColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                qrCodeBytes?.toImageBitmap()?.let { imageBitmap ->
                    val isDark = isSystemInDarkTheme()
                    val invertMatrix = ColorMatrix(floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Game QR Code",
                            modifier = Modifier.fillMaxHeight(),
                            colorFilter = if (isDark) ColorFilter.colorMatrix(invertMatrix) else null
                        )
                    }
                }

                TextButton(
                    onClick = onShowGameCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Game Code", fontWeight = FontWeight.Medium)
                }

                HorizontalDivider()

                InfoRow(
                    label = "Expires in",
                    value = timerString,
                    mainOppColor = mainOppColor,
                    subOppColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                HorizontalDivider()
            }

            if (hasLocationAccess) {
                LocationButtons(
                    hostVM = hostVM,
                    gameModel = gameModel,
                    showLocationButton = showLocationButton || isGuest,
                    isConnected = isConnected,
                    isRotating = isRotating,
                    mainOppColor = mainOppColor
                )
            }

            val currentCourse = course
            if (currentCourse != null) {
                if (currentCourse.customPar) {
                    InfoRow(
                        label = "Holes",
                        value = currentCourse.numHoles.toString(),
                        mainOppColor = mainOppColor,
                        subOppColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    HolesPickerRow(gameModel, hostVM, game.numberOfHoles, mainOppColor)
                }
            } else {
                HolesPickerRow(gameModel, hostVM, game.numberOfHoles, mainOppColor)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun HolesPickerRow(
    gameModel: GameViewModel,
    hostVM: HostViewModel,
    selectedNumber: Int,
    mainOppColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Holes:", style = MaterialTheme.typography.labelLarge, color = mainOppColor)
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        NumberPickerView(
            selectedNumber = selectedNumber,
            onSelectedNumberChange = {
                gameModel.setNumberOfHole(it)
                hostVM.resetTimer()
            },
            minNumber = 9,
            maxNumber = 21
        )
    }
}

@Composable
private fun LocationButtons(
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    showLocationButton: Boolean,
    isConnected: Boolean,
    isRotating: Boolean,
    mainOppColor: Color
) {
    val selectedCourse by gameModel.course.collectAsState()
    val locationName = selectedCourse?.name

    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "retryRotation"
    )

    if (!isConnected) return

    if (showLocationButton) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Location:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mainOppColor,
                    fontWeight = FontWeight.SemiBold
                )
                AnimatedContent(
                    targetState = locationName,
                    transitionSpec = {
                        slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                        slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    },
                    label = "locationName"
                ) { name ->
                    Text(
                        text = name ?: "No Location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (name != null) Modifier.basicMarquee() else Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedContent(
                targetState = locationName == null,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                },
                label = "locationButtons"
            ) { noLocation ->
                if (noLocation) {
                    Button(
                        onClick = { hostVM.searchNearby() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .width(180.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Nearby")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { hostVM.retry() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier
                                    .rotate(rotation)
                                    .size(24.dp),
                                tint = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                .clickable { hostVM.exit() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Location",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider()
    } else if (locationName != null) {
        InfoRow(
            label = "Location",
            value = locationName,
            mainOppColor = mainOppColor,
            subOppColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()
    }
}
/**
 * Full-screen popup showing the game code big enough to read across a table,
 * styled to match the app's other transient popups. While it's up, lobby
 * notices ("X joined your game") render as a compact banner underneath instead
 * of a second full-screen overlay.
 */
@Composable
private fun GameCodeOverlay(
    visible: Boolean,
    code: String,
    notice: StatusMessage?,
    onDismiss: () -> Unit
) {
    // Keep the last code/notice around so nothing pops out mid dismiss animation
    var lastCode by remember { mutableStateOf(code) }
    if (visible) lastCode = code
    var lastNotice by remember { mutableStateOf<StatusMessage?>(null) }
    notice?.let { lastNotice = it }

    StatusOverlayScaffold(visible = visible, dismissOnScrimTap = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PopupCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Game Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    GameCodeTiles(code = lastCode)

                    Text(
                        text = "Tap anywhere to close",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = notice != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                lastNotice?.let { message ->
                    Surface(
                        shape = CircleShape,
                        color = AlertDialogDefaults.containerColor,
                        tonalElevation = AlertDialogDefaults.TonalElevation
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = message.icon,
                                contentDescription = null,
                                tint = message.tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = message.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
