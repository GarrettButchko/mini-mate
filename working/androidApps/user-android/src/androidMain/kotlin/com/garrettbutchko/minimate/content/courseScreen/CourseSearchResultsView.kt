package com.garrettbutchko.minimate.content.courseScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.logic.SearchResultBusinessLogic
import com.garrettbutchko.minimate.utilities.CourseIDGenerator
import com.garrettbutchko.minimate.utilities.PasswordGenerator
import com.garrettbutchko.minimate.viewModels.CourseSearchViewModel
import com.garrettbutchko.minimate.viewModels.CourseViewModel
import kotlinx.coroutines.delay
import org.koin.core.context.GlobalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.garrettbutchko.minimate.shared.R

@Composable
fun CourseSearchResultsView(
    mapItems: List<MapItemDTO> = emptyList(),
    nameExists: Map<String, Boolean>,
    isLoading: Boolean,
    onSelect: (MapItemDTO, Course) -> Unit = { _, _ -> },
    onRetry: () -> Unit = {}
) {

    val koin = remember { GlobalContext.get() }
    val courseVM = koin.get<CourseViewModel>()
    val courseSearch = koin.get< CourseSearchViewModel>()

    val logic = remember { SearchResultBusinessLogic() }
    var showRetryButton by remember { mutableStateOf(false) }
    val shouldShowLoadingState = isLoading || mapItems.isEmpty()

    LaunchedEffect(shouldShowLoadingState, logic.retryButtonDelay) {
        showRetryButton = false
        if (shouldShowLoadingState) {
            delay((logic.retryButtonDelay * 1000).toLong())
            showRetryButton = true
        }
    }

    LaunchedEffect(isLoading, mapItems.size, nameExists.size) {
        println("[CourseSearchResultsView] loading=$isLoading mapItems=${mapItems.size} nameExists=${nameExists.size}")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (shouldShowLoadingState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Trying to find nearby courses...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    if (showRetryButton) {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(top = 16.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Text("Try Again")
                        }
                    }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 40.dp,
                    bottom = 16.dp
                )
            ) {
                itemsIndexed(mapItems) { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    SearchResultRow(
                        mapItem = item,
                        nameExists = nameExists,
                        courseVM = courseVM,
                        onSelect = onSelect
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)

            ,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nearby Courses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        courseVM.cancel()
                        courseSearch.setNewMapPosition()
                    },
                    modifier = Modifier.size(width = 70.dp, height = 30.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelMedium // Optional: adjust to fit the small height
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    mapItem: MapItemDTO,
    nameExists: Map<String, Boolean>,
    courseVM: CourseViewModel,
    onSelect: (MapItemDTO, Course) -> Unit = { _, _ -> }
) {
    val supported = nameExists[mapItem.name.orEmpty()] ?: false
    val subtitle = courseVM.getSearchResultSubtitle(mapItem)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mapItem, mapItem.toFallbackCourse(isSupported = supported)) }
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mapItem.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (supported) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFEEE5FF), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun MapItemDTO.toFallbackCourse(isSupported: Boolean): Course {
    return Course.create(
        id = CourseIDGenerator.generateCourseID(this),
        name = name ?: "Unknown",
        password = PasswordGenerator.generateStrong(),
        latitude = coordinate.latitude,
        longitude = coordinate.longitude,
        isSupported = isSupported
    ).copy(placeID = placeID)
}

