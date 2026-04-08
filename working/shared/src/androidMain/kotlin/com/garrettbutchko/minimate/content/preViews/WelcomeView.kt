package com.garrettbutchko.minimate.content.preViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeView(
    displayedText: String = "Welcome to MiniMate",
    showLoading: Boolean = false,
    gradientColors: List<Color> = listOf(Color.Blue, Color.Green),
    onAppear: () -> Unit = {},
    onDisappear: () -> Unit = {}
) {
    // Equivalent to .onAppear and .onDisappear in SwiftUI
    DisposableEffect(Unit) {
        onAppear()
        onDispose {
            onDisappear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Text(
                text = displayedText,
                fontSize = 32.sp, // Equivalent to .largeTitle
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            // Logo Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logo",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (showLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Trying to reconnect...",
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(60.dp), // Replicates scaleEffect(1.5)
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
}
