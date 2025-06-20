package com.miletracker.core.data.util

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object CommonUtils {

    fun formatDistance(distanceKm: Double): String = "%.1f km".format(distanceKm)

    fun formatDuration(durationMs: Long): String {
        val minutes = durationMs / 60_000
        return if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
    }

    fun formatSpeed(speedKmh: Double): String = "%.1f km/h".format(speedKmh)

    fun roundToTwoDecimals(value: Double): Double = (value * 100.0).roundToInt() / 100.0

    fun toTitleCase(input: String): String =
        input.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

    fun capitalizeFirstLetter(input: String): String =
        if (input.isEmpty()) input else input[0].uppercase() + input.substring(1)

    fun formatCurrencyAmount(amount: Double, currencySymbol: String = "₹"): String =
        "$currencySymbol%.2f".format(amount)

    fun roundValueUsingFormatter(value: Double, decimalPlaces: Int = 2): Double {
        val factor = 10.0.pow(decimalPlaces)
        return (value * factor).roundToInt() / factor
    }

    fun getDistanceFromLatLonInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatDistanceMeters(meters: Double): String =
        if (meters < 1000) "${meters.roundToInt()}m" else "%.1f km".format(meters / 1000.0)

    fun metersToKm(meters: Double): Double = roundToTwoDecimals(meters / 1000.0)

    fun kmToMeters(km: Double): Double = km * 1000.0

    fun isValidLatLng(lat: Double, lng: Double): Boolean =
        abs(lat) <= 90.0 && abs(lng) <= 180.0 && !(lat == 0.0 && lng == 0.0)
}
