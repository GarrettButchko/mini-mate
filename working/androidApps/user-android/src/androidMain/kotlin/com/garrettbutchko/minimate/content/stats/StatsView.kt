package com.garrettbutchko.minimate.content.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Share
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.helper.CourseBannerAdView
import com.garrettbutchko.minimate.content.helper.BarChartView
import com.garrettbutchko.minimate.content.helper.LogoDefault
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.modifiers.ButtonSkim
import com.garrettbutchko.minimate.content.helper.modifiers.SwipeableRow
import com.garrettbutchko.minimate.content.game.GameReviewView
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.holeModels.Hole
import com.garrettbutchko.minimate.extensions.formatted
import com.garrettbutchko.minimate.extensions.toInstant
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.StatsViewModel
import com.garrettbutchko.minimate.utilities.NetworkChecker
import java.util.Locale


@Composable
@Suppress("UNUSED_PARAMETER")
fun StatsView(
    userGames: List<Game>,
    isRefreshing: Boolean,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameManager: GameManager,
    statsViewModel: StatsViewModel
) {
    val user by authModel.userModel.collectAsState()


    var pickedSection by rememberSaveable { mutableStateOf("Games") }
    var latestFirst by rememberSaveable { mutableStateOf(true) }
    var editingGameID by rememberSaveable { mutableStateOf<String?>(null) }

    val sortedGames = remember(userGames, latestFirst) {
        if (latestFirst) {
            userGames.sortedByDescending { it.date.toInstant() }
        } else {
            userGames.sortedBy { it.date.toInstant() }
        }
    }

    val averagePerGame = gameManager.averageStrokesPerGame()
    val averagePerHole = gameManager.averageStrokesPerHole()
    val bestGame = gameManager.bestGameStrokes()?.toString() ?: "N/A"
    val worstGame = gameManager.worstGameStrokes()?.toString() ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (pickedSection == "Games") "Game Stats" else "Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            if (pickedSection == "Games") {
                Row(
                    modifier = Modifier
                        .size(30.dp)
                        .subtleShadow()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { latestFirst = !latestFirst },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (latestFirst) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = "Toggle game sort order",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SectionPicker(
            sections = listOf("Games", "Overview"),
            selected = pickedSection,
            onSelected = { pickedSection = it }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            @OptIn(ExperimentalAnimationApi::class)
            AnimatedContent(
                targetState = pickedSection,
                transitionSpec = {
                    // Games should slide in from the leading (left) edge, Overview from trailing (right)
                    if (targetState == "Games") {

                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth }, // Enter from the left
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(150))) togetherWith
                                (slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },  // Exit to the right
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(150)))

                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth }, // Start from the right (positive)
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(150))) togetherWith
                                (slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth }, // Exit to the left (negative)
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(150)))
                    }.using(SizeTransform(clip = false))
                }
            ) { section ->
                when (section) {
                    "Games" -> GamesSection(
                        games = sortedGames,
                        isRefreshing = isRefreshing,
                        user = user,
                        editingGameID = editingGameID,
                        onEditingGameIDChange = { editingGameID = it },
                        statsViewModel = statsViewModel
                    )

                    else -> {
                        LaunchedEffect(section) {
                            statsViewModel.setEditOn(false)
                        }

                        if (gameManager.hasGames()) {
                            OverviewSection(
                                gameManager = gameManager,
                                averagePerGame = averagePerGame,
                                averagePerHole = averagePerHole,
                                bestGame = bestGame,
                                worstGame = worstGame,
                                user = user
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmptyState(message = "Hit the course to see your stats appear.")
                            }
                        }
                    }
                }
            }

            if (user == null) {
                Text("Stats will populate after the signed-in user loads.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionPicker(
    sections: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape = RoundedCornerShape(50))
    ) {
        sections.forEachIndexed { index, section ->
            SegmentedButton(
                selected = section == selected,
                onClick = { onSelected(section) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondary,
                    activeContentColor = MaterialTheme.colorScheme.onSecondary,
                    activeBorderColor = MaterialTheme.colorScheme.secondary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.secondary
                ),
                label = { Text(section) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GamesSection(
    games: List<Game>,
    isRefreshing: Boolean,
    user: com.garrettbutchko.minimate.dataModels.userModels.UserModel?,
    editingGameID: String?,
    onEditingGameIDChange: (String?) -> Unit,
    statsViewModel: StatsViewModel
) {
    var reviewGame by remember { mutableStateOf<Game?>(null) }
    var deletingIds by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp).padding(bottom = 14.dp)
    ) {
        CourseBannerAdView(
            adUnitID = "ca-app-pub-8261962597301587/1525588861",
            isGuest = user == null,
            isPro = user?.isPro,
            isConnected = NetworkChecker.shared.isConnected
        )

        if (user?.isPro == false && user.gameIDs.size >= 2) {
            PromoCard {
                Text(
                    text = "You’ve reached the free limit. Upgrade to Pro to store more than 2 games.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isRefreshing && games.isEmpty()) {
            LoadingState()
        } else if (games.isNotEmpty()) {
            games.forEach { game ->
                AnimatedVisibility(
                    visible = game.id !in deletingIds,
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SwipeableRow(
                        editingID = editingGameID,
                        onEditingIDChange = onEditingGameIDChange,
                        id = game.id,
                        buttonPressFunction = { reviewGame = game },
                        buttonOne = ButtonSkim(
                            color = Color(0xFF1E88E5),
                            icon = Icons.Filled.Share,
                            string = makeShareableSummary(game)
                        ),
                        deleteFunction = {
                            deletingIds = deletingIds + game.id
                            scope.launch {
                                delay(300)
                                statsViewModel.deleteGame(game.id)
                            }
                        }
                    ) {
                        GameSummaryCard(game = game)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                LogoDefault(topPadding = 0.dp)
            }

        } else if (user?.gameIDs?.isNotEmpty() == true) {
            LoadingState()
        } else {
            EmptyState()
        }
    }

    reviewGame?.let { game ->
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    reviewGame = null
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
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
}

@Composable
private fun PromoCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(25.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun OverviewSection(
    gameManager: GameManager,
    averagePerGame: Double,
    averagePerHole: Double,
    bestGame: String,
    worstGame: String,
    user: com.garrettbutchko.minimate.dataModels.userModels.UserModel?
) {
    val spacing = 10.dp

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp).padding(bottom = 8.dp)) {
        SectionStatsCard(
            title = "Basic Stats",
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    StatCard(
                        title = "Players Faced",
                        value = gameManager.totalPlayersFaced().toString(),
                        infoText = "Number of players other than yourself you played with.",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Holes Played",
                        value = gameManager.totalHolesPlayed().toString(),
                        infoText = "Total holes played (including unfinished holes).",
                        modifier = Modifier.weight(1f)
                    )
                }

                StatCard(
                    title = "Games Played",
                    value = gameManager.totalGamesPlayed().toString(),
                    infoText = "Total games played."
                )

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    StatCard(
                        title = "Strokes/Game",
                        value = String.format(Locale.US, "%.1f", averagePerGame),
                        infoText = "Average strokes per game.",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Strokes/Hole",
                        value = String.format(Locale.US, "%.1f", averagePerHole),
                        infoText = "Average strokes per hole.",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )



            CourseBannerAdView(
                adUnitID = "ca-app-pub-8261962597301587/1525588861",
                isGuest = user == null,
                isPro = user?.isPro,
                isConnected = NetworkChecker.shared.isConnected
            )

        SectionStatsCard(
            title = "Average 18-Hole Game",
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .height(150.dp)
                ) {
                    BarChartView(
                        data = gameManager.averageHoles18(),
                        title = "Average Strokes"
                    )
                }
            }
        )

        SectionStatsCard(
            title = "Misc Stats",
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    StatCard(
                        title = "Best Game",
                        value = bestGame,
                        color = Color(0xFF2E7D32),
                        infoText = "Lowest total strokes in a game.",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Worst Game",
                        value = worstGame,
                        color = Color(0xFFC62828),
                        infoText = "Highest total strokes in a game.",
                        modifier = Modifier.weight(1f)
                    )
                }

                StatCard(
                    title = "Holes-in-One",
                    value = gameManager.holeInOneCount().toString(),
                    infoText = "Number of holes with one stroke."
                )
            }
        )

        SectionStatsCard(
            title = "Average 9-Hole Game",
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .height(150.dp)
                ) {
                    BarChartView(
                        data = gameManager.averageHoles9(),
                        title = "Average Strokes"
                    )
                }
            }
        )
    }
}

