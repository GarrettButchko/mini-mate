package com.garrettbutchko.minimate.courseScreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.garrettbutchko.minimate.dataModels.MapRegionData
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.viewModels.CourseSearchViewModel
import com.garrettbutchko.minimate.viewModels.CourseViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.log2
import kotlin.math.min
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
    val selectedMapItem by courseVM.selectedMapItem.collectAsState()
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
            CourseMap(
                position = position,
                mapItems = mapItems,
                selectedMapItem = selectedMapItem,
                hasLocationAccess = hasLocationAccess,
                onSelectMapItem = { courseVM.updatePosition(it) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = 16.dp),
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
                            .shadow(8.dp, CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LocationButton(
                        hasLocationAccess = hasLocationAccess,
                        onRequestPermission = requestPermissions
                    )
                }
                AnimatedContent(
                    targetState = isUpperHalf,
                    transitionSpec = {
                        // This defines how they slide relative to each other
                        slideInVertically(initialOffsetY = { it }) togetherWith
                                slideOutVertically(targetOffsetY = { it })
                    },
                    label = "CourseViewTransition"
                ) { targetIsUpperHalf ->
                    // Use the targetState provided by the closure to prevent "flicker"
                    if (!targetIsUpperHalf) {
                        CourseSearchButton(
                            isLoading = isLoadingCourses,
                            onSearch = { courseVM.searchNearby() }
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(25.dp))
                                .height(250.dp)
                                .clip(RoundedCornerShape(25.dp)),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ) {
                            AnimatedContent(
                                targetState = selectedCourse,
                                transitionSpec = {
                                    if (targetState != null) {
                                        // Moving TO ResultView (Slide in from Right, Exit to Left)
                                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                                (slideOutHorizontally { -it } + fadeOut())
                                    } else {
                                        // Moving BACK to ResultsView (Slide in from Left, Exit to Right)
                                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                                (slideOutHorizontally { it } + fadeOut())
                                    }
                                },
                                label = "CourseDetailTransition"
                            ) { targetCourse ->
                                if (targetCourse != null) {
                                    CourseResultView(
                                        selectedCourse = targetCourse,
                                        selectedMapItem = selectedMapItem,
                                        viewModel = courseVM
                                    )
                                } else {
                                    CourseSearchResultsView(
                                        mapItems = mapItems,
                                        nameExists = nameExists,
                                        isLoading = isLoadingCourses,
                                        onSelect = { selected, course ->
                                            courseVM.updatePosition(selected)
                                            courseVM.setCourse(course)
                                        },
                                        onRetry = { courseVM.searchNearby() }
                                    )
                                }
                            }
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
private fun CourseMap(
    position: MapRegionData?,
    mapItems: List<MapItemDTO>,
    selectedMapItem: MapItemDTO?,
    hasLocationAccess: Boolean,
    onSelectMapItem: (MapItemDTO) -> Unit
) {
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        )
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(
        position?.updateKey
    ) {
        val region = position ?: return@LaunchedEffect

        val center = LatLng(region.latitude, region.longitude)
        val halfLat = (region.latitudeDelta / 2.0).coerceAtLeast(1e-6)
        val halfLng = (region.longitudeDelta / 2.0).coerceAtLeast(1e-6)

        val bounds = LatLngBounds(
            LatLng(region.latitude - halfLat, region.longitude - halfLng),
            LatLng(region.latitude + halfLat, region.longitude + halfLng)
        )

        runCatching {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 0))
        }.onFailure {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(center, mapZoomFor(region))
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationAccess),
        uiSettings = uiSettings
    ) {
        mapItems.forEach { item ->
            val coordinate = item.coordinate
            Marker(
                state = MarkerState(position = LatLng(coordinate.latitude, coordinate.longitude)),
                title = item.name,
                onClick = {
                    onSelectMapItem(item)
                    false
                }
            )
        }
    }
}

private fun mapZoomFor(region: MapRegionData?): Float {
    region ?: return 14f

    val latitudeDelta = region.latitudeDelta.coerceAtLeast(1e-6)
    val longitudeDelta = region.longitudeDelta.coerceAtLeast(1e-6)

    val latitudeZoom = log2(360.0 / latitudeDelta)
    val longitudeZoom = log2(360.0 / longitudeDelta)
    val zoom = min(latitudeZoom, longitudeZoom)
    val zoomBias = 1.5f

    return (zoom.toFloat() + zoomBias).coerceIn(2f, 21f)
}

@Composable
private fun LocationButton(
    hasLocationAccess: Boolean,
    onRequestPermission: () -> Unit
) {
    val koin = remember { GlobalContext.get() }
    val viewModel = koin.get<CourseSearchViewModel>()
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
                    viewModel.setNewMapPosition()
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









