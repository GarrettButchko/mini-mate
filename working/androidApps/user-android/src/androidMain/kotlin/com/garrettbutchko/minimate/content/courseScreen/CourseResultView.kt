package com.garrettbutchko.minimate.content.courseScreen

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.courseModels.SocialPlatform
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.shared.R
import com.garrettbutchko.minimate.viewModels.CourseViewModel

@Composable
fun CourseResultView(
    selectedCourse: Course,
    selectedMapItem: MapItemDTO?,
    viewModel: CourseViewModel,
    onBackToResults: () -> Unit,
    onClaimCourse: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 55.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CourseDirectionsButton(viewModel = viewModel)
            }

            if (selectedCourse.isSupported) {
                item {
                    CourseSupportedLocationCard(locationName = selectedMapItem?.name ?: selectedCourse.name)
                }
                if (selectedCourse.socialLinks.isNotEmpty()) {
                    item {
                        CourseSocialMediaCard(course = selectedCourse)
                    }
                }
            } else {
                item {
                    CourseClaimButton(
                        onClaim = onClaimCourse
                    )
                }
            }

            item {
                CourseContactInfoCard(mapItem = selectedMapItem)
            }

            item {
                CourseLocationInfoCard(mapItem = selectedMapItem)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            CourseResultViewHeader(courseName = selectedMapItem?.name ?: selectedCourse.name) {
                onBackToResults()
            }
        }
    }
}

@Composable
fun CourseResultViewHeader(
    courseName: String?,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.size(width = 40.dp, height = 30.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(18.dp) // Scaled down to fit your 30.dp height
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = courseName ?: "Course",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
fun CourseDirectionsButton(viewModel: CourseViewModel) {
    Button(
        onClick = { viewModel.getDirections() },
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp), // Height to accommodate the VStack-style layout
        shape = RoundedCornerShape(15.dp), // Matches your cornerRadius: 15
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Directions, // Equivalent to "arrow.turn.up.right"
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Get Directions",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CourseSupportedLocationCard(locationName: String?) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .padding(14.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEEE5FF))) {
            Image(
                painter = painterResource(id = R.drawable.logo_image_opp),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("Supported Location", style = MaterialTheme.typography.bodyLarge)
            if (locationName != null) {
                Text("$locationName has official MiniMate data (par + more).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CourseSocialMediaCard(course: Course) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Social Media",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Horizontal Scroll
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                course.socialLinks.forEach { link ->
                    if (link.platform != SocialPlatform.DEFAULT) {
                        // The individual "Link" Surface
                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, link.url.toUri())
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = link.platform.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Platform Icon
                                Icon(
                                    painter = painterResource(id = link.platform.hashCode()),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseContactInfoCard(mapItem: MapItemDTO?) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Phone, // Replaces 📞
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Contact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            val phoneNumber = mapItem?.phoneNumber
            val websiteUrl = mapItem?.url
            val hasPhone = !phoneNumber.isNullOrBlank()
            val hasWebsite = websiteUrl?.contains("http") == true

            // Call Button
            if (hasPhone) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)), // Material Green
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Call $phoneNumber", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Website Button
            if (hasWebsite) {
                Button(
                    onClick = {
                        websiteUrl.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)), // Material Blue
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Visit Website", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Empty State
            if (!hasPhone && !hasWebsite) {
                Text(
                    text = "No contact info available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CourseLocationInfoCard(mapItem: MapItemDTO?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn, // The standard "mappin" equivalent
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = mapItem?.name ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = mapItem?.address?.shortAddress ?: mapItem?.address?.fullAddress ?: "Address unknown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun CourseClaimButton(
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClaim, // Surface supports onClick directly
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Adjust padding to match your layout
        shape = RoundedCornerShape(12.dp), // Closest to "Material.regular"
        shadowElevation = 4.dp // Matches your .cardShadow()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp), // This is the internal .padding() from your SwiftUI code
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Language, // Closest to "safari.fill"
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Is this your course? Click to Claim!",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

