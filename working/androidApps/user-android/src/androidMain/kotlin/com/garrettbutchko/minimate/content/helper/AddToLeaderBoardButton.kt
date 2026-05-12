package com.garrettbutchko.minimate.content.helper

import co.touchlab.kermit.Logger

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.playerModels.Player
import com.garrettbutchko.minimate.repositories.CourseLeaderboardRepository

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddToLeaderBoardButton(
    course: Course?,
    player: Player,
    isProfaneName: Boolean = false, // Passed externally if ProfanityFilter is not in KMP
    courseLeaderBoardRepo: CourseLeaderboardRepository = remember { CourseLeaderboardRepository() }
) {
    var added by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Match SwiftUI's visibility logic
    val shouldShow = course != null &&
            !isProfaneName &&
            !player.incomplete &&
            course.tier >= 2 &&
            player.email != null &&
            course.customPar &&
            course.leaderBoardActive

    if (shouldShow) {
        AnimatedContent(
            targetState = added,
            transitionSpec = {
                if (targetState) {
                    // Animating to "Added" state (scale + opacity)
                    (scaleIn() + fadeIn()).togetherWith(fadeOut())
                } else {
                    // Animating to Action Button state
                    (fadeIn() + slideInVertically { -it }).togetherWith(fadeOut())
                }
            },
            label = "leaderboard_button_anim"
        ) { isAdded ->
            if (isAdded) {
                // --- Confirmation View ---
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.size(120.dp, 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Added",
                        tint = Color.Green,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Added",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
            } else {
                // --- Action Button ---
                Button(
                    onClick = {
                        course.id.let { courseId ->
                            coroutineScope.launch {
                                isSubmitting = true
                                try {
                                    val result = courseLeaderBoardRepo.submitScore(courseId, player)
                                    if (result.isSuccess) {
                                        added = true
                                        Logger.i("Score submitted successfully!")
                                    } else {
                                        Logger.i("Submission failed: ${result.exceptionOrNull()?.message}")
                                    }
                                } catch (e: Exception) {
                                    Logger.i("Network or System Error: ${e.localizedMessage}")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubmitting) Color.Gray else Color.Blue,
                        disabledContainerColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(120.dp, 20.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Leaderboard",
                                fontSize = 10.sp, // Using slightly smaller font to fit 120dp constraint
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
