package com.garrettbutchko.minimate.content.tabviews.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.dataModels.playerModels.LeaderboardEntry
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel

/**
 * BouncingBallsView
 *
 * Renders the top-3 podium for the leaderboard — equivalent to the iOS BouncingBallsView.
 * "Bouncing balls" animation does not have a direct compose equivalent for the ball physics;
 * this version uses static podium columns matching the visual structure without physics bounce.
 * An animated entry transition via AnimatedVisibility can be added in a follow-up pass.
 *
 * @param topThreePlayers sorted leaderboard entries (positions 1, 2, 3).
 * @param vm              the LeaderBoardViewModel needed to delete players.
 * @param courseId        passed through for the delete action.
 * @param containerHeight visual height for the entire podium — default 220 dp.
 */
@Composable
fun BouncingBallsView(
    topThreePlayers: List<LeaderboardEntry>,
    vm: LeaderBoardViewModel,
    courseId: String,
    modifier: Modifier = Modifier,
    containerHeight: Dp = 220.dp
) {
    // iOS shows places 2-1-3 (left to right), matching podium visual convention
    val capped = topThreePlayers.take(3)

    if (capped.isEmpty()) {
        PodiumEmptyState(containerHeight = containerHeight * 0.8f, modifier = modifier)
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(containerHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom
        ) {
            // Display order: 2nd place, 1st place, 3rd place (podium style)
            PodiumSlot(rank = 2, players = capped, vm = vm, courseId = courseId, modifier = Modifier.weight(1f))
            PodiumSlot(rank = 1, players = capped, vm = vm, courseId = courseId, modifier = Modifier.weight(1f))
            PodiumSlot(rank = 3, players = capped, vm = vm, courseId = courseId, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PodiumEmptyState(containerHeight: Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Leaderboard updates will show here",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun PodiumSlot(
    rank: Int,
    players: List<LeaderboardEntry>,
    vm: LeaderBoardViewModel,
    courseId: String,
    modifier: Modifier
) {
    val index = rank - 1
    val player = players.getOrNull(index)
    if (player != null) {
        PodiumColumn(
            player = player,
            rank = rank,
            vm = vm,
            courseId = courseId,
            modifier = modifier
        )
    } else {
        Spacer(modifier = modifier)
    }
}

@Composable
private fun PodiumColumn(
    player: LeaderboardEntry,
    rank: Int,
    vm: LeaderBoardViewModel,
    courseId: String,
    modifier: Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // No ripple to match SwiftUI Button style here if desired, or keep default
            onClick = { showDeleteDialog = true }
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        val bubbleSize = bubbleSize(rank)
        val podiumColor = podiumColor(rank)

        // Avatar bubble
        Box(
            modifier = Modifier
                .size(bubbleSize)
                .clip(CircleShape)
                .background(podiumColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder avatar — iOS uses "logoOpp" asset
            Text(
                text = player.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = podiumColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Player name + strokes
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${player.totalStrokes} strokes",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Podium block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight(rank))
                .clip(RoundedCornerShape(20.dp))
                .background(podiumColor.copy(alpha = if (rank == 1) 0.26f else 0.18f)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = rankTitle(rank),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Player?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deletePlayerEntry(courseID = courseId, playerID = player.id)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun bubbleSize(rank: Int): Dp = when (rank) {
    1 -> 85.dp
    2 -> 70.dp
    3 -> 55.dp
    else -> 60.dp
}

private fun podiumHeight(rank: Int): Dp = when (rank) {
    1 -> 92.dp
    2 -> 70.dp
    3 -> 56.dp
    else -> 56.dp
}

private fun podiumColor(rank: Int): Color = when (rank) {
    1 -> Color(0xFFFFCC00) // yellow
    2 -> Color(0xFF8E8E93) // gray
    3 -> Color(0xFF8B4513) // brown
    else -> Color(0xFF8E8E93)
}

private fun rankTitle(rank: Int): String = when (rank) {
    1 -> "First"
    2 -> "Second"
    3 -> "Third"
    else -> "Top"
}
