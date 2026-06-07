package com.garrettbutchko.minimate.content.preViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.di.KoinHelperParent
import com.garrettbutchko.minimate.shared.R

@Composable
fun WelcomeView(
    welcomeText: String = "Welcome to MiniMate",
    gradientColors: List<Color> = listOf(
        colorResource(id = R.color.swiftBlue),
        colorResource(id = R.color.swiftGreen)
    )
) {
    val viewModel = remember(welcomeText) { KoinHelperParent.getWelcomeViewModel(welcomeText) }
    val displayedText by viewModel.displayedText.collectAsState()
    val showLoading by viewModel.showLoading.collectAsState()

    DisposableEffect(Unit) {
        viewModel.onAppear()
        onDispose {
            viewModel.onDisappear()
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


                val logoRes = if (isSystemInDarkTheme()) {
                    R.drawable.logo_image
                } else {
                    R.drawable.logo_image_opp
                }
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                )


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

                    LogoLoadingView(size = 60.dp)
                }
            }
        }
    }
}


