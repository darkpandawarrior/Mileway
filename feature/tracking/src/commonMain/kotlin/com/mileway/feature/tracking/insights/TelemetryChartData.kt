package com.mileway.feature.tracking.insights

import com.mileway.core.data.model.db.LocationData
import kotlin.math.sqrt

/**
 * Pure-Kotlin telemetry series derivation for the data-preview screen's graphs (Wave 3 live-map
 * polish). No Compose/Android imports, so it is unit-testable on the JVM like [com.mileway.feature
 * .tracking.map.MapRouteBuilder].
 *
 * Each series is a plain [List<Float>] in point order — a [SparklineChart] just draws it, no
 * further math needed at render time.
 */
object TelemetryChartData {
    /** One derived series plus its min/max for axis scaling. */
    data class Series(
        val values: List<Float>,
        val min: Float,
        val max: Float,
    ) {
        val isEmpty: Boolean get() = values.isEmpty()

        companion object {
            val EMPTY = Series(emptyList(), 0f, 0f)
        }
    }

    /** Speed (km/h), altitude (m) and combined accelerometer magnitude series for [points]. */
    data class TelemetrySeries(
        val speedKmh: Series,
        val altitudeM: Series,
        val accelMagnitude: Series,
    )

    /** Empty guard: an empty or single-point track yields all-empty series, never divides by zero. */
    fun derive(points: List<LocationData>): TelemetrySeries {
        if (points.size < 2) return TelemetrySeries(Series.EMPTY, Series.EMPTY, Series.EMPTY)

        val speed = points.map { it.speed * 3.6f }
        val altitude = points.map { it.altitude.toFloat() }
        val accel =
            points.map {
                sqrt(
                    it.accelerometerX * it.accelerometerX +
                        it.accelerometerY * it.accelerometerY +
                        it.accelerometerZ * it.accelerometerZ,
                )
            }

        return TelemetrySeries(
            speedKmh = speed.toSeries(),
            altitudeM = altitude.toSeries(),
            accelMagnitude = accel.toSeries(),
        )
    }

    private fun List<Float>.toSeries(): Series {
        if (isEmpty()) return Series.EMPTY
        return Series(values = this, min = min(), max = max())
    }
}
