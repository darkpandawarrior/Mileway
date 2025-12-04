package com.miletracker.core.ui.components.pickers

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anhaki.picktime.PickDate
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.PickHourMinuteSecond
import com.anhaki.picktime.utils.PickDateOrder
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.anhaki.picktime.utils.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

private fun formatFriendlyDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    val year = cal.get(Calendar.YEAR)
    return "$day$suffix $monthName $year"
}

private fun formatTime24h(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

private fun formatTime12h(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%02d:%02d %s".format(h, minute, amPm)
}

object WheelPickersDefaults {
    val verticalSpace: Dp = 10.dp
    val horizontalSpace: Dp = 15.dp

    @Composable
    fun selectedStyle(): PickTimeTextStyle {
        val style = MaterialTheme.typography.headlineSmall
        return PickTimeTextStyle(
            color = MaterialTheme.colorScheme.primary,
            fontSize = style.fontSize,
            fontFamily = style.fontFamily ?: FontFamily.Default,
            fontWeight = style.fontWeight ?: FontWeight.SemiBold,
        )
    }

    @Composable
    fun unselectedStyle(): PickTimeTextStyle {
        val style = MaterialTheme.typography.titleMedium
        return PickTimeTextStyle(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = style.fontSize,
            fontFamily = style.fontFamily ?: FontFamily.Default,
            fontWeight = style.fontWeight ?: FontWeight.Medium,
        )
    }

    @Composable
    fun focusIndicator(): PickTimeFocusIndicator = PickTimeFocusIndicator(
        enabled = true,
        widthFull = false,
        background = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    )
}

@Composable
fun WheelDatePicker(
    initialDay: Int,
    initialMonth: Int,
    initialYear: Int,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    isLooping: Boolean = true,
    extraRow: Int = 1,
    minYear: Int? = null,
    maxYear: Int? = null
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (minYear ?: (currentYear - 100))..(maxYear ?: (currentYear + 50))

    PickDate(
        initialDay = initialDay,
        onDayChange = onDayChange,
        initialMonth = initialMonth,
        onMonthChange = onMonthChange,
        initialYear = initialYear,
        onYearChange = onYearChange,
        selectedTextStyle = WheelPickersDefaults.selectedStyle(),
        unselectedTextStyle = WheelPickersDefaults.unselectedStyle(),
        verticalSpace = WheelPickersDefaults.verticalSpace,
        horizontalSpace = WheelPickersDefaults.horizontalSpace,
        containerColor = MaterialTheme.colorScheme.surface,
        isLooping = isLooping,
        extraRow = extraRow,
        focusIndicator = WheelPickersDefaults.focusIndicator(),
        yearRange = yearRange
    )
}

/**
 * Month + Year wheel picker (no day column visible).
 *
 * PickTime's PickDate always renders DAY/MONTH/YEAR. We render in MYD order, constrain DAY
 * to a single value, and mask the DAY column with a surface-coloured overlay sized via a
 * custom Layout measure policy.
 */
@Composable
fun WheelMonthYearPicker(
    initialMonth: Int,
    initialYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    isLooping: Boolean = false,
    extraRow: Int = 1,
    minYear: Int? = null,
    maxYear: Int? = null,
    monthList: List<String> = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (minYear ?: (currentYear - 100))..(maxYear ?: (currentYear + 50))

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val selectedTextStyle = WheelPickersDefaults.selectedStyle()

    val longestMonth = remember(monthList) { monthList.maxBy { it.length } }
    val maxYearText = remember(yearRange) { yearRange.last.toString() }

    val spacePx = with(density) { WheelPickersDefaults.horizontalSpace.toPx() }
    val monthWidthPx = textMeasurer.measure(
        text = AnnotatedString("$longestMonth "),
        style = selectedTextStyle.toTextStyle()
    ).size.width.toFloat()
    val yearWidthPx = textMeasurer.measure(
        text = AnnotatedString("$maxYearText "),
        style = selectedTextStyle.toTextStyle()
    ).size.width.toFloat()
    val dayWidthPx = textMeasurer.measure(
        text = AnnotatedString("31 "),
        style = selectedTextStyle.toTextStyle()
    ).size.width.toFloat()

    val desiredContentWidthPx = (monthWidthPx + yearWidthPx + dayWidthPx + (spacePx * 2f)).toInt()

    Layout(
        modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
        content = {
            PickDate(
                initialDay = 1,
                dayRange = 1..1,
                onDayChange = { /* ignored */ },
                initialMonth = initialMonth,
                monthList = monthList,
                dateOrder = PickDateOrder.MYD,
                onMonthChange = onMonthChange,
                initialYear = initialYear,
                yearRange = yearRange,
                onYearChange = onYearChange,
                selectedTextStyle = selectedTextStyle,
                unselectedTextStyle = WheelPickersDefaults.unselectedStyle(),
                verticalSpace = WheelPickersDefaults.verticalSpace,
                horizontalSpace = WheelPickersDefaults.horizontalSpace,
                containerColor = MaterialTheme.colorScheme.surface,
                isLooping = isLooping,
                extraRow = extraRow,
                focusIndicator = WheelPickersDefaults.focusIndicator(),
            )
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
        }
    ) { measurables, constraints ->
        val layoutWidthPx = min(desiredContentWidthPx, constraints.maxWidth)
        val constrained = constraints.copy(minWidth = layoutWidthPx, maxWidth = layoutWidthPx)

        val pickerPlaceable = measurables[0].measure(constrained)

        val maskWidthPx = min((dayWidthPx + spacePx).toInt(), pickerPlaceable.width)
        val maskPlaceable = measurables[1].measure(
            Constraints.fixed(maskWidthPx, pickerPlaceable.height)
        )

        layout(pickerPlaceable.width, pickerPlaceable.height) {
            pickerPlaceable.place(0, 0)
            maskPlaceable.place(pickerPlaceable.width - maskWidthPx, 0)
        }
    }
}

@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    use12HourFormat: Boolean = false,
    isLooping: Boolean = true,
    extraRow: Int = 1
) {
    PickHourMinute(
        initialHour = initialHour,
        onHourChange = onHourChange,
        initialMinute = initialMinute,
        onMinuteChange = onMinuteChange,
        selectedTextStyle = WheelPickersDefaults.selectedStyle(),
        unselectedTextStyle = WheelPickersDefaults.unselectedStyle(),
        verticalSpace = WheelPickersDefaults.verticalSpace,
        horizontalSpace = WheelPickersDefaults.horizontalSpace,
        containerColor = MaterialTheme.colorScheme.surface,
        isLooping = isLooping,
        extraRow = extraRow,
        focusIndicator = WheelPickersDefaults.focusIndicator(),
        timeFormat = if (use12HourFormat) TimeFormat.HOUR_12 else TimeFormat.HOUR_24
    )
}

