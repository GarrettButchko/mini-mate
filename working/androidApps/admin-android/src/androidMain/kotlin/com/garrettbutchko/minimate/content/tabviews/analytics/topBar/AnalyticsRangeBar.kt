package com.garrettbutchko.minimate.content.tabviews.analytics.topBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.repositories.analytics.AnalyticsRange
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import kotlinx.coroutines.launch

/**
 * AnalyticsRangeBar
 *
 * Mirrors the iOS AnalyticsRangeBar which shows:
 * 1. A dropdown menu for quick presets (Last 7 / 30 / 90 days).
 * 2. A "Custom Range" button that opens a bottom sheet (CustomRangeSheet).
 *
 * SwiftUI's .sheet → Android ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsRangeBar(
    vm: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val range by vm.range.collectAsState()
    val course by vm.currentCourse.collectAsState()

    var showPresetMenu by remember { mutableStateOf(false) }
    var showCustomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Preset range dropdown
        Surface(
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f)
        ) {
            TextButton(
                onClick = { showPresetMenu = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = if (range.isCustom) {
                        "${vm.getDateRangeString()}, ${range.daysBetween} days"
                    } else {
                        range.title
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            DropdownMenu(
                expanded = showPresetMenu,
                onDismissRequest = { showPresetMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Last 7 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last7, course)
                        showPresetMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Last 30 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last30, course)
                        showPresetMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Last 90 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last90, course)
                        showPresetMenu = false
                    }
                )
            }
        }

        // Custom Range button
        Button(
            onClick = { showCustomSheet = true },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF).copy(alpha = 0.9f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "Custom Range",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }

    // Custom range bottom sheet — replaces SwiftUI .sheet with .presentationDetents
    if (showCustomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomSheet = false },
            sheetState = sheetState
        ) {
            CustomRangeSheet(
                vm = vm,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCustomSheet = false
                    }
                },
                onApply = { startDate, endDate ->
                    vm.setRange(AnalyticsRange.Custom(start = startDate, end = endDate), course)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCustomSheet = false
                    }
                },
                initialStart = range.startDate.toEpochMillis(),
                initialEnd = range.endDate.toEpochMillis()
            )
        }
    }
}

// Helper: convert kotlinx.datetime.LocalDate to epoch millis
private fun kotlinx.datetime.LocalDate.toEpochMillis(): Long {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, year)
        set(java.util.Calendar.MONTH, month.ordinal)
        set(java.util.Calendar.DAY_OF_MONTH, day)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
