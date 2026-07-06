package com.mileway.feature.tracking.service.motion

import com.mileway.feature.tracking.service.location.SensorSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class ImuAnalyzerTest {
    @Test
    fun `all quiet and still`() {
        val result = ImuAnalyzer.analyze(SensorSnapshot(), motionStill = true)
        assertEquals(ImuAnalysis(motionStill = true, harshAccel = false, possibleSpin = false), result)
    }

    @Test
    fun `harsh accel while moving`() {
        val sensors = SensorSnapshot(accelX = 6f)
        val result = ImuAnalyzer.analyze(sensors, motionStill = false)
        assertEquals(ImuAnalysis(motionStill = false, harshAccel = true, possibleSpin = false), result)
    }

    @Test
    fun `possible spin while moving`() {
        val sensors = SensorSnapshot(gyroZ = 5f)
        val result = ImuAnalyzer.analyze(sensors, motionStill = false)
        assertEquals(ImuAnalysis(motionStill = false, harshAccel = false, possibleSpin = true), result)
    }

    @Test
    fun `both harsh accel and spin simultaneously`() {
        val sensors = SensorSnapshot(accelX = 6f, gyroZ = 5f)
        val result = ImuAnalyzer.analyze(sensors, motionStill = false)
        assertEquals(ImuAnalysis(motionStill = false, harshAccel = true, possibleSpin = true), result)
    }
}
