package server.data.calibration

import kotlinx.serialization.Serializable

/**
 * Request to set a raw PWM pulse width for calibration purposes.
 */
@Serializable
data class SetPulseRequest(
    val channel: Int,
    val pulseUs: Int
)

/**
 * Request to test steering at a specific angle.
 */
@Serializable
data class SetSteeringAngleRequest(
    val angle: Float
)

/**
 * Request to test motor throttle.
 */
@Serializable
data class SetThrottleRequest(
    val throttlePercent: Float
)

/**
 * Request to update servo calibration values.
 */
@Serializable
data class UpdateServoCalibrationRequest(
    val minPulseUs: Int? = null,
    val maxPulseUs: Int? = null,
    val centerAngle: Float? = null,
    val leftAngle: Float? = null,
    val rightAngle: Float? = null
)

/**
 * Request to update motor calibration values.
 */
@Serializable
data class UpdateMotorCalibrationRequest(
    val minPulseUs: Int? = null,
    val maxPulseUs: Int? = null,
    val neutralPulseUs: Int? = null,
    val forwardMinPulseUs: Int? = null,
    val forwardMaxPulseUs: Int? = null,
    val reverseMaxPulseUs: Int? = null,
    val reverseMinPulseUs: Int? = null
)

/**
 * Response containing current calibration values.
 */
@Serializable
data class CalibrationResponse(
    val servo: ServoCalibration,
    val motor: MotorCalibration
)

@Serializable
data class ServoCalibration(
    val channel: Int,
    val minPulseUs: Int,
    val maxPulseUs: Int,
    val minAngle: Float,
    val maxAngle: Float,
    val centerAngle: Float,
    val leftAngle: Float,
    val rightAngle: Float
)

@Serializable
data class MotorCalibration(
    val channel: Int,
    val minPulseUs: Int,
    val maxPulseUs: Int,
    val neutralPulseUs: Int,
    val forwardMinPulseUs: Int,
    val forwardMaxPulseUs: Int,
    val reverseMaxPulseUs: Int,
    val reverseMinPulseUs: Int
)

