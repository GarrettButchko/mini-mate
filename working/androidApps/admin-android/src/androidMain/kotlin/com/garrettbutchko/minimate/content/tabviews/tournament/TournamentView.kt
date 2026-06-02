package com.garrettbutchko.minimate.content.tabviews.tournament

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.tabviews.leaderboard.InfoStateFeatureItem
import com.garrettbutchko.minimate.content.tabviews.leaderboard.InfoStateScaffold

private val gradientColors = listOf(
    Color(0xFFFF9800).copy(alpha = 0.8f),
    Color(0xFF34C759).copy(alpha = 0.8f)
)

private fun Modifier.tournamentGradient(): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val brush = Brush.horizontalGradient(gradientColors)
        onDrawWithContent {
            drawContent()
            drawRect(brush, blendMode = BlendMode.SrcIn)
        }
    }

@Composable
fun TournamentView(modifier: Modifier = Modifier) {
    Column {

        Spacer(modifier = Modifier.weight(0.1f))

        InfoStateScaffold(modifier = modifier.weight(1f)) {
            // Large gradient icon — matches iOS medal.fill with orange→green gradient, no circle bg
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .tournamentGradient(),
                tint = Color.White
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tournaments Coming Soon",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Organize and manage mini golf tournaments with leaderboards, player tracking, and real-time scoring.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoStateFeatureItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Tournament Brackets",
                    description = "Create and manage tournament brackets"
                )
                InfoStateFeatureItem(
                    icon = Icons.Default.Group,
                    title = "Player Management",
                    description = "Invite and track player performance"
                )
                InfoStateFeatureItem(
                    icon = Icons.Default.BarChart,
                    title = "Live Leaderboards",
                    description = "Real-time scoring and rankings"
                )
            }

            // Badge — gradient applied to entire row content (no background), matches iOS foregroundStyle gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .tournamentGradient(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Available in a future update",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.1f))
    }
}
