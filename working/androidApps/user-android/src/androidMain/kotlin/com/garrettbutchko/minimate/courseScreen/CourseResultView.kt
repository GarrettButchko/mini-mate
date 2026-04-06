package com.garrettbutchko.minimate.courseScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.viewModels.CourseViewModel

@Composable
fun CourseResultView(
    selectedCourse: Course?,
    viewModel: CourseViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (selectedCourse != null) {
            CourseResultViewHeader(courseName = selectedCourse.name) {
                viewModel.cancel()
            }

            if (selectedCourse.isSupported) {
                CourseSupportedLocationCard(course = selectedCourse, locationName = selectedCourse.name)
                if (!selectedCourse.socialLinks.isNullOrEmpty()) {
                    CourseSocialMediaCard(course = selectedCourse)
                }
            } else {
                CourseClaimButton(null)
            }

            CourseContactInfoCard(mapItem = null)
            CourseLocationInfoCard(mapItem = null)
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
            .padding(8.dp)
    ) {
        Button(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Text("←")
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = courseName ?: "Course",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CourseDirectionsButton(viewModel: CourseViewModel) {
    Button(onClick = { viewModel.getDirections() }, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🧭", style = MaterialTheme.typography.bodyLarge)
            Text("Get Directions", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CourseSupportedLocationCard(course: Course, locationName: String?) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .shadow(4.dp, RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .padding(14.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEEE5FF))) {
            Text("🏳️", modifier = Modifier.align(Alignment.Center))
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
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👥")
            Spacer(Modifier.width(8.dp))
            Text("Social Media", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(8.dp))

        // Horizontal scrolling row of buttons
        Row(modifier = Modifier) {
            course.socialLinks.forEach { link ->
                if (link.platform != com.garrettbutchko.minimate.dataModels.courseModels.SocialPlatform.DEFAULT) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        context.startActivity(intent)
                    }, modifier = Modifier.padding(end = 8.dp)) {
                        Text(link.platform.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}

@Composable
fun CourseContactInfoCard(mapItem: MapItemDTO?) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📞")
            Spacer(Modifier.width(8.dp))
            Text("Contact", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(8.dp))

        if (mapItem?.phoneNumber != null && mapItem.phoneNumber!!.isNotBlank()) {
            Button(onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mapItem.phoneNumber}"))
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Call ${mapItem.phoneNumber}")
            }
        }

        if (mapItem?.url != null && mapItem.url!!.contains("http")) {
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapItem.url))
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text("Visit Website")
            }
        }
    }
}

@Composable
fun CourseLocationInfoCard(mapItem: MapItemDTO?) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍")
            Spacer(Modifier.width(8.dp))
            Text("Location", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(8.dp))

        Text(mapItem?.name ?: "Unknown", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(mapItem?.address?.shortAddress ?: mapItem?.address?.fullAddress ?: "Address unknown", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CourseClaimButton(mapItem: MapItemDTO?) {
    Button(onClick = { /* TODO: open claim flow */ }, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Is this your course? Click to Claim!")
            Spacer(Modifier.weight(1f))
            Text("›")
        }
    }
}

