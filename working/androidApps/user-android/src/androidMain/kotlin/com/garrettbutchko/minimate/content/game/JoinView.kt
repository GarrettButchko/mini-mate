package com.garrettbutchko.minimate.content.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.di.KoinHelper
import com.garrettbutchko.minimate.content.helper.AppAlertDialog
import com.garrettbutchko.minimate.content.helper.GAME_CODE_LENGTH
import com.garrettbutchko.minimate.content.helper.GameCodeTile
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.LobbyChurnNotifier
import com.garrettbutchko.minimate.content.helper.SectionCard
import com.garrettbutchko.minimate.content.helper.StatusMessage
import com.garrettbutchko.minimate.content.helper.TransientStatusOverlay
import com.garrettbutchko.minimate.content.helper.WaitingIndicatorView
import com.garrettbutchko.minimate.content.helper.rememberTransientStatus
import com.garrettbutchko.minimate.helper.QRScannerView
import com.garrettbutchko.minimate.logic.JoinViewBusinessLogic
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.shared.R as SharedR
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun JoinGameView(
    showJoin: Boolean,
    onShowJoinChange: (Boolean) -> Unit,
    viewManager: ViewManager,
    gameModel: GameViewModel,
    authModel: AuthViewModel
) {
    val joinVM = remember { KoinHelper.getJoinViewModel() }
    val logic = remember { JoinViewBusinessLogic() }

    val game by gameModel.game.collectAsState()
    val inGame by joinVM.inGame.collectAsState()
    val gameCode by joinVM.gameCode.collectAsState()
    val message by joinVM.message.collectAsState()
    val showExitAlert by joinVM.showExitAlert.collectAsState()
    val userModel by authModel.userModel.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    val lobbyNotice = rememberTransientStatus()
    val joinedTint = colorResource(id = SharedR.color.swiftGreen)

    val wasInGame = remember { mutableStateOf(inGame) }
    LaunchedEffect(inGame) {
        val previouslyInGame = wasInGame.value
        wasInGame.value = inGame
        if (previouslyInGame || !inGame) return@LaunchedEffect

        val host = game.players.firstOrNull { it.userId == game.hostUserId }
        lobbyNotice.show(
            StatusMessage(
                title = "Joined!",
                subtitle = host?.let { "You joined ${it.name}'s game." } ?: "You joined the game.",
                icon = Icons.Default.PersonAdd,
                tint = joinedTint
            )
        )
    }

    LobbyChurnNotifier(
        players = game.players,
        enabled = inGame,
        possessive = "the game",
        selfId = userModel?.googleId,
        onNotice = { lobbyNotice.show(it) }
    )


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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minContentHeight = maxHeight - 92.dp - 92.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 92.dp, bottom = 92.dp, start = 16.dp, end = 16.dp),
            ) {
                if (!inGame) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minContentHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        JoinCard(
                            gameCode = gameCode,
                            message = message,
                            mainOppColor = MaterialTheme.colorScheme.onSurface,
                            formatCode = logic::formatEnteredCode,
                            onCodeChange = { joinVM.setGameCode(it) },
                            onScanClick = { showScanner = true }
                        )
                    }
                } else {
                    ActiveLobby(
                        gameModel = gameModel,
                        logic = logic,
                        mainOppColor = MaterialTheme.colorScheme.onSurface
                    )
                }
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

        TransientStatusOverlay(lobbyNotice)
    }

    if (showExitAlert) {
        AppAlertDialog(
            onDismissRequest = { joinVM.setShowExitAlert(false) },
            title = "Exit Game?",
            message = "Are you sure?",
            confirmText = "Leave",
            onConfirm = {
                joinVM.leaveGame()
                joinVM.setShowExitAlert(false)
            },
            destructive = true,
            dismissText = "Cancel",
            onDismiss = { joinVM.setShowExitAlert(false) }
        )
    }

    if (showScanner) {
        QRScannerView(
            onScan = { scanned ->
                joinVM.setGameCode(logic.formatScannedCode(scanned))
                showScanner = false
            },
            onCanceled = { showScanner = false }
        )
    }
}

@Composable
private fun JoinCard(
    gameCode: String,
    message: String,
    mainOppColor: Color,
    formatCode: (String) -> String,
    onCodeChange: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard {
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

                GameCodeField(
                    gameCode = gameCode,
                    formatCode = formatCode,
                    onCodeChange = onCodeChange
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
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
        }
    }
}

