package com.garrettbutchko.minimate.courseScreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.core.content.ContextCompat
import com.garrettbutchko.minimate.dataModels.MapRegionData
import com.garrettbutchko.minimate.viewModels.CourseViewModel
import org.koin.core.context.GlobalContext

@Composable
fun CourseScreen(
    courseVM: CourseViewModel? = null
) {
    val koin = remember { GlobalContext.get() }
    val courseVM = courseVM ?: koin.get<CourseViewModel>()

    val position by courseVM.position.collectAsState()
    val isUpperHalf by courseVM.isUpperHalf.collectAsState()
    val selectedCourse by courseVM.selectedCourse.collectAsState()
    val nameExists by courseVM.nameExists.collectAsState()
    val isLoadingCourses by courseVM.isLoadingCourses.collectAsState()
    val mapItems by courseVM.mapItems.collectAsState()
    val hasLocationAccess by courseVM.hasLocationAccess.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        println("[CourseScreen] permission result granted=$granted")
        runCatching { courseVM.requestLocationAccess() }
            .onFailure { error ->
                println("[CourseScreen] permission callback error: ${error.message}")
            }
    }

    val requestPermissions: () -> Unit = {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            courseVM.requestLocationAccess()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        courseVM.onAppearance()
        courseVM.requestLocationAccess()
    }

    LaunchedEffect(isLoadingCourses, isUpperHalf, mapItems.size, selectedCourse, nameExists.size) {
        println(
            "[CourseScreen] loading=$isLoadingCourses upperHalf=$isUpperHalf " +
                "mapItems=${mapItems.size} selectedCourse=${selectedCourse?.name} " +
                "nameExists=${nameExists.size}"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationAccess) {
            // Map placeholder
            MapPlaceholder(position = position)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp), // Standard Material height for bars
                    horizontalArrangement = Arrangement.SpaceBetween, // Pushes text and button to opposite sides
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Course Search",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            // 1. NO WEIGHT HERE - This lets it wrap the content
                            .shadow(8.dp, RoundedCornerShape(25.dp))
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LocationButton(
                        viewModel = courseVM,
                        hasLocationAccess = hasLocationAccess,
                        onRequestPermission = requestPermissions
                    )
                }

                // Bottom search panel
                AnimatedVisibility(
                    visible = !isUpperHalf,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    CourseSearchButton(
                        isLoading = isLoadingCourses,
                        onSearch = {
                            if (hasLocationAccess) {
                                courseVM.searchNearby()
                            } else {
                                requestPermissions()
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isUpperHalf,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .shadow(8.dp, RoundedCornerShape(25.dp))
                            .clip(RoundedCornerShape(25.dp)),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ) {
                        if (selectedCourse != null) {
                            CourseResultView(
                                selectedCourse = selectedCourse,
                                viewModel = courseVM
                            )
                        } else {
                            CourseSearchResultsView(
                                mapItems = mapItems,
                                nameExists = nameExists,
                                isLoading = isLoadingCourses,
                                onSelect = { selected ->
                                    println("[CourseScreen] result tapped name=${selected.name} placeId=${selected.placeID}")
                                    courseVM.updatePosition(selected)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            LocationPermissionPrompt(onRequest = requestPermissions)
        }
    }
}



@Composable
private fun MapPlaceholder(position: MapRegionData?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (position != null) Color(0xFFE0E0E0) else Color(0xFFF0F0F0)
            ),
        contentAlignment = Alignment.Center
    ) {
        val lat = position?.latitude?.toInt() ?: 0
        val lng = position?.longitude?.toInt() ?: 0
        Text(
            "\uD83D\uDDFA️ Map\n(${lat}°, ${lng}°)",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun LocationButton(
    viewModel: CourseViewModel,
    hasLocationAccess: Boolean,
    onRequestPermission: () -> Unit
) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable {
                    if (hasLocationAccess) {
                        viewModel.searchNearby()
                    } else {
                        onRequestPermission()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                // This is the Android equivalent of "location.fill"
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "Search Nearby",
                // Use your primary color to make it pop like iOS blue
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

}



@Composable
private fun LocationPermissionPrompt(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Please enable Location Services for this app.\n\nTap 'Open Settings' → Location → Allow While Using the App.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = onRequest) {
            Text("Allow Location")
        }
    }
}









