package com.miletracker.core.ui.components.pickers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.darkokoa.datetimewheelpicker.WheelDatePicker
import dev.darkokoa.datetimewheelpicker.WheelTimePicker
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private val systemTz: TimeZone get() = TimeZone.currentSystemDefault()

private fun Long.toLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(systemTz).date

private fun LocalDate.toEpochMillis(): Long = atStartOfDayIn(systemTz).toEpochMilliseconds()

/**
 * A spinning-WHEEL date picker in a Material3 dialog (multiplatform — Android + iOS), backed by
 * darkokoa/datetime-wheel-picker. Drop-in replacement for the Material3 dial/calendar date dialog.
 */
@Composable
fun WheelDatePickerDialog(
    initialDateMillis: Long?,
    onConfirm: (millis: Long) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select date",
) {
    val startDate =
        remember(initialDateMillis) {
            (initialDateMillis ?: Clock.System.now().toEpochMilliseconds()).toLocalDate()
        }
    var snapped by remember { mutableStateOf(startDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WheelDatePicker(
                    startDate = startDate,
                    onSnappedDate = { snapped = it },
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(snapped.toEpochMillis()) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * A spinning-WHEEL time picker in a Material3 dialog (multiplatform). Takes/returns minutes-since-midnight
 * as hour + minute. Drop-in replacement for the Material3 dial time dialog.
 */
@Composable
fun WheelTimePickerDialog(
    initialMinutes: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select time",
) {
    val startTime =
        remember(initialMinutes) {
            LocalTime(
                hour = (initialMinutes / 60).coerceIn(0, 23),
                minute = (initialMinutes % 60).coerceIn(0, 59),
            )
        }
    var snapped by remember { mutableStateOf(startTime) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WheelTimePicker(
                    startTime = startTime,
                    onSnappedTime = { snapped = it },
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(snapped.hour, snapped.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
