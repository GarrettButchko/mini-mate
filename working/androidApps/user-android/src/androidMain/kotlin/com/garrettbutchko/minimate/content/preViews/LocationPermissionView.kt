package com.garrettbutchko.minimate.content.preViews

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MobileOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garrettbutchko.minimate.shared.R as SharedR

/**
 * First-run explainer shown before the system location prompt, so the user knows
 * why Mini Mate is asking. Presented as a bottom sheet from the sign-in screen;
 * "Allow Location" hands off to the real Android permission dialog.
 */
@Composable
fun LocationPermissionView(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brand = colorResource(id = SharedR.color.swiftGreen)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = SharedR.color.bg))
            .padding(horizontal = 28.dp)
            .padding(bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(brand.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = brand,
                    modifier = Modifier.size(48.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Location Access",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = SharedR.color.mainOpp)
                )
                Text(
                    text = "Mini Mate uses your location to find mini golf courses near you — " +
                        "it's a core feature of the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = SharedR.color.subTwoOpp),
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colorResource(id = SharedR.color.sub)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Default.Map,
                        color = colorResource(id = SharedR.color.swiftBlue),
                        title = "Find Nearby Courses",
                        description = "Instantly discover mini golf courses around you"
                    )
                    FeatureRow(
                        icon = Icons.Default.Shield,
                        color = colorResource(id = SharedR.color.swiftGreen),
                        title = "Never Stored or Shared",
                        description = "Your location is used only in the moment — never saved"
                    )
                    FeatureRow(
                        icon = Icons.Default.MobileOff,
                        color = colorResource(id = SharedR.color.swiftOrange),
                        title = "No Background Tracking",
                        description = "We only check location while the app is open"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = colorResource(id = SharedR.color.subTwoOpp),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Android will show a permission popup — tap \"While using the app\" to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = SharedR.color.subTwoOpp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onAllow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = SharedR.color.swiftBlue),
                    contentColor = Color.White
                )
            ) {
                Text("Allow Location", style = MaterialTheme.typography.titleMedium)
            }

            TextButton(
                onClick = onSkip,
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    text = "Not Now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = SharedR.color.swiftBlue).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    color: Color,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colorResource(id = SharedR.color.mainOpp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = SharedR.color.subTwoOpp)
            )
        }
    }
}
