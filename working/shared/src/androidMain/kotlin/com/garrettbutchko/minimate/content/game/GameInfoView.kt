package com.garrettbutchko.minimate.content.game.fix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.viewModels.GameInfoViewModel

@Composable
fun GameInfoViewAndroid(
    game: Game,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(game) { GameInfoViewModel(game) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag indicator
        Surface(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(width = 38.dp, height = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(3.dp)
        ) {}

        Text(
            text = "Game Info",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "INFO",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                UserInfoRow(label = "Game ID/Code", value = viewModel.gameId)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(label = "Number of players", value = viewModel.playerCount)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(label = "Number of holes", value = viewModel.holeCount)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(label = "Date Started", value = viewModel.dateStarted)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(label = "Location", value = viewModel.location)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(label = "Course ID", value = viewModel.courseId)
            }
        }
    }
}

@Composable
private fun UserInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
