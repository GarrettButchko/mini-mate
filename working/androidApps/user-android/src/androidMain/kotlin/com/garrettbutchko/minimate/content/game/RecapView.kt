package com.garrettbutchko.minimate.content.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    val stableCourse = remember { course }
    val sortedPlayers = remember(game) { logic.sortPlayers(game) }
    var gameReview by remember { mutableStateOf<Game?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showConfetti = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Review of Game",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.5f)
                )
                Text(
                    "Congratulations!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (sortedPlayers.size > 1) {
                PlayerStandingCard(
                    player = sortedPlayers[0],
                    place = PlayerStanding.FIRST,
                    course = stableCourse,
                    onlyPlayer = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerStandingCard(
                        player = sortedPlayers[1],
                        place = PlayerStanding.SECOND,
                        course = stableCourse,
                        onlyPlayer = false,
                        modifier = Modifier.weight(1f)
                    )
                    if (sortedPlayers.size > 2) {
                        PlayerStandingCard(
                            player = sortedPlayers[2],
                            place = PlayerStanding.THIRD,
                            course = stableCourse,
                            onlyPlayer = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (sortedPlayers.size > 3) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedPlayers.drop(3)) { player ->
                            PlayerStandingCard(player = player, place = null, course = stableCourse, onlyPlayer = false)
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
                        course = stableCourse,
                        onlyPlayer = true
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { gameReview = game },
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Review Game",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.padding(bottom = 24.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 20.dp, horizontal = 30.dp)
            ) {
                Icon(
                    imageVector = if (isGuest) Icons.Default.AccountCircle else Icons.Default.Home,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGuest) "Back to Sign In Menu" else "Go Back to Main Menu",
                    fontWeight = FontWeight.Bold
                )
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
        ModalBottomSheet(
            onDismissRequest = { gameReview = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null
        ) {
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
        PlayerStanding.FIRST -> Color(0xFFFFD54F)
        PlayerStanding.SECOND -> Color.Gray
        PlayerStanding.THIRD -> Color(0xFFD7A86E)
        null -> Color.Transparent
    }

    val cardModifier = when {
        place != null && !onlyPlayer ->
            Modifier.background(medalColor.copy(alpha = 0.2f), RoundedCornerShape(25.dp))
        place != null && onlyPlayer ->
            Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(25.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(25.dp))
        else ->
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(25.dp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(cardModifier)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            PhotoIconView(
                photoURL = player.photoURL,
                name = logic.formatPlayerName(player.name, place, onlyPlayer),
                ballColor = player.ballColor,
                imageSize = logic.getImageSize(place).dp,
                backgroundColor = if (place != null && !onlyPlayer) medalColor.copy(alpha = 0.45f) else null,
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.Start)
            )
            Text(
                text = player.totalStrokes.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                textAlign = TextAlign.End
            )
        }
        AddToLeaderBoardButton(course = course, player = player)
    }
}
