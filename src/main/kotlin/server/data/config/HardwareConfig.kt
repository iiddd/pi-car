package server.data.config

import kotlinx.serialization.Serializable

/**
 * Hardware configuration loaded from application.yaml
 *
 * SINGLE SOURCE OF TRUTH: All values MUST be defined in application.yaml.
 * The application will fail to start if any required config is missing.
 */
@Serializable
data class HardwareConfig(
    val servo: ServoConfig,
    val motor: MotorConfig
)

@Serializable
data class ServoConfig(
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
data class MotorConfig(
    val channel: Int,
    val minPulseUs: Int,
    val maxPulseUs: Int,
    val neutralPulseUs: Int,
    // Dead zone configuration
    val forwardMinPulseUs: Int,   // Where motor starts moving forward
    val forwardMaxPulseUs: Int,   // Full throttle forward
    val reverseMaxPulseUs: Int,   // Where motor starts moving reverse
    val reverseMinPulseUs: Int    // Full throttle reverse
)
