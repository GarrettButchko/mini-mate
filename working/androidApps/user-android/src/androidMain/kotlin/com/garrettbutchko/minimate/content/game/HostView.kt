package com.garrettbutchko.minimate.content.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.content.helper.AddLocalPlayerView
import com.garrettbutchko.minimate.content.helper.NumberPickerView
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.repositories.gameRepos.LocalGameRepository
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.HostViewModel
import com.garrettbutchko.minimate.viewModels.toImageBitmap
import kotlinx.coroutines.launch

@Composable
fun HostGameView(
    showHost: Boolean,
    onShowHostChange: (Boolean) -> Unit,
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    locationHandler: LocationFinding,
    localGameRepo: LocalGameRepository,
    isGuest: Boolean = false,
    onBack: () -> Unit = {}
) {
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
    hostVM.timeRemaining.collectAsState() // keep timer-driven recomposition active

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        hostVM.resetTimer()
        hostVM.setUp()
        hostVM.startTimer { hostVisible -> onShowHostChange(hostVisible) }
    }

    DisposableEffect(Unit) {
        onDispose {
            hostVM.stopTimer()
            hostVM.resetTimer()

            if (hostVM.shouldDismissGameOnDisappear(
                    isStarted = game.started,
                    isDismissed = game.dismissed,
                    showHost = showHost
                )
            ) {
                gameModel.dismissGame()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 92.dp, bottom = 92.dp)
                .padding(horizontal = 16.dp)
                .horizontalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(680.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlayersSection(
                    hostVM = hostVM,
                    gameModel = gameModel,
                    onlineGame = onlineGame
                )

                GameInfoSection(
                    hostVM = hostVM,
                    gameModel = gameModel,
                    hasLocationAccess = hasLocationAccess,
                    showLocationButton = showLocationButton,
                    isConnected = NetworkChecker.shared.isConnected,
                    isRotating = isRotating,
                    qrCodeBytes = qrCodeBytes
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
                .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isGuest) {
                    IconButton(onClick = {
                        hostVM.handleGuestBackAction(
                            dismissGame = { gameModel.dismissGame() },
                            navigateToSignIn = { hostVM.viewManager.navigateToSignIn() }
                        )
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Blue)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = hostVM.getHeaderTitle(isOnline = onlineGame),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    hostVM.handleStartGame(
                        isGuest = isGuest,
                        deleteGuestGame = {
                            scope.launch {
                                localGameRepo.deleteGuestGame()
                            }
                        },
                        performStart = {
                            hostVM.startGame(
                                onHostHidden = { onShowHostChange(false) },
                                isGuest = isGuest
                            )
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Start Game", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
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
        AlertDialog(
            onDismissRequest = { hostVM.setShowDeleteAlert(false) },
            title = { Text("Delete Player?") },
            confirmButton = {
                TextButton(onClick = {
                    hostVM.handleDeletePlayer(playerToDelete) { id ->
                        gameModel.removePlayer(id)
                    }
                    hostVM.setShowDeleteAlert(false)
                    hostVM.setPlayerToDelete(null)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { hostVM.setShowDeleteAlert(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlayersSection(
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    onlineGame: Boolean
) {
    val game by gameModel.game.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = hostVM.getPlayersHeaderText(playerCount = game.players.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
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

                IconButton(
                    onClick = {
                        hostVM.setShowAddLocalPlayer(true)
                        hostVM.resetTimer()
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Player")
                }

                if (onlineGame) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Text("Searching...", style = MaterialTheme.typography.labelSmall)
                    }
                }
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
    qrCodeBytes: ByteArray?
) {
    val game by gameModel.game.collectAsState()
    val course by gameModel.course.collectAsState()
    val onlineGame by gameModel.onlineGame.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Game Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            if (onlineGame) {
                InfoRow(label = "Game Code", value = game.id)

                qrCodeBytes?.toImageBitmap()?.let { imageBitmap ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Game QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }

                InfoRow(label = "Expires in", value = hostVM.timeString(), trailingIcon = {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                })
            }

            if (hasLocationAccess) {
                LocationButtons(
                    hostVM = hostVM,
                    gameModel = gameModel,
                    showLocationButton = showLocationButton,
                    isConnected = isConnected,
                    isRotating = isRotating
                )
            }

            if (course?.customPar == true) {
                InfoRow(label = "Holes", value = course?.numHoles?.toString() ?: "18")
            } else {
                Text("Holes", style = MaterialTheme.typography.labelLarge)
                NumberPickerView(
                    selectedNumber = game.numberOfHoles,
                    onSelectedNumberChange = {
                        gameModel.setNumberOfHole(it)
                        hostVM.resetTimer()
                    },
                    minNumber = 9,
                    maxNumber = 21
                )
            }
        }
    }
}

@Composable
private fun LocationButtons(
    hostVM: HostViewModel,
    gameModel: GameViewModel,
    showLocationButton: Boolean,
    isConnected: Boolean,
    isRotating: Boolean
) {
    val selectedCourse by gameModel.course.collectAsState()
    val locationName = selectedCourse?.name

    if (!isConnected) return

    if (showLocationButton) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(label = "Location", value = locationName ?: "No Location")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (locationName == null) {
                    Button(onClick = { hostVM.searchNearby() }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Nearby")
                    }
                } else {
                    IconButton(onClick = { hostVM.retry() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.rotate(if (isRotating) 360f else 0f)
                        )
                    }
                    IconButton(onClick = { hostVM.exit() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Location")
                    }
                }
            }
        }
    } else if (locationName != null) {
        InfoRow(label = "Location", value = locationName)
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.weight(1f))
            if (trailingIcon != null) {
                trailingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

