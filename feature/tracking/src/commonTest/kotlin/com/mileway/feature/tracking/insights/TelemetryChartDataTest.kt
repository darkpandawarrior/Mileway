package com.mileway.feature.tracking.insights

import com.mileway.core.data.model.db.LocationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryChartDataTest {
    private fun point(
        speed: Float,
        altitude: Double,
        accelX: Float = 0f,
        accelY: Float = 0f,
        accelZ: Float = 0f,
    ) = LocationData(
        activity = "DRIVING",
        speed = speed,
        lat = 0.0,
        lng = 0.0,
        token = "tok",
        batteryPercentage = 100.0,
        altitude = altitude,
        accelerometerX = accelX,
        accelerometerY = accelY,
        accelerometerZ = accelZ,
    )

    @Test
    fun `empty points yields empty series`() {
        val series = TelemetryChartData.derive(emptyList())
        assertTrue(series.speedKmh.isEmpty)
        assertTrue(series.altitudeM.isEmpty)
        assertTrue(series.accelMagnitude.isEmpty)
    }

    @Test
    fun `single point is treated as empty guard`() {
        val series = TelemetryChartData.derive(listOf(point(speed = 10f, altitude = 5.0)))
        assertTrue(series.speedKmh.isEmpty)
    }

    @Test
    fun `derives speed in kmh with correct min max`() {
        val points = listOf(point(speed = 0f, altitude = 0.0), point(speed = 10f, altitude = 0.0))
        val series = TelemetryChartData.derive(points)
        assertEquals(listOf(0f, 36f), series.speedKmh.values)
        assertEquals(0f, series.speedKmh.min)
        assertEquals(36f, series.speedKmh.max)
    }

    @Test
    fun `derives altitude series directly from meters`() {
        val points = listOf(point(speed = 0f, altitude = 100.0), point(speed = 0f, altitude = 120.0))
        val series = TelemetryChartData.derive(points)
        assertEquals(listOf(100f, 120f), series.altitudeM.values)
    }

    @Test
    fun `derives accel magnitude from 3 axes`() {
        val points =
            listOf(
                point(speed = 0f, altitude = 0.0, accelX = 3f, accelY = 4f, accelZ = 0f),
                point(speed = 0f, altitude = 0.0, accelX = 0f, accelY = 0f, accelZ = 0f),
            )
        val series = TelemetryChartData.derive(points)
        assertEquals(5f, series.accelMagnitude.values[0])
        assertEquals(0f, series.accelMagnitude.values[1])
    }
}
