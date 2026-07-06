package com.mileway.feature.tracking.service.motion

import com.mileway.feature.tracking.service.location.SensorSnapshot

/** Combined read of one [SensorSnapshot]: stillness + harsh-accel + possible-mock-spin. */
data class ImuAnalysis(
    val motionStill: Boolean,
    val harshAccel: Boolean,
    val possibleSpin: Boolean,
)

/**
 * Wave-2 IMU polish: folds [HarshAccelDetector] + [GyroSpinHeuristic] over a raw [SensorSnapshot]
 * into one [ImuAnalysis]. Stillness itself is fused elsewhere (accelerometer + gravity filter +
 * Play Services activity recognition, see `LocationTrackingService.onFix`) since that already
 * needs cross-fix gravity state this pure per-sample analyzer doesn't carry — it's passed in
 * rather than recomputed here.
 */
object ImuAnalyzer {
    fun analyze(
        sensors: SensorSnapshot,
        motionStill: Boolean,
    ): ImuAnalysis =
        ImuAnalysis(
            motionStill = motionStill,
            harshAccel = HarshAccelDetector.isHarsh(sensors.accelX, sensors.accelY, sensors.accelZ),
            possibleSpin = GyroSpinHeuristic.isPossibleSpin(sensors.gyroX, sensors.gyroY, sensors.gyroZ),
        )
}
