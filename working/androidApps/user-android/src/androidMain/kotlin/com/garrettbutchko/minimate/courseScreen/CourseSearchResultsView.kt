package com.garrettbutchko.minimate.courseScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO

@Composable
fun CourseSearchResultsView(
    mapItems: List<MapItemDTO> = emptyList(),
    nameExists: Map<String, Boolean>,
    isLoading: Boolean,
    onSelect: (MapItemDTO) -> Unit = {}
) {
    LaunchedEffect(isLoading, mapItems.size, nameExists.size) {
        println("[CourseSearchResultsView] loading=$isLoading mapItems=${mapItems.size} nameExists=${nameExists.size}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Nearby Courses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        } else {
            if (mapItems.isEmpty()) {
                Text(
                    "No courses found nearby. Tap search to find courses!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    mapItems.forEach { item ->
                        SearchResultRow(mapItem = item, distanceMiles = null, nameExists = nameExists) { selected ->
                            onSelect(selected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    mapItem: MapItemDTO,
    distanceMiles: Double?,
    nameExists: Map<String, Boolean>,
    onSelect: (MapItemDTO) -> Unit = {}
) {
    val supported = nameExists[mapItem.name ?: ""] ?: false
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onSelect(mapItem) }
        .padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mapItem.name ?: "Unknown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val subtitle = buildString {
                    if (distanceMiles != null) append("${String.format("%.1f", distanceMiles)} mi • ")
                    append(mapItem.address?.shortAddress ?: "")
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (supported) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(28.dp).background(Color(0xFFEEE5FF), shape = androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                        Text("★")
                    }
                }
            }
        }
    }
}

