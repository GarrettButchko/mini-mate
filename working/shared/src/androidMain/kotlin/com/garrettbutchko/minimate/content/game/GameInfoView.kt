package com.garrettbutchko.minimate.content.game.fix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.viewModels.GameInfoViewModel
import com.garrettbutchko.minimate.content.helper.InfoRow
import com.garrettbutchko.minimate.content.helper.SectionCard

@Composable
fun GameInfoViewAndroid(
    game: Game,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(game) { GameInfoViewModel(game) }
    val bgColor = colorResource(id = R.color.bg)
    val mainOppColor = colorResource(id = R.color.mainOpp)
    val subColor = colorResource(id = R.color.sub)
    val subOppColor = colorResource(id = R.color.subOpp)
    val subThreeOppColor = colorResource(id = R.color.subThreeOpp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Game Info",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = mainOppColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionCard("Info") {
                InfoRow(label = "Game ID/Code", value = viewModel.gameId, mainOppColor = mainOppColor, subOppColor = subOppColor)
                HorizontalDivider()
                InfoRow(label = "Number of players", value = viewModel.playerCount, mainOppColor = mainOppColor, subOppColor = subOppColor)
                HorizontalDivider()
                InfoRow(label = "Number of holes", value = viewModel.holeCount, mainOppColor = mainOppColor, subOppColor = subOppColor)
                HorizontalDivider()
                InfoRow(label = "Date Started", value = viewModel.dateStarted, mainOppColor = mainOppColor, subOppColor = subOppColor)
                HorizontalDivider()
                InfoRow(label = "Location", value = viewModel.location, mainOppColor = mainOppColor, subOppColor = subOppColor)
                HorizontalDivider()
                InfoRow(label = "Course ID", value = viewModel.courseId, mainOppColor = mainOppColor, subOppColor = subOppColor)
            }
        }
    }
}