@Composable
private fun GameSummaryCard(
    game: Game
) {
    val sortedPlayers = remember(game) {
        game.players.sortedBy { it.totalStrokes }
    }
    val title = game.locationName ?: game.date.formatted()
    val subtitle = if (game.locationName != null) {
        "${game.startTime.formatted()} - ${game.numberOfHoles} Holes"
    } else {
        "${game.numberOfHoles} Holes"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(25.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    maxLines = 1
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (sortedPlayers.isNotEmpty()) {
                if (game.players.size > 1) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 150.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .height(60.dp)
                                        .padding(vertical = 8.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                )
                            }

                            val winner = player.id == sortedPlayers.first().id
                            PhotoIconView(
                                photoURL = player.photoURL,
                                name = if (winner) {
                                    "${player.name}🥇"
                                } else {
                                    player.name
                                },
                                ballColor = player.ballColor,
                                imageSize = 20.dp,
                                backgroundColor = if (winner) {
                                    Color(0xFFFFCC00)
                                } else {
                                   null
                                }
                            )
                        }
                    }
                } else {
                    PhotoIconView(
                        photoURL = sortedPlayers.first().photoURL,
                        name = sortedPlayers.first().name,
                        ballColor = sortedPlayers.first().ballColor,
                        imageSize = 20.dp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .height(130.dp)
        ) {
            BarChartView(
                data = averageStrokes(game),
                title = "Average Strokes"
            )
        }
    }
}


@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LogoLoadingView()
        Text(
            text = "Loading Games...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState(message: String = "Play a game to get started.") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_image),
            contentDescription = "Logo",
            modifier = Modifier
                .size(50.dp)
        )

        Text(
            text = "No Games Yet!",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

    }
}


private fun averageStrokes(game: Game): List<Hole> {
    val holeCount = game.numberOfHoles
    val playerCount = game.players.size
    if (playerCount == 0) return emptyList()

    val sums = IntArray(holeCount)
    game.players.forEach { player ->
        player.holes.forEach { hole ->
            val index = hole.number - 1
            if (index in sums.indices) {
                sums[index] += hole.strokes
            }
        }
    }

    return sums.mapIndexed { index, total ->
        val average = total / playerCount
        Hole(number = index + 1, strokes = average)
    }
}

private fun makeShareableSummary(game: Game): String {
    val lines = mutableListOf(
        "MiniMate Scorecard",
        "Date: ${game.date.formatted()}",
        ""
    )

    game.players.forEach { player ->
        val holeLine = player.holes.joinToString(separator = "") { "|${it.strokes}" }
        lines += "${player.name}: ${player.totalStrokes} strokes (${player.totalStrokes})"
        lines += "Holes $holeLine"
    }

    lines += ""
    lines += "Download MiniMate: https://apps.apple.com/app/id6745438125"
    return lines.joinToString(separator = "\n")
}

