package com.garrettbutchko.minimate.content.tabviews.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.garrettbutchko.minimate.admin.BuildConfig
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.garrettbutchko.minimate.content.helper.modifiers.SwipeableRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.playerModels.LeaderboardEntry
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel

/**
 * LeaderBoardView
 *
 * Sheet-level leaderboard screen shown from MainView.
 * iOS presented this as a sheet; Android renders it as a full composable
 * suitable for either ModalBottomSheet or a NavHost destination.
 *
 * Tier gates match iOS:
 *  - tier < 2 → upgrade paywall
 *  - tier >= 2 && !leaderBoardActive → inactive state
 *  - tier >= 2 && leaderBoardActive && count <= 3 → "not enough data"
 *  - tier >= 2 && leaderBoardActive && count > 3 → full leaderboard
 */
@Composable
fun LeaderBoardView(
    vm: LeaderBoardViewModel,
    course: Course?,
    modifier: Modifier = Modifier
) {
    val leaderboard by vm.allTimeLeaderboard.collectAsState()

    LaunchedEffect(course?.id) {
        course?.id?.let { vm.onAppear(courseID = it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fixed title at the top
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "All Time Leaderboard",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            if (course != null) {
                when {
                    course.tier < 2 -> LeaderboardUpgradeView()
                    !course.leaderBoardActive -> LeaderboardInactiveView()
                    leaderboard.size <= 3 -> LeaderboardNotEnoughDataView()
                    else -> LeaderboardContent(
                        entries = leaderboard,
                        vm = vm,
                        courseId = course.id
                    )
                }

                if (BuildConfig.DEBUG) {
                    LeaderboardDebugTools(vm = vm, courseId = course.id)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Full leaderboard — podium + ranked list rows 4–25
// ---------------------------------------------------------------------------
@Composable
private fun LeaderboardContent(
    entries: List<LeaderboardEntry>,
    vm: LeaderBoardViewModel,
    courseId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        BouncingBallsView(
            topThreePlayers = entries.take(3),
            vm = vm,
            courseId = courseId
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rows 4–25 in a scrollable list
        val listEntries = entries.drop(3).take(22) // ranks 4..25
        var editingPlayerID by remember { mutableStateOf<String?>(null) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                itemsIndexed(listEntries) { idx, player ->
                    val rank = idx + 4 // ranks start at 4 after the podium
                    SwipeableRow(
                        editingID = editingPlayerID,
                        onEditingIDChange = { editingPlayerID = it },
                        id = player.id,
                        buttonPressFunction = {},
                        deleteFunction = {
                            vm.deletePlayerEntry(courseID = courseId, playerID = player.id)
                            editingPlayerID = null
                        }
                    ) {
                        PlayerRow(player = player, rank = rank)
                    }
                    if (idx < listEntries.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlayerRow(player: LeaderboardEntry, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${player.totalStrokes}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "strokes",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Empty / gate states
// ---------------------------------------------------------------------------

@Composable
private fun LeaderboardNotEnoughDataView() {
    InfoStateScaffold {
        Icon(
            imageVector = Icons.Default.SportsScore,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Not enough Player Data Yet",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Your course is live, but no rounds have been completed by players yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoStateFeatureItem(icon = Icons.Default.QrCode, title = "Share Course", description = "Give players your course QR code to start tracking scores.")
            InfoStateFeatureItem(icon = Icons.AutoMirrored.Filled.TrendingUp, title = "Live Monitoring", description = "Once play begins, rankings will appear here in real-time.")
            InfoStateFeatureItem(icon = Icons.Default.Shield, title = "Score Validation", description = "Scores are automatically verified by the MiniMate system.")
        }
    }
}

@Composable
private fun LeaderboardInactiveView() {
    InfoStateScaffold {
        Icon(
            imageVector = Icons.Default.PauseCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Leaderboard Inactive",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "This leaderboard is currently set to inactive. Players cannot see the leaderboard or submit new scores.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LeaderboardUpgradeView() {
    InfoStateScaffold {
        // Icon with lock overlay
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFFF9800).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFFFF9800)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Unlock Leaderboards",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Take your course to the next level. Let players compete for the top spot and track their all-time best scores.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoStateFeatureItem(icon = Icons.Default.EmojiEvents, title = "All-Time Rankings", description = "Automated leaderboards for every player.")
            InfoStateFeatureItem(icon = Icons.Default.Shield, title = "Profanity Free", description = "Name filtering and score validation to keep your course family-friendly.")
        }
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFF9800), Color(0xFFFFCC00))),
                    RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "Upgrade to Tier 2",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** Shared scaffold for the various leaderboard gate states. */
@Composable
fun InfoStateScaffold(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        content()
    }
}

@Composable
fun InfoStateFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color(0xFFFF9800).copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFFF9800)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Debug tools — only rendered in DEBUG builds
// ---------------------------------------------------------------------------
@Composable
private fun LeaderboardDebugTools(vm: LeaderBoardViewModel, courseId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⚙ Debug",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.seedDebugData(courseId) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
            ) {
                Text("Seed 25 Players")
            }
            Button(
                onClick = { vm.clearDebugData(courseId) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
            ) {
                Text("Clear All")
            }
        }
    }
}
