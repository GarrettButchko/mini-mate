package com.garrettbutchko.minimate.content.game

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.game.fix.GameInfoViewAndroid
import com.garrettbutchko.minimate.content.helper.BannerAdView
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.modifiers.SyncScrollStates
import com.garrettbutchko.minimate.logic.ScoreCardBusinessLogic
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreCardGameView(
    isGuest: Boolean,
    gameModel: GameViewModel,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    modifier: Modifier = Modifier
) {
    val logic = remember { ScoreCardBusinessLogic() }
    val context = LocalContext.current

    val game by gameModel.game.collectAsState()
    val course by gameModel.course.collectAsState()
    val currentCourse = course
    val user by authModel.userModel.collectAsState()

    val headerScrollState = rememberScrollState()
    val rowsScrollState = rememberScrollState()
    val totalScrollState = rememberScrollState()
    SyncScrollStates(headerScrollState, rowsScrollState, totalScrollState)

    val holeCount = logic.getHoleCount(
        hasCustomPar = currentCourse?.customPar,
        courseNumHoles = currentCourse?.numHoles,
        gameNumHoles = game.numberOfHoles
    )

    var showCompleteDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showRecap by rememberSaveable { mutableStateOf(false) }
    var hasUploaded by rememberSaveable { mutableStateOf(false) }
    var passGame by remember { mutableStateOf<com.garrettbutchko.minimate.dataModels.gameModels.Game?>(null) }

    LaunchedEffect(game.id) {
        if (!game.completed) {
            showRecap = false
            hasUploaded = false
            passGame = null
        }
    }

    LaunchedEffect(game.completed) {
        if (game.completed) {
            passGame = game
            logic.processEndGame(
                hasUploaded = hasUploaded,
                persistAction = { gameModel.finishAndPersistGame(game = game, isGuest = isGuest) },
                markAsUploaded = { hasUploaded = true }
            )
            showRecap = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Scorecard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    currentCourse?.name?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
                currentCourse?.logo?.takeIf { it.isNotBlank() }?.let { logo ->
                    AsyncImage(model = logo, contentDescription = "Course Logo", modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = { showInfoSheet = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Game Info")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(parseColor(currentCourse?.scoreCardColorDT).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(100.dp).padding(start = 12.dp)
                    )
                    Row(modifier = Modifier.horizontalScroll(headerScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.width(1.dp).height(78.dp))
                            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                                PhotoIconView(
                                    photoURL = player.photoURL,
                                    name = player.name,
                                    ballColor = player.ballColor,
                                    imageSize = 30.dp,
                                    showName = true
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.width(100.dp)) {
                        for (i in 1..holeCount) {
                            if (i > 1) HorizontalDivider()
                            Column(
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Hole $i", fontWeight = FontWeight.Medium)
                                if (currentCourse?.customPar == true) {
                                    currentCourse.pars.getOrNull(i - 1)?.let { par ->
                                        Text("Par: $par", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.horizontalScroll(rowsScrollState)) {
                        game.players.forEachIndexed { pIndex, player ->
                            if (pIndex > 0) HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxSize())
                            Column(modifier = Modifier.width(100.dp)) {
                                for (i in 1..holeCount) {
                                    if (i > 1) HorizontalDivider()
                                    val strokes = player.holes.firstOrNull { it.number == i }?.strokes ?: 0
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val next = (strokes - 1).coerceAtLeast(0)
                                                gameModel.updateHoleStroke(player.id, i, next)
                                            }
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                        }
                                        Text(strokes.toString(), fontWeight = FontWeight.SemiBold)
                                        IconButton(
                                            onClick = {
                                                val next = (strokes + 1).coerceAtMost(10)
                                                gameModel.updateHoleStroke(player.id, i, next)
                                            }
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(100.dp).padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (currentCourse?.customPar == true) {
                            val totalPar = logic.calculateTotalPar(currentCourse.pars.map { it })
                            Text("Par: $totalPar", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(modifier = Modifier.horizontalScroll(totalScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.width(1.dp).height(64.dp))
                            Box(modifier = Modifier.width(100.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Total: ${player.totalStrokes}")
                            }
                        }
                    }
                }
            }

            if (!showRecap) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showCompleteDialog = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Complete Game")
                    }

                    if (currentCourse?.customAdActive == true) {
                        Button(
                            onClick = {
                                val link = currentCourse.adLink
                                if (!link.isNullOrBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                currentCourse.adTitle?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                                currentCourse.adDescription?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                            }
                            currentCourse.adImage?.takeIf { it.isNotBlank() }?.let {
                                AsyncImage(model = it, contentDescription = "Ad", modifier = Modifier.size(48.dp))
                            }
                        }
                    } else if (logic.shouldShowAds(NetworkChecker.shared.isConnected, isGuest, user?.isPro)) {
                        BannerAdView(
                            adUnitID = "ca-app-pub-8261962597301587/6716977198",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showRecap && passGame != null) {
            RecapGameView(
                game = passGame,
                course = currentCourse,
                isGuest = isGuest,
                onContinue = {
                    val shouldShowAd = logic.shouldShowAds(
                        isConnected = NetworkChecker.shared.isConnected,
                        isGuest = isGuest,
                        isPro = user?.isPro
                    )
                    if (shouldShowAd) {
                        viewManager.navigateToAd(isGuest = isGuest)
                    } else {
                        viewManager.navigateToMain(1)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            containerColor = colorResource(id = R.color.bg)
        ) {
            GameInfoViewAndroid(game = game)
        }
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Game?") },
            text = { Text("You will not be able to change your scores after this point.") },
            confirmButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    gameModel.setCompletedGame(completedGame = true)
                }) {
                    Text("Complete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



