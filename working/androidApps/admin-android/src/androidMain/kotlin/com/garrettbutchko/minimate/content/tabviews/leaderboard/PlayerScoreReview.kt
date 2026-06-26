package com.garrettbutchko.minimate.content.tabviews.leaderboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.holeModels.HoleDTO
import com.garrettbutchko.minimate.dataModels.playerModels.PlayerDTO
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor



//WARNING MIGHT BE USED LATER


/**
 * PlayerScoreReview
 *
 * Detailed scorecard for a specific player — equivalent to the iOS PlayerScoreReview.
 * Renders a list of holes with their pars (if customPar is on) and the player's strokes.
 *
 * @param player the PlayerDTO containing the holes/strokes.
 * @param course the Course containing par info.
 */
@Composable
fun PlayerScoreReview(
    player: PlayerDTO,
    course: Course,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        PlayerScoreHeader(playerName = player.name)
        Spacer(modifier = Modifier.height(8.dp))
        ScoreGrid(player = player, course = course)
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun PlayerScoreHeader(playerName: String) {
    Column {
        // Drag handle — equivalent to SwiftUI Capsule drag indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .width(38.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                thickness = 5.dp
            )
        }
        Text(
            text = "$playerName's Scorecard",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

// ---------------------------------------------------------------------------
// Score Grid
// ---------------------------------------------------------------------------
@Composable
private fun ScoreGrid(player: PlayerDTO, course: Course) {
    val scoreCardColor: Color = course.scoreCardColorDT?.toColor()
        ?: MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(25.dp),
        color = scoreCardColor
    ) {
        Column {
            // Player header row
            PlayerHeaderRow(player = player)

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

            // Scrollable score rows
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val sortedHoles = player.holes.sortedBy { it.number }
                sortedHoles.forEachIndexed { idx, hole ->
                    if (idx > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                    HoleScoreRow(
                        hole = hole,
                        par = if (course.customPar && course.pars.size >= hole.number) {
                            course.pars[hole.number - 1]
                        } else null
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

            // Footer / Total Row
            TotalRow(player = player, course = course)
        }
    }
}

@Composable
private fun PlayerHeaderRow(player: PlayerDTO) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Surface(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = player.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = player.email ?: "Guest Player",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        VerticalDivider(
            modifier = Modifier.height(60.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        )

        Column(
            modifier = Modifier.width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${player.totalStrokes}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Strokes",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun HoleScoreRow(hole: HoleDTO, par: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hole ${hole.number}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            if (par != null) {
                Text(
                    text = "Par: $par",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.height(60.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${hole.strokes}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TotalRow(player: PlayerDTO, course: Course) {
    val totalPar = if (course.customPar) course.pars.sum() else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 16.dp)
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            if (totalPar != null) {
                Text(
                    text = "Par: $totalPar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.height(60.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Total: ${player.totalStrokes}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