@Composable
fun WheelTimePickerWithSeconds(
    initialHour: Int,
    initialMinute: Int,
    initialSecond: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSecondChange: (Int) -> Unit,
    use12HourFormat: Boolean = false,
    isLooping: Boolean = true,
    extraRow: Int = 1
) {
    PickHourMinuteSecond(
        initialHour = initialHour,
        onHourChange = onHourChange,
        initialMinute = initialMinute,
        onMinuteChange = onMinuteChange,
        initialSecond = initialSecond,
        onSecondChange = onSecondChange,
        selectedTextStyle = WheelPickersDefaults.selectedStyle(),
        unselectedTextStyle = WheelPickersDefaults.unselectedStyle(),
        verticalSpace = WheelPickersDefaults.verticalSpace,
        horizontalSpace = WheelPickersDefaults.horizontalSpace,
        containerColor = MaterialTheme.colorScheme.surface,
        isLooping = isLooping,
        extraRow = extraRow,
        focusIndicator = WheelPickersDefaults.focusIndicator(),
    )
}

// ── Date picker bottom sheet (legacy 3-param overload) ────────────────────────

@Composable
fun WheelDatePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    title: String = "Select Date",
    allowPastDates: Boolean = true,
    minYear: Int? = null,
    maxYear: Int? = null
) {
    WheelDatePickerBottomSheetInternal(
        showSheet = showSheet,
        onDismiss = onDismiss,
        initialDate = initialDate,
        onDateSelected = { year, month, day, _ -> onDateSelected(year, month, day) },
        title = title,
        allowPastDates = allowPastDates,
        minYear = minYear,
        maxYear = maxYear,
        minTimestamp = null,
        maxTimestamp = null,
        validationMessage = null,
        maxValidationMessage = null
    )
}

