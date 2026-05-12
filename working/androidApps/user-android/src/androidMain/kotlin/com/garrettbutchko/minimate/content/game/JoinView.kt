package com.garrettbutchko.minimate.content.game

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.helper.QRScannerView
import com.garrettbutchko.minimate.logic.JoinViewBusinessLogic
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.JoinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun JoinGameView(
    showJoin: Boolean,
    onShowJoinChange: (Boolean) -> Unit,
    joinVM: JoinViewModel = JoinViewModel(),
    viewManager: ViewManager,
    gameModel: GameViewModel,
    onBack: () -> Unit = {}
) {
    val logic = remember { JoinViewBusinessLogic() }

    val game by gameModel.game.collectAsState()
    val inGame by joinVM.inGame.collectAsState()
    val gameCode by joinVM.gameCode.collectAsState()
    val message by joinVM.message.collectAsState()
    val showExitAlert by joinVM.showExitAlert.collectAsState()

    var showScanner by remember { mutableStateOf(false) }


    LaunchedEffect(showJoin) {
        joinVM.joinDidDismiss(showJoin)
    }

    LaunchedEffect(game.started) {
        joinVM.gameDidStart(started = game.started) {
            viewManager.navigateToScoreCard(isGuest = false)
        }
    }

    LaunchedEffect(game.dismissed) {
        joinVM.gameDidDismiss(dismissed = game.dismissed)
    }

    DisposableEffect(Unit) {
        onDispose {
            onShowJoinChange(false)
            joinVM.joinDidDismiss(showJoin = false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 92.dp, bottom = 92.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (!inGame) {
                JoinCard(
                    gameCode = gameCode,
                    message = message,
                    logic = logic,
                    mainOppColor = MaterialTheme.colorScheme.onSurface,
                    subColor = MaterialTheme.colorScheme.surfaceVariant,
                    onCodeChange = { joinVM.setGameCode(logic.formatEnteredCode(it)) },
                    onScanClick = { showScanner = true }
                )
            } else {
                ActiveLobby(
                    gameModel = gameModel,
                    logic = logic,
                    mainOppColor = MaterialTheme.colorScheme.onSurface,
                    subColor = MaterialTheme.colorScheme.surfaceVariant
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
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        )
                    )
                )
                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Join Game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

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
        ) {
            if (inGame) {
                Button(
                    onClick = { joinVM.setShowExitAlert(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Exit Game", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                val disabled = logic.isJoinButtonDisabled(gameCode)
                Button(
                    onClick = { joinVM.joinGame() },
                    enabled = !disabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.SportsGolf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Game", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showExitAlert) {
        AlertDialog(
            onDismissRequest = { joinVM.setShowExitAlert(false) },
            title = { Text("Exit Game?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    joinVM.leaveGame()
                    joinVM.setShowExitAlert(false)
                }) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { joinVM.setShowExitAlert(false) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    if (showScanner) {
        ModalBottomSheet(onDismissRequest = { showScanner = false }) {
            Text(
                text = "Scan QR Code",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                QRScannerView(
                    onScan = { scanned ->
                        joinVM.setGameCode(logic.formatScannedCode(scanned))
                        showScanner = false
                    },
                    onCanceled = { showScanner = false }
                )
            }
        }
    }
}

@Composable
private fun JoinCard(
    gameCode: String,
    message: String,
    logic: JoinViewBusinessLogic,
    mainOppColor: Color,
    subColor: Color,
    onCodeChange: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = subColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Enter Game Code",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = mainOppColor
                )
                Text(
                    "Ask the host for the 6-digit code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = gameCode,
                    onValueChange = onCodeChange,
                    singleLine = true,
                    label = { Text("GameCode ie: 123456") },
                    modifier = Modifier.width(220.dp)
                )

                Text("OR", style = MaterialTheme.typography.labelSmall, color = mainOppColor)

                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code")
                }
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(subColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
        }

        Text(
            text = if (logic.isJoinButtonDisabled(gameCode)) "Enter a 6 character game code" else "Ready to join",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActiveLobby(
    gameModel: GameViewModel,
    logic: JoinViewBusinessLogic,
    mainOppColor: Color,
    subColor: Color
) {
    val game by gameModel.game.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = subColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = logic.getPlayersHeaderText(game.players.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = mainOppColor
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    game.players.forEach { player ->
                        HostPlayerIconView(
                            player = player,
                            isRemovable = false,
                            onTap = null
                        )
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Waiting...",
                            style = MaterialTheme.typography.labelSmall,
                            color = mainOppColor
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = subColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoLine("Code", game.id, mainOppColor)
                InfoLine("Holes", game.numberOfHoles.toString(), mainOppColor)
                InfoLine("Location", logic.getLocationName(game.locationName), mainOppColor)
            }
        }
    }
}

@Composable
private fun InfoLine(title: String, value: String, mainOppColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = mainOppColor)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}