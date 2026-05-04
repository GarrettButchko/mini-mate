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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.game.fix.GameInfoViewAndroid
import com.garrettbutchko.minimate.content.helper.PhotoIconView
import com.garrettbutchko.minimate.content.helper.BarChartView
import com.garrettbutchko.minimate.content.helper.modifiers.SyncScrollStates
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.dataModels.gameModels.Game
import com.garrettbutchko.minimate.modelKotlinExtensions.ballColor
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
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

    var titleHeightPx by remember { mutableStateOf(0) }
    var holesColumnHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // Compute a dp height for the hole rows column when available, otherwise fallback
    val holeColumnHeightDp = if (holesColumnHeightPx > 0) with(density) { holesColumnHeightPx.toDp() } else (viewModel.holeCount * 60).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp).padding(bottom = 16.dp)
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
                // Left column with title; capture its measured height so the course logo can match it
                Column(modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coords ->
                        titleHeightPx = coords.size.height
                    }
                ) {
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
                    // Match logo size to the title column height (converted to dp). Fall back to 50.dp.
                    val logoSize = if (titleHeightPx > 0) with(density) { titleHeightPx.toDp() } else 50.dp
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Course Logo",
                        modifier = Modifier
                            .size(logoSize)
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
                        .background(Color.subVsColor(makeColor = course?.scoreCardColor), RoundedCornerShape(25.dp))
                ) {
                    BarChartView(
                        data = viewModel.averageStrokes(),
                        title = "Average Strokes",
                        paddingReview = true
                    )
                }
            }

            // Score Grid View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space
                    .shadow(8.dp, RoundedCornerShape(25.dp))
                    .background(Color.subVsColor(makeColor = course?.scoreCardColor), RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
            ) {
                // Player Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Name",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                    // Vertical divider between hole numbers and player columns. When inside a
                    // vertically-scrollable parent the height constraint is unbounded, so give
                    // the divider an explicit height based on hole count (each row = 60.dp).
                    VerticalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(holeColumnHeightDp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Row(modifier = Modifier.horizontalScroll(headerScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(holeColumnHeightDp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                PhotoIconView(
                                    photoURL = player.photoURL,
                                    name = player.name,
                                    ballColor = player.ballColor,
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
                    Column(modifier = Modifier.width(100.dp).onGloballyPositioned { coords ->
                        holesColumnHeightPx = coords.size.height
                    }) {
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

                    VerticalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(holeColumnHeightDp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )


                    // Player Score Columns (Scrollable Right Area)
                    Row(modifier = Modifier.horizontalScroll(rowsScrollState)) {
                        game.players.forEachIndexed { index, player ->
                            if (index > 0) VerticalDivider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(holeColumnHeightDp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
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
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
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

            // Footer View - mimic SwiftUI VStack with overlayed dismiss button and share link
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background row containing the share icon. Behavior mirrors SwiftUI:
                    // if showBackToStatsButton is true we add a leading spacer so the share icon
                    // ends up at the trailing edge; otherwise it sits at the leading edge.
                    if (!isInCourseSettings) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (showBackToStatsButton) Spacer(modifier = Modifier.weight(1f))
                            if (com.garrettbutchko.minimate.utilities.NetworkChecker.shared.isConnected) {
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
                    }

                    // Centered dismiss button overlay (appears only when requested)
                    if (showBackToStatsButton) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(60.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.swiftBlue))
                            ) {
                                Text("Dismiss", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Custom Ad Logic (below the overlayed area)
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

    // Use a modal bottom sheet to more closely mimic SwiftUI's sheet presentation
    if (showInfoView) {
        ModalBottomSheet(
            onDismissRequest = { showInfoView = false }
        ) {
            GameInfoViewAndroid(game = game)
        }
    }
}
