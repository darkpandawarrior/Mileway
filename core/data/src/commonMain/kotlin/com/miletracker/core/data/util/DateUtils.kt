package com.miletracker.core.data.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateUtils {

    fun epochToDisplayDate(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val monthAbbr = local.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "${local.day} $monthAbbr ${local.year}"
    }

    fun epochToTime12h(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val hour12 = if (local.hour == 0) 12 else if (local.hour > 12) local.hour - 12 else local.hour
        val amPm = if (local.hour < 12) "AM" else "PM"
        return "%d:%02d %s".format(hour12, local.minute, amPm)
    }

    fun epochToTime(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "%02d:%02d".format(local.hour, local.minute)
    }

    fun epochToTime24h(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "%d:%02d:%02d".format(local.hour, local.minute, local.second)
    }

    fun epochToDateTime(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val monthAbbr = local.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val hour12 = if (local.hour == 0) 12 else if (local.hour > 12) local.hour - 12 else local.hour
        val amPm = if (local.hour < 12) "AM" else "PM"
        return "$monthAbbr ${local.day}, ${local.year} %02d:%02d %s".format(hour12, local.minute, amPm)
    }

    fun epochToDisplayDateTime(epochMs: Long): String = epochToDateTime(epochMs)

    fun epochToDateSlash(epochMs: Long): String {
        val local = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "%02d/%02d/%d".format(local.day, local.month.number, local.year)
    }

    fun monthStartMillis(): Long {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val monthStart = LocalDateTime(now.year, now.month, 1, 0, 0, 0)
        return monthStart.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun getDurationDifferenceInMinutes(pastTimeInMillis: Long): Long {
        val diffMs = kotlin.time.Clock.System.now().toEpochMilliseconds() - pastTimeInMillis
        return diffMs / 60_000L
    }

    fun dateStringToMilliseconds(dateString: String): Long {
        return try {
            val parts = dateString.split("-")
            if (parts.size != 3) return -1L
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            val date = LocalDateTime(year, month, day, 0, 0, 0)
            date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } catch (e: Exception) {
            -1L
        }
    }
}
