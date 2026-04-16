package com.garrettbutchko.minimate.content

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.garrettbutchko.minimate.shared.R

@Composable
fun TitleView(
    colors: List<Color>? = null,
    isManager: Boolean = false,
    modifier: Modifier = Modifier
) {
    val defaultColors = listOf(
        colorResource(id = R.color.swiftRed),
        colorResource(id = R.color.swiftOrange), // Orange
        colorResource(id = R.color.swiftYellow),
        colorResource(id = R.color.swiftGreen),
        colorResource(id = R.color.swiftBlue),
        colorResource(id = R.color.swiftPurple), // Purple
        colorResource(id = R.color.swiftIndigo), // Indigo
        colorResource(id = R.color.swiftPink)  // Pink
    )
    val actualColors = colors ?: defaultColors

    Box(
        modifier = modifier.size(220.dp, 150.dp),
        contentAlignment = Alignment.Center
    ) {


        // Foreground Title text
        Column(
            modifier = Modifier.width(if (isManager) 150.dp else 130.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mini",
                    fontSize = if (isManager) 28.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Corresponds to .mainOpp
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isManager) "Manager" else "Mate",
                    fontSize = if (isManager) 28.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Orbiting background
        OrbitingCirclesView(
            colors = actualColors,
            modifier = Modifier
                .size(220.dp, 150.dp)
                .clipToBounds()
        )
    }
}

data class OrbitingCircle(
    val angleOffset: Float,
    val size: Float,
    val speedMultiplier: Float,
    val verticalScale: Float,
    val color: Color
)

@Composable
fun OrbitingCirclesView(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var orbitingCircles by remember { mutableStateOf<List<OrbitingCircle>>(emptyList()) }

    // Generate circles on appear or when colors change
    LaunchedEffect(colors) {
        orbitingCircles = (0 until 8).map { index ->
            OrbitingCircle(
                angleOffset = index * (360f / 8f),
                size = Random.nextFloat() * 30f + 20f, // 10..20
                speedMultiplier = Random.nextFloat() * 0.4f + 0.8f, // 0.8..1.2
                verticalScale = Random.nextFloat() * 30f + 30f, // 30..60
                color = colors[index % colors.size]
            )
        }
    }

    // Animation timer loop
    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val startTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                // Calculate elapsed time in seconds
                time = (frameTimeNanos - startTime) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val baseRotation = time * 50f

        orbitingCircles.forEach { circle ->
            val angle = baseRotation * circle.speedMultiplier + circle.angleOffset
            val radians = Math.toRadians(angle.toDouble()).toFloat()

            // Calculate positions based on geometry
            val x = (width * 0.4f) * cos(radians)
            val y = (circle.verticalScale / 60f * (height * 0.4f)) * sin(radians)

            val scale = 0.5f + 0.5f * (1f + sin(radians))

            drawCircle(
                color = circle.color,
                radius = (circle.size * scale) / 2f,
                center = Offset(x = width / 2f + x, y = height / 2f + y),
                alpha = 0.4f + 0.6f * scale
            )
        }
    }
}