/**
 * Passcode-style entry matching the host's GameCodeOverlay: one rounded square
 * per character. A hidden text field drives the keyboard; the tiles are purely
 * visual. Tap a filled tile to overwrite that position, tap an empty one to
 * append, and paste/scan replace the whole code.
 */
@Composable
private fun GameCodeField(
    gameCode: String,
    formatCode: (String) -> String,
    onCodeChange: (String) -> Unit
) {
    // Which tile the next keystroke overwrites; null = append at the end. Mirrors
    // iOS's selectedIndex — tapping a filled tile targets it for the next
    // keystroke instead of just moving the (invisible, untappable) field's own
    // caret, which is what let you type past a letter but never back into it.
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(gameCode, TextRange(gameCode.length))) }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // External updates (QR scan, Kotlin-side formatting) → resync the field
    LaunchedEffect(gameCode) {
        if (fieldValue.text != gameCode) {
            selectedIndex = null
            fieldValue = TextFieldValue(gameCode, TextRange(gameCode.length))
        }
    }

    val chars = fieldValue.text
    val cursorIndex = (selectedIndex ?: chars.length).coerceIn(0, GAME_CODE_LENGTH - 1)

    fun selectTile(index: Int) {
        // Tap a filled tile to overwrite it, tap an empty one to append.
        selectedIndex = index.takeIf { it < chars.length }
        focusRequester.requestFocus()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { selectTile(chars.length) }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(GAME_CODE_LENGTH) { index ->
                GameCodeTile(
                    char = chars.getOrNull(index),
                    isActive = isFocused && index == cursorIndex,
                    onClick = { selectTile(index) }
                )
            }
        }

        BasicTextField(
            value = fieldValue,
            onValueChange = { new ->
                // The field's own caret is meaningless here — it's invisible and
                // always resets to the end on each resync below, so every raw
                // keystroke shows up as an append/removal at the end regardless
                // of which tile is "selected". Remap that onto the selected tile
                // instead of trusting the field's native insert-at-cursor behavior,
                // which would just shove the rest of the code sideways.
                val old = fieldValue.text
                var result = new.text
                var newSelection = selectedIndex

                val sel = selectedIndex
                if (sel != null && sel < old.length && new.text.length == old.length + 1) {
                    // Typed one character with a tile selected → overwrite that position
                    val overwritten = old.toCharArray()
                    overwritten[sel] = new.text.last()
                    result = String(overwritten)
                    newSelection = if (sel + 1 < overwritten.size) sel + 1 else null
                } else if (sel != null && sel < old.length && new.text.length == old.length - 1) {
                    // Backspace with a tile selected → delete that position
                    val remaining = old.toMutableList().also { it.removeAt(sel) }
                    result = remaining.joinToString(separator = "")
                    newSelection = if (sel < remaining.size) sel else null
                } else if (new.text.length > old.length + 1) {
                    // Multi-character jump = paste → the pasted text becomes the code
                    result = if (new.text.startsWith(old)) new.text.removePrefix(old) else new.text
                    newSelection = null
                }

                val formatted = formatCode(result)
                selectedIndex = newSelection
                fieldValue = TextFieldValue(formatted, TextRange(formatted.length))
                onCodeChange(formatted)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false
            ),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            // Shrunk to 1.dp instead of overlaying the tile grid: a full-size
            // invisible field drawn on top would win every tap itself (Compose
            // hit-tests top-most first) before a tile's own onClick ever saw it.
            // At 1.dp it holds focus/IME only — iOS solves the same problem with
            // .allowsHitTesting(false) on its hidden field; this is the Compose
            // equivalent since BasicTextField has no such flag.
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused }
        )
    }
}

@Composable
private fun ActiveLobby(
    gameModel: GameViewModel,
    logic: JoinViewBusinessLogic,
    mainOppColor: Color
) {
    val game by gameModel.game.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SectionCard(title = logic.getPlayersHeaderText(game.players.size)) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    WaitingIndicatorView(
                        label = "Waiting..."
                    )
                }
            }
        }

        SectionCard() {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("Code", game.id, mainOppColor)
                HorizontalDivider()
                InfoRow("Holes", game.numberOfHoles.toString(), mainOppColor)
                HorizontalDivider()
                InfoRow("Location", logic.getLocationName(game.locationName), mainOppColor)
            }
        }
    }
}
