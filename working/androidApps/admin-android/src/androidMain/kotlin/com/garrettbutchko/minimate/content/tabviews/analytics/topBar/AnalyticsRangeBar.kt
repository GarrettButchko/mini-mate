package com.garrettbutchko.minimate.content.tabviews.analytics.topBar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.repositories.analytics.AnalyticsRange
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalyticsRangeBar(
    vm: AnalyticsViewModel,
    range: AnalyticsRange,
    course: Course?,
    modifier: Modifier = Modifier
) {
    var showRangeDropdown by remember { mutableStateOf(false) }
    var showCustomSheet by remember { mutableStateOf(false) }
    val customSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val rangeLabel = if (range.isCustom) {
        "${vm.getDateRangeString()}, ${range.daysBetween} days"
    } else {
        range.title
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(25.dp))
                    .clickable { showRangeDropdown = true },
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showRangeDropdown,
                onDismissRequest = { showRangeDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Last 7 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last7, course)
                        showRangeDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Last 30 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last30, course)
                        showRangeDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Last 90 days") },
                    onClick = {
                        vm.setRange(AnalyticsRange.Last90, course)
                        showRangeDropdown = false
                    }
                )
            }
        }

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .clickable { showCustomSheet = true },
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Custom Range",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (showCustomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomSheet = false },
            sheetState = customSheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            CustomRangeSheet(
                vm = vm,
                onDismiss = { showCustomSheet = false },
                onApply = { start, end ->
                    vm.setRange(AnalyticsRange.Custom(start, end), course)
                    showCustomSheet = false
                }
            )
        }
    }
}
