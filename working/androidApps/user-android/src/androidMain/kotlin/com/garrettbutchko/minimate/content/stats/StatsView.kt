package com.garrettbutchko.minimate.content.stats

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.BannerAdView
import com.garrettbutchko.minimate.content.helper.BarChartView
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.holeModels.Hole
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import java.util.Locale

@Composable
@Suppress("UNUSED_PARAMETER")
fun StatsView(
    userGames: List<Game>,
    isRefreshing: Boolean,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameManager: GameManager
) {
    val user by authModel.userModel.collectAsState()
    var pickedSection by rememberSaveable { mutableStateOf("Games") }
    var latestFirst by rememberSaveable { mutableStateOf(true) }

    val sortedGames = remember(userGames, latestFirst) {
        if (latestFirst) {
            userGames.reversed()
        } else {
            userGames
        }
    }

    val averagePerGame = gameManager.averageStrokesPerGame()
    val averagePerHole = gameManager.averageStrokesPerHole()
    val bestGame = gameManager.bestGameStrokes()?.toString() ?: "N/A"
    val worstGame = gameManager.worstGameStrokes()?.toString() ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
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
                color = colorResource(id = R.color.mainOpp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (pickedSection == "Games") {
                Row(
                    modifier = Modifier
                        .size(30.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.sub))
                        .clickable { latestFirst = !latestFirst },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (latestFirst) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = "Toggle game sort order",
                        tint = colorResource(id = R.color.mainOpp)
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
            Crossfade(targetState = pickedSection, label = "stats-section") { section ->
                when (section) {
                    "Games" -> GamesSection(
                        games = sortedGames,
                        isRefreshing = isRefreshing,
                        user = user
                    )

                    else -> OverviewSection(
                        gameManager = gameManager,
                        averagePerGame = averagePerGame,
                        averagePerHole = averagePerHole,
                        bestGame = bestGame,
                        worstGame = worstGame
                    )
                }
            }

            if (user == null) {
                Text("Stats will populate after the signed-in user loads.")
            }
        }
    }
}

@Composable
private fun SectionPicker(
    sections: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        sections.forEach { section ->
            val isSelected = section == selected
            Button(
                onClick = { onSelected(section) },
                modifier = Modifier.weight(1f)
                    .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(25.dp)
                ),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else colorResource(id = R.color.sub),
                    contentColor = if (isSelected) Color.White else colorResource(id = R.color.mainOpp)
                )
            ) {
                Text(section)
            }
        }
    }
}

@Composable
private fun GamesSection(
    games: List<Game>,
    isRefreshing: Boolean,
    user: com.garrettbutchko.minimate.dataModels.userModels.UserModel?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isRefreshing) {
            LoadingState()
        } else if (games.isNotEmpty()) {
            games.forEach { game ->
                GameSummaryCard(
                    game = game,
                    currentUserId = user?.googleId
                )
            }
        } else if (user?.gameIDs?.isNotEmpty() == true) {
            LoadingState()
        } else {
            EmptyState()
        }
    }
}

@Composable
private fun OverviewSection(
    gameManager: GameManager,
    averagePerGame: Double,
    averagePerHole: Double,
    bestGame: String,
    worstGame: String
) {
    val spacing = 10.dp

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionStatsCard(
            title = "Basic Stats",
            spacing = spacing
        ) {
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

        BannerAdView(adUnitID = "ca-app-pub-8261962597301587/1525588861",
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(25.dp)
                )
                .height(80.dp)
                .background(
                    color = colorResource(id = R.color.sub),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(14.dp))

        SectionStatsCard(
            title = "Average 18-Hole Game",
            spacing = spacing
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(id = R.color.subTwo))
            ) {
                BarChartView(
                    data = gameManager.averageHoles18(),
                    title = "Average Strokes"
                )
            }
        }

        SectionStatsCard(
            title = "Misc Stats",
            spacing = spacing
        ) {
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

        SectionStatsCard(
            title = "Average 9-Hole Game",
            spacing = spacing
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(id = R.color.subTwo))
            ) {
                BarChartView(
                    data = gameManager.averageHoles9(),
                    title = "Average Strokes"
                )
            }
        }
    }
}

@Composable
private fun GameSummaryCard(
    game: Game,
    currentUserId: String?
) {
    val userPlayer = remember(game, currentUserId) {
        findUserInGame(game, currentUserId)
    }
    val title = game.locationName ?: "Game"
    val subtitle = "${game.numberOfHoles} Holes"
    val score = userPlayer?.totalStrokes?.toString() ?: "N/A"

    SectionStatsCard(
        title = title,
        spacing = 10.dp
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Players",
                value = game.players.size.toString(),
                infoText = "Number of players in this game.",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Your Strokes",
                value = score,
                infoText = "Your total strokes in this game.",
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(id = R.color.subTwo))
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
        CircularProgressIndicator()
        Text(
            text = "Loading Games...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No Games Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Play a game to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun findUserInGame(game: Game, currentUserId: String?): Player? {
    if (currentUserId != null) {
        game.players.firstOrNull { it.userId == currentUserId }?.let { return it }
    }

    game.players.firstOrNull { it.userId == game.hostUserId }?.let { return it }

    return game.players.firstOrNull()
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
