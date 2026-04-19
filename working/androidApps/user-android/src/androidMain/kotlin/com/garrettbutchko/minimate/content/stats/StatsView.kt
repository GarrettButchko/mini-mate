package com.garrettbutchko.minimate.content.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.managers.GameManager
import com.garrettbutchko.minimate.managers.ViewManager
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import java.util.Locale

@Composable
fun StatsView(
    userGames: List<Game>,
    isRefreshing: Boolean,
    authModel: AuthViewModel,
    viewManager: ViewManager,
    gameManager: GameManager
) {
    val user by authModel.userModel.collectAsState()
    val averagePerGame = gameManager.averageStrokesPerGame()
    val averagePerHole = gameManager.averageStrokesPerHole()
    val bestGame = gameManager.bestGameStrokes()?.toString() ?: "N/A"
    val worstGame = gameManager.worstGameStrokes()?.toString() ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Stats", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        SectionStatsCard(
            title = "Basic Stats",
            spacing = 10.dp
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                infoText = "Total games played.",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

            StatValueRow(label = "Refreshing", value = if (isRefreshing) "Yes" else "No")
            StatValueRow(label = "Games cached", value = userGames.size.toString())
        }

        SectionStatsCard(
            title = "Misc Stats",
            spacing = 10.dp
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                infoText = "Number of holes with one stroke.",
            )
        }

        if (user == null) {
            Text("Stats will populate after the signed-in user loads.")
        }

        Button(onClick = { viewManager.navigateToMain(1) }) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun StatValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

