package com.garrettbutchko.minimate.content.game

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.helper.AddToLeaderBoardButton
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.logic.PlayerStanding
import com.garrettbutchko.minimate.logic.RecapViewBusinessLogic
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecapGameView(
    game: Game?,
    course: Course?,
    isGuest: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logic = remember { RecapViewBusinessLogic() }
    val sortedPlayers = remember(game) { logic.sortPlayers(game) }
    var gameReview by remember { mutableStateOf<Game?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showConfetti = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Review of Game", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Congratulations!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (sortedPlayers.size > 1) {
                PlayerStandingCard(
                    player = sortedPlayers[0],
                    place = PlayerStanding.FIRST,
                    course = course,
                    onlyPlayer = false
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    PlayerStandingCard(
                        player = sortedPlayers[1],
                        place = PlayerStanding.SECOND,
                        course = course,
                        onlyPlayer = false,
                        modifier = Modifier.weight(1f)
                    )
                    if (sortedPlayers.size > 2) {
                        PlayerStandingCard(
                            player = sortedPlayers[2],
                            place = PlayerStanding.THIRD,
                            course = course,
                            onlyPlayer = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (sortedPlayers.size > 3) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedPlayers.size - 3) { index ->
                            val player = sortedPlayers[index + 3]
                            PlayerStandingCard(
                                player = player,
                                place = null,
                                course = course,
                                onlyPlayer = false
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                if (sortedPlayers.isNotEmpty()) {
                    PlayerStandingCard(
                        player = sortedPlayers[0],
                        place = PlayerStanding.FIRST,
                        course = course,
                        onlyPlayer = true
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { gameReview = game }) {
                    Text("Review Game")
                }
            }

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (isGuest) "Continue" else "Back to Home")
            }
        }

        if (showConfetti) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        angle = Angle.TOP,
                        spread = 50,
                        speed = 28f,
                        maxSpeed = 42f,
                        damping = 0.9f,
                        emitter = Emitter(duration = 180, TimeUnit.MILLISECONDS).max(80),
                        position = Position.Relative(0.5, 0.0),
                        shapes = listOf(Shape.Square, Shape.Circle)
                    ),
                    Party(
                        angle = 60,
                        spread = 40,
                        speed = 24f,
                        maxSpeed = 36f,
                        emitter = Emitter(duration = 160, TimeUnit.MILLISECONDS).max(60),
                        position = Position.Relative(0.15, 0.0)
                    ),
                    Party(
                        angle = 120,
                        spread = 40,
                        speed = 24f,
                        maxSpeed = 36f,
                        emitter = Emitter(duration = 160, TimeUnit.MILLISECONDS).max(60),
                        position = Position.Relative(0.85, 0.0)
                    )
                )
            )
        }
    }

    gameReview?.let { review ->
        ModalBottomSheet(onDismissRequest = { gameReview = null }) {
            GameReviewView(game = review)
        }
    }
}

@Composable
private fun PlayerStandingCard(
    player: Player,
    place: PlayerStanding?,
    course: Course?,
    onlyPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val logic = remember { RecapViewBusinessLogic() }

    val medalColor = when (place) {
        PlayerStanding.FIRST -> Color(0xFFFFD54F).copy(alpha = 0.45f)
        PlayerStanding.SECOND -> Color.LightGray.copy(alpha = 0.45f)
        PlayerStanding.THIRD -> Color(0xFFD7A86E).copy(alpha = 0.45f)
        null -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconBackground = if (place != null && !onlyPlayer) medalColor else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (place != null && !onlyPlayer) medalColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhotoIconView(
                    photoURL = player.photoURL,
                    name = logic.formatPlayerName(player.name, place, onlyPlayer),
                    ballColor = player.ballColor,
                    imageSize = logic.getImageSize(place).dp,
                    backgroundColor = iconBackground,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = player.totalStrokes.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            AddToLeaderBoardButton(course = course, player = player)
        }
    }
}

