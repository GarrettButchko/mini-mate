package com.garrettbutchko.minimate.content.game

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.game.fix.GameInfoViewAndroid
import com.garrettbutchko.minimate.content.helper.BannerAdView
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.StepperControl
import com.garrettbutchko.minimate.content.helper.modifiers.SyncScrollStates
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.logic.ScoreCardBusinessLogic
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
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
        if (game.id.isNotEmpty() && !game.completed) {
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

    var titleHeightPx by remember { mutableStateOf(0) }
    var holesColumnHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val holeColumnHeightDp = if (holesColumnHeightPx > 0) with(density) { holesColumnHeightPx.toDp() } else (holeCount * 60).dp
    val scoreGridDividerColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.mainOpp).copy(alpha = 0.1f)
    val primaryTextColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.mainOpp)
    val secondaryTextColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.subTwoOpp)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coords -> titleHeightPx = coords.size.height }
                ) {
                    Text(
                        text = "Scorecard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    currentCourse?.name?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, fontSize = 14.sp, color = secondaryTextColor)
                    }
                }
                currentCourse?.logo?.takeIf { it.isNotEmpty() }?.let { logoUrl ->
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    val logoSize = if (titleHeightPx > 0) with(density) { titleHeightPx.toDp() } else 50.dp
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Course Logo",
                        modifier = Modifier
                            .size(logoSize)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showInfoSheet = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Game Info",
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(id = com.garrettbutchko.minimate.shared.R.color.swiftBlue)
                    )
                }
            }

            val hasCustomColor = currentCourse?.scoreCardColor != null
            val gridBgColor = Color.subVsColor(makeColor = currentCourse?.scoreCardColor)
            val colorBackground = if (hasCustomColor) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(gridBgColor, RoundedCornerShape(25.dp))
            } else {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(8.dp, RoundedCornerShape(25.dp))
                    .background(gridBgColor, RoundedCornerShape(25.dp))
            }

            // Score Grid
            Column(
                modifier = colorBackground
                    .clip(RoundedCornerShape(25.dp))
            ) {
                // Player Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.width(1.dp).height(holeColumnHeightDp),
                        color = scoreGridDividerColor
                    )
                    Row(modifier = Modifier.horizontalScroll(headerScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider(
                                modifier = Modifier.width(1.dp).height(holeColumnHeightDp),
                                color = scoreGridDividerColor
                            )
                            Box(
                                modifier = Modifier.width(100.dp).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
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

                HorizontalDivider(color = scoreGridDividerColor)

                // Scrollable Score Rows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hole Numbers Column
                    Column(modifier = Modifier.width(100.dp).onGloballyPositioned { coords ->
                        holesColumnHeightPx = coords.size.height
                    }) {
                        for (i in 1..holeCount) {
                            if (i > 1) HorizontalDivider(color = scoreGridDividerColor)
                            Column(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Hole $i", fontWeight = FontWeight.Medium, color = primaryTextColor)
                                if (currentCourse?.customPar == true) {
                                    currentCourse.pars.getOrNull(i - 1)?.let { par ->
                                        Text("Par: $par", style = MaterialTheme.typography.labelSmall, color = secondaryTextColor)
                                    }
                                }
                            }
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier.width(1.dp).height(holeColumnHeightDp),
                        color = scoreGridDividerColor
                    )

                    // Player Score Columns
                    Row(modifier = Modifier.horizontalScroll(rowsScrollState)) {
                        game.players.forEachIndexed { pIndex, player ->
                            if (pIndex > 0) VerticalDivider(
                                modifier = Modifier.width(1.dp).height(holeColumnHeightDp),
                                color = scoreGridDividerColor
                            )
                            Column(modifier = Modifier.width(100.dp)) {
                                for (i in 1..holeCount) {
                                    if (i > 1) HorizontalDivider(color = scoreGridDividerColor)
                                    val strokes = player.holes.firstOrNull { it.number == i }?.strokes ?: 0
                                    StepperControl(
                                        value = strokes,
                                        onDecrement = { gameModel.updateHoleStroke(player.id, i, (strokes - 1).coerceAtLeast(0)) },
                                        onIncrement = { gameModel.updateHoleStroke(player.id, i, (strokes + 1).coerceAtMost(10)) },
                                        textColor = primaryTextColor,
                                        modifier = Modifier.fillMaxWidth().height(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = scoreGridDividerColor)

                // Total Row
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(100.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor
                        )
                        if (currentCourse?.customPar == true && currentCourse.pars.isNotEmpty()) {
                            val totalPar = logic.calculateTotalPar(currentCourse.pars.map { it })
                            Text("Par: $totalPar", style = MaterialTheme.typography.labelSmall, color = secondaryTextColor)
                        }
                    }
                    VerticalDivider(color = scoreGridDividerColor)
                    Row(modifier = Modifier.horizontalScroll(totalScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider(color = scoreGridDividerColor)
                            Box(
                                modifier = Modifier.width(100.dp).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${player.totalStrokes}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryTextColor
                                )
                            }
                        }
                    }
                }
            }

            if (!showRecap) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.width(200.dp).height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.swiftBlue)
                            )
                        ) {
                            Text(
                                "Complete Game",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
            containerColor = MaterialTheme.colorScheme.background
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