// ── Date picker bottom sheet (new 4-param overload with timestamp + validation) ─

@Composable
fun WheelDatePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (year: Int, month: Int, day: Int, timestamp: Long) -> Unit,
    title: String = "Select Date",
    allowPastDates: Boolean = true,
    minYear: Int? = null,
    maxYear: Int? = null,
    minTimestamp: Long? = null,
    maxTimestamp: Long? = null,
    validationMessage: String? = null,
    maxValidationMessage: String? = null
) {
    WheelDatePickerBottomSheetInternal(
        showSheet = showSheet,
        onDismiss = onDismiss,
        initialDate = initialDate,
        onDateSelected = onDateSelected,
        title = title,
        allowPastDates = allowPastDates,
        minYear = minYear,
        maxYear = maxYear,
        minTimestamp = minTimestamp,
        maxTimestamp = maxTimestamp,
        validationMessage = validationMessage,
        maxValidationMessage = maxValidationMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WheelDatePickerBottomSheetInternal(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (year: Int, month: Int, day: Int, timestamp: Long) -> Unit,
    title: String = "Select Date",
    allowPastDates: Boolean = true,
    minYear: Int? = null,
    maxYear: Int? = null,
    minTimestamp: Long? = null,
    maxTimestamp: Long? = null,
    validationMessage: String? = null,
    maxValidationMessage: String? = null
) {
    if (!showSheet) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val effectiveMinYear = if (!allowPastDates) currentYear else (minYear ?: (currentYear - 100))
    val effectiveMaxYear = maxYear ?: (currentYear + 50)

    var selectedYear by remember(initialDate) { mutableIntStateOf(initialDate.get(Calendar.YEAR)) }
    var selectedMonth by remember(initialDate) { mutableIntStateOf(initialDate.get(Calendar.MONTH) + 1) }
    var selectedDay by remember(initialDate) { mutableIntStateOf(initialDate.get(Calendar.DAY_OF_MONTH)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .heightIn(max = 385.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            val previewCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, selectedDay)
            }
            Text(
                text = formatFriendlyDate(previewCalendar.timeInMillis),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            WheelDatePicker(
                initialDay = selectedDay,
                initialMonth = selectedMonth,
                initialYear = selectedYear,
                onDayChange = { selectedDay = it },
                onMonthChange = {
                    selectedMonth = it
                    val maxDay = Calendar.getInstance().apply {
                        set(selectedYear, it - 1, 1)
                    }.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (selectedDay > maxDay) selectedDay = maxDay
                },
                onYearChange = { selectedYear = it },
                minYear = effectiveMinYear,
                maxYear = effectiveMaxYear
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val selectedTimestamp = Calendar.getInstance().apply {
                            set(selectedYear, selectedMonth - 1, selectedDay, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        if (minTimestamp != null && selectedTimestamp < minTimestamp) {
                            Toast.makeText(
                                context,
                                validationMessage ?: "Selected date must be after the previous date",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (maxTimestamp != null && selectedTimestamp > maxTimestamp) {
                            Toast.makeText(
                                context,
                                maxValidationMessage ?: "Selected date must not be after the allowed date",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        onDateSelected(selectedYear, selectedMonth, selectedDay, selectedTimestamp)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Confirm") }
            }
        }
    }
}

// ── Time picker bottom sheet (legacy 2-param overload) ────────────────────────

@Composable
fun WheelTimePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    title: String = "Select Time",
    use24Hour: Boolean = true
) {
    WheelTimePickerBottomSheetInternal(
        showSheet = showSheet,
        onDismiss = onDismiss,
        initialHour = initialHour,
        initialMinute = initialMinute,
        onTimeSelected = { hour: Int, minute: Int, _ -> onTimeSelected(hour, minute) },
        title = title,
        use24Hour = use24Hour,
        dateContext = null,
        minTimestamp = null,
        maxTimestamp = null,
        validationMessage = null,
        maxValidationMessage = null
    )
}

// ── Time picker bottom sheet (new overload with timestamp + validation) ────────

@Composable
fun WheelTimePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int, timestamp: Long) -> Unit,
    title: String = "Select Time",
    use24Hour: Boolean = true,
    dateContext: String? = null,
    minTimestamp: Long? = null,
    maxTimestamp: Long? = null,
    validationMessage: String? = null,
    maxValidationMessage: String? = null
) {
    WheelTimePickerBottomSheetInternal(
        showSheet = showSheet,
        onDismiss = onDismiss,
        initialHour = initialHour,
        initialMinute = initialMinute,
        onTimeSelected = onTimeSelected,
        title = title,
        use24Hour = use24Hour,
        dateContext = dateContext,
        minTimestamp = minTimestamp,
        maxTimestamp = maxTimestamp,
        validationMessage = validationMessage,
        maxValidationMessage = maxValidationMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WheelTimePickerBottomSheetInternal(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int, timestamp: Long) -> Unit,
    title: String = "Select Time",
    use24Hour: Boolean = true,
    dateContext: String? = null,
    minTimestamp: Long? = null,
    maxTimestamp: Long? = null,
    validationMessage: String? = null,
    maxValidationMessage: String? = null
) {
    if (!showSheet) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var selectedHour by remember(initialHour) { mutableIntStateOf(initialHour) }
    var selectedMinute by remember(initialMinute) { mutableIntStateOf(initialMinute) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .heightIn(max = 385.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = if (use24Hour) formatTime24h(selectedHour, selectedMinute)
                       else formatTime12h(selectedHour, selectedMinute),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            WheelTimePicker(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it },
                use12HourFormat = !use24Hour
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val selectedTimestamp = if (dateContext != null) {
                            val parsedDate = parseDateContext(dateContext)
                            Calendar.getInstance().apply {
                                if (parsedDate != null) {
                                    time = parsedDate
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                            }.timeInMillis
                        } else {
                            selectedHour * 3600000L + selectedMinute * 60000L
                        }

                        if (minTimestamp != null && selectedTimestamp < minTimestamp) {
                            Toast.makeText(
                                context,
                                validationMessage ?: "Time must be on or after the previous time",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (maxTimestamp != null && selectedTimestamp > maxTimestamp) {
                            Toast.makeText(
                                context,
                                maxValidationMessage ?: "Time must not be after the allowed time",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        onTimeSelected(selectedHour, selectedMinute, selectedTimestamp)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Confirm") }
            }
        }
    }
}

private fun parseDateContext(dateContext: String): Date? {
    return try {
        val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply { isLenient = false }
        fmt.parse(dateContext)
    } catch (e: Exception) {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            fmt.parse(dateContext)
        } catch (ex: Exception) {
            null
        }
    }
}

// ── Date range picker bottom sheet ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelDateRangePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    title: String = "Select Date Range",
    minYear: Int? = null,
    maxYear: Int? = null,
    allowPastDates: Boolean = true
) {
    if (!showSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        WheelDateRangePickerContent(
            initialStartMillis = initialStartMillis,
            initialEndMillis = initialEndMillis,
            onConfirm = { start, end ->
                onConfirm(start, end)
                onDismiss()
            },
            onCancel = onDismiss,
            title = title,
            minYear = minYear,
            maxYear = maxYear,
            allowPastDates = allowPastDates
        )
    }
}

@Composable
fun WheelDateRangePickerContent(
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    onCancel: () -> Unit,
    title: String = "Select Date Range",
    minYear: Int? = null,
    maxYear: Int? = null,
    allowPastDates: Boolean = true
) {
    val context = LocalContext.current
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val effectiveMinYear = minYear ?: (if (allowPastDates) currentYear - 100 else currentYear)
    val effectiveMaxYear = maxYear ?: (currentYear + 50)

    val startCalendar = remember {
        Calendar.getInstance().apply {
            when {
                initialStartMillis != null -> timeInMillis = initialStartMillis
                !allowPastDates -> { /* already today */ }
                else -> add(Calendar.MONTH, -2)
            }
        }
    }
    var startDay by remember { mutableIntStateOf(startCalendar.get(Calendar.DAY_OF_MONTH)) }
    var startMonth by remember { mutableIntStateOf(startCalendar.get(Calendar.MONTH) + 1) }
    var startYear by remember { mutableIntStateOf(startCalendar.get(Calendar.YEAR)) }

    val endCalendar = remember {
        Calendar.getInstance().apply {
            if (initialEndMillis != null) timeInMillis = initialEndMillis
            else add(Calendar.MONTH, 6)
        }
    }
    var endDay by remember { mutableIntStateOf(endCalendar.get(Calendar.DAY_OF_MONTH)) }
    var endMonth by remember { mutableIntStateOf(endCalendar.get(Calendar.MONTH) + 1) }
    var endYear by remember { mutableIntStateOf(endCalendar.get(Calendar.YEAR)) }

    var isSelectingStart by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RangeTypeButton(text = "Start Date", isSelected = isSelectingStart, onClick = { isSelectingStart = true })
            RangeTypeButton(text = "End Date", isSelected = !isSelectingStart, onClick = { isSelectingStart = false })
        }

        Spacer(modifier = Modifier.height(16.dp))

        val previewCalendar = Calendar.getInstance().apply {
            if (isSelectingStart) set(startYear, startMonth - 1, startDay)
            else set(endYear, endMonth - 1, endDay)
        }
        Text(
            text = formatFriendlyDate(previewCalendar.timeInMillis),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSelectingStart) {
            WheelDatePicker(
                initialDay = startDay, initialMonth = startMonth, initialYear = startYear,
                onDayChange = { startDay = it }, onMonthChange = { startMonth = it },
                onYearChange = { startYear = it }, minYear = effectiveMinYear, maxYear = effectiveMaxYear
            )
        } else {
            WheelDatePicker(
                initialDay = endDay, initialMonth = endMonth, initialYear = endYear,
                onDayChange = { endDay = it }, onMonthChange = { endMonth = it },
                onYearChange = { endYear = it }, minYear = effectiveMinYear, maxYear = effectiveMaxYear
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)
            ) { Text("Cancel") }

            Button(
                onClick = {
                    val startMillis = Calendar.getInstance().apply {
                        set(startYear, startMonth - 1, startDay, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val endMillis = Calendar.getInstance().apply {
                        set(endYear, endMonth - 1, endDay, 23, 59, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    if (startMillis <= endMillis) {
                        onConfirm(startMillis, endMillis)
                    } else {
                        Toast.makeText(
                            context,
                            "End date must be after or equal to start date",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)
            ) { Text("Apply") }
        }
    }
}

// ── Time range picker bottom sheet ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelTimeRangePickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    initialStartHour: Int = 9,
    initialStartMinute: Int = 0,
    initialEndHour: Int = 17,
    initialEndMinute: Int = 0,
    onConfirm: (startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    title: String = "Select Time Range",
    use24Hour: Boolean = true,
    dateContext: String? = null,
    validateSameDay: Boolean = true
) {
    if (!showSheet) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var startMinute by remember { mutableIntStateOf(initialStartMinute) }
    var endHour by remember { mutableIntStateOf(initialEndHour) }
    var endMinute by remember { mutableIntStateOf(initialEndMinute) }

    var isSelectingStart by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .heightIn(max = 385.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RangeTypeButton(text = "Start Time", isSelected = isSelectingStart, onClick = { isSelectingStart = true })
                RangeTypeButton(text = "End Time", isSelected = !isSelectingStart, onClick = { isSelectingStart = false })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSelectingStart) {
                    if (use24Hour) formatTime24h(startHour, startMinute) else formatTime12h(startHour, startMinute)
                } else {
                    if (use24Hour) formatTime24h(endHour, endMinute) else formatTime12h(endHour, endMinute)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSelectingStart) {
                WheelTimePicker(
                    initialHour = startHour, initialMinute = startMinute,
                    onHourChange = { startHour = it }, onMinuteChange = { startMinute = it },
                    use12HourFormat = !use24Hour
                )
            } else {
                WheelTimePicker(
                    initialHour = endHour, initialMinute = endMinute,
                    onHourChange = { endHour = it }, onMinuteChange = { endMinute = it },
                    use12HourFormat = !use24Hour
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        if (validateSameDay) {
                            val startMins = startHour * 60 + startMinute
                            val endMins = endHour * 60 + endMinute
                            if (endMins <= startMins) {
                                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }
                        onConfirm(startHour, startMinute, endHour, endMinute)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun RangeTypeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                         else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
