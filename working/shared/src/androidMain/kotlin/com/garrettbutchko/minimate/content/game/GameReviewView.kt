package com.garrettbutchko.minimate.content.game

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.game.fix.GameInfoViewAndroid
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.BarChartView
import com.garrettbutchko.minimate.content.helper.modifiers.SyncScrollStates
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.viewModels.GameReviewViewModel

fun parseColor(hex: String?): Color {
    if (hex.isNullOrEmpty()) return Color.DarkGray
    return try {
        Color(android.graphics.Color.parseColor(if (!hex.startsWith("#")) "#$hex" else hex))
    } catch (e: Exception) {
        Color.DarkGray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameReviewView(
    game: Game,
    showBackToStatsButton: Boolean = false,
    isInCourseSettings: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val viewModel = remember(game) { GameReviewViewModel(game) }
    val course by viewModel.course.collectAsState()
    var showInfoView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCourse()
    }

    val context = LocalContext.current

    val headerScrollState = rememberScrollState()
    val rowsScrollState = rememberScrollState()
    val totalScrollState = rememberScrollState()

    // Synchronize horizontal scrolling for the scorecard grid
    SyncScrollStates(headerScrollState, rowsScrollState, totalScrollState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scorecard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    game.locationName?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp
                        )
                    }
                }

                course?.logo?.takeIf { it.isNotEmpty() }?.let { logoUrl ->
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Course Logo",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { showInfoView = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(24.dp))
                }
            }

            // Average Strokes Chart
            if (!isInCourseSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .shadow(8.dp, RoundedCornerShape(25.dp))
                        .background(parseColor(course?.scoreCardColorDT).copy(alpha = 0.3f), RoundedCornerShape(25.dp))
                ) {
                    BarChartView(
                        data = viewModel.averageStrokes(),
                        title = "Average Strokes",
                        // paddingReview = true
                    )
                }
            }

            // Score Grid View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space
                    .shadow(8.dp, RoundedCornerShape(25.dp))
                    .background(parseColor(course?.scoreCardColorDT).copy(alpha = 0.3f), RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
            ) {
                // Player Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Name",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .width(100.dp)
                            .padding(start = 16.dp)
                    )
                    VerticalDivider()
                    Row(modifier = Modifier.horizontalScroll(headerScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider()
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                PhotoIconView(
                                    photoURL = player.photoURL,
                                    name = player.name,
                                    ballColor = parseColor(player.ballColorDT),
                                    imageSize = 30.dp,
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
                
                HorizontalDivider()

                // Scrollable Score Rows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hole Numbers (Sticky Left Column)
                    Column(modifier = Modifier.width(100.dp)) {
                        for (i in 1..viewModel.holeCount) {
                            if (i > 1) HorizontalDivider()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Hole $i", fontWeight = FontWeight.Medium)
                                if (course?.customPar == true && course?.pars != null && course!!.pars.size >= i) {
                                    Text("Par: ${course!!.pars[i - 1]}", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    VerticalDivider()

                    // Player Score Columns (Scrollable Right Area)
                    Row(modifier = Modifier.horizontalScroll(rowsScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider()
                            Column(modifier = Modifier.width(100.dp)) {
                                val sortedHoles = player.holes.sortedBy { it.number }
                                sortedHoles.forEachIndexed { hIndex, hole ->
                                    if (hIndex > 0) HorizontalDivider()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${hole.strokes}")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Total Row
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
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Total", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        if (course?.customPar == true && course?.pars?.isNotEmpty() == true) {
                            Text("Par: ${course!!.pars.sum()}", fontSize = 12.sp)
                        }
                    }
                    VerticalDivider()
                    Row(modifier = Modifier.horizontalScroll(totalScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider()
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Total: ${player.totalStrokes}")
                            }
                        }
                    }
                }
            }

            // Footer View
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isInCourseSettings) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (showBackToStatsButton) Arrangement.SpaceBetween else Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBackToStatsButton) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.height(50.dp).width(150.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                            ) {
                                Text("Dismiss", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        IconButton(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, viewModel.shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Game"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(28.dp))
                        }
                    }
                }

                // Custom Ad Logic
                if (isInCourseSettings) {
                    val currentCourse = course
                    if (currentCourse?.customAdActive == true) {
                        Button(
                            onClick = {
                                val link = currentCourse.adLink
                                if (!link.isNullOrEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    currentCourse.adTitle?.let {
                                        Text(it, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                    currentCourse.adDescription?.let {
                                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    }
                                }
                                currentCourse.adImage?.takeIf { it.isNotEmpty() }?.let { adImage ->
                                    Spacer(modifier = Modifier.width(16.dp))
                                    AsyncImage(
                                        model = adImage,
                                        contentDescription = "Ad Image",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    } else if (currentCourse?.customAdActive == false) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Google Ad Here (If Not Pro User)", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showInfoView) {
        AlertDialog(
            onDismissRequest = { showInfoView = false },
            confirmButton = {
                TextButton(onClick = { showInfoView = false }) {
                    Text("Close")
                }
            },
            text = {
                GameInfoViewAndroid(game = game)
            }
        )
    }
}
