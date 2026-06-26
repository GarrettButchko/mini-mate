package com.garrettbutchko.minimate.content.tabviews.analytics.topBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import java.util.Calendar

/**
 * CustomRangeSheet
 *
 * SwiftUI used iOS DatePicker inline pickers inside a sheet.
 * On Android we use Material3 DatePickerDialog (triggered by buttons) — there is no
 * inline date picker equivalent in M3 without a third-party lib.
 *
 * The sheet is rendered as a bottom-sheet content block; the caller (AnalyticsRangeBar)
 * wraps it in a ModalBottomSheet or similar container.
 *
 * @param onDismiss called when the user taps Cancel.
 * @param onApply   called with (startLocalDate, endLocalDate) when the user taps Apply.
 * @param initialStart the current range start as epoch millis.
 * @param initialEnd   the current range end as epoch millis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRangeSheet(
    onDismiss: () -> Unit,
    onApply: (LocalDate, LocalDate) -> Unit,
    initialStart: Long = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000,
    initialEnd: Long = System.currentTimeMillis()
) {
    var startMillis by remember { mutableLongStateOf(initialStart) }
    var endMillis by remember { mutableLongStateOf(initialEnd) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // DatePickerState for start
    val startPickerState = rememberDatePickerState(initialSelectedDateMillis = startMillis)
    // DatePickerState for end
    val endPickerState = rememberDatePickerState(initialSelectedDateMillis = endMillis)

    val daysBetween = remember(startMillis, endMillis) {
        ((endMillis - startMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Custom Range — $daysBetween days",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // From date selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "From",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = { showStartPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = millisToDisplayString(startMillis))
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // To date selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "To",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = millisToDisplayString(endMillis))
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Cancel / Apply buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Text(text = "Cancel", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val s = minOf(startMillis, endMillis)
                    val e = maxOf(startMillis, endMillis)
                    onApply(millisToLocalDate(s), millisToLocalDate(e))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Apply", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Start date picker dialog
    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        // Enforce max = endMillis, min = endMillis - 90 days
                        val minMillis = endMillis - 90L * 24 * 60 * 60 * 1000
                        startMillis = millis.coerceIn(minMillis, endMillis)
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    // End date picker dialog
    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        // Enforce min = startMillis, max = min(startMillis+90, now)
                        val maxMillis = minOf(
                            startMillis + 90L * 24 * 60 * 60 * 1000,
                            System.currentTimeMillis()
                        )
                        endMillis = millis.coerceIn(startMillis, maxMillis)
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
}

// Converts epoch millis to a human-readable "MMM dd, yyyy" string
private fun millisToDisplayString(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
}

// Converts epoch millis to kotlinx.datetime.LocalDate
private fun millisToLocalDate(millis: Long): LocalDate {
    val instant = kotlin.time.Instant.fromEpochMilliseconds(millis)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
