package server.data.config

import kotlinx.serialization.Serializable

/**
 * Hardware configuration loaded from application.yaml
 */
@Serializable
data class HardwareConfig(
    val servo: ServoConfig,
    val motor: MotorConfig
)

@Serializable
data class ServoConfig(
    val channel: Int = 0,
    val minPulseUs: Int = 1000,
    val maxPulseUs: Int = 2000,
    val minAngle: Float = 0f,
    val maxAngle: Float = 180f,
    val centerAngle: Float = 120f,
    val leftAngle: Float = 90f,
    val rightAngle: Float = 150f
)

@Serializable
data class MotorConfig(
    val channel: Int = 1,
    val minPulseUs: Int = 1000,
    val maxPulseUs: Int = 2000,
    val neutralPulseUs: Int = 1500,
    val forwardMinPulseUs: Int = 1600,
    val reverseMaxPulseUs: Int = 1400
)

