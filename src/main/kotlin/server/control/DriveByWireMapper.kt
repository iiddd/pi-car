package server.control

import server.Config
import kotlin.math.abs
import kotlin.math.sign

/**
 * DriveByWire mapper - converts normalized speed/steer to PWM pulses.
 *
 * Features:
 * - Expo curve for finer control around center
 * - Throttle deadband handling (respects motor dead zone config)
 * - Ramp smoothing to avoid pulse jumps
 * - Uses calibration from Config (single source of truth)
 */
class DriveByWireMapper(
    // Expo curve strength (0 = linear, 1 = max expo)
    private val throttleExpo: Float = 0.3f,
    private val steerExpo: Float = 0.2f,
    // Ramp rate limit (max µs change per tick at 50Hz)
    private val maxEscRampPerTick: Int = 20,
    private val maxSteerRampPerTick: Int = 40
) {
    // Current pulse values (for ramping)
    private var currentEscPulse: Int = Config.motorConfig.neutralPulseUs
    private var currentSteerPulse: Int = Config.servoConfig.centerPulseUs

    /**
     * Map normalized speed [-1..1] to ESC pulse width.
     * Respects motor dead zone configuration.
     */
    fun mapThrottleToPulse(speed: Float): Int {
        val motorConfig = Config.motorConfig

        // Apply expo curve for finer control around center
        val expoSpeed = applyExpo(speed, throttleExpo)

        val targetPulse = when {
            expoSpeed > 0.01f -> {
                // Forward: map 0..1 to forwardMinPulse..forwardMaxPulse
                val range = motorConfig.forwardMaxPulseUs - motorConfig.forwardMinPulseUs
                motorConfig.forwardMinPulseUs + (expoSpeed * range).toInt()
            }
            expoSpeed < -0.01f -> {
                // Reverse: map -1..0 to reverseMinPulse..reverseMaxPulse
                val range = motorConfig.reverseMaxPulseUs - motorConfig.reverseMinPulseUs
                motorConfig.reverseMaxPulseUs + (expoSpeed * range).toInt()
            }
            else -> {
                // Neutral (within deadband)
                motorConfig.neutralPulseUs
            }
        }

        // Apply ramp smoothing
        currentEscPulse = rampToward(currentEscPulse, targetPulse, maxEscRampPerTick)

        // Clamp to absolute limits
        return currentEscPulse.coerceIn(motorConfig.minPulseUs, motorConfig.maxPulseUs)
    }

    /**
     * Map normalized steer [-1..1] to servo pulse width.
     * -1 = full left, 0 = center, +1 = full right
     * Uses pulse-based calibration for precise control.
     */
    fun mapSteerToPulse(steer: Float): Int {
        val servoConfig = Config.servoConfig

        // Apply expo curve
        val expoSteer = applyExpo(steer, steerExpo)

        // Map directly to pulse values (more precise than angle-based)
        // -1 = leftPulseUs, 0 = centerPulseUs, +1 = rightPulseUs
        val targetPulse = when {
            expoSteer < 0 -> {
                // Interpolate between center and left
                val t = -expoSteer
                servoConfig.centerPulseUs + (t * (servoConfig.leftPulseUs - servoConfig.centerPulseUs)).toInt()
            }
            expoSteer > 0 -> {
                // Interpolate between center and right
                val t = expoSteer
                servoConfig.centerPulseUs + (t * (servoConfig.rightPulseUs - servoConfig.centerPulseUs)).toInt()
            }
            else -> servoConfig.centerPulseUs
        }

        // Apply ramp smoothing
        currentSteerPulse = rampToward(currentSteerPulse, targetPulse, maxSteerRampPerTick)

        // Clamp to servo limits
        return currentSteerPulse.coerceIn(servoConfig.minPulseUs, servoConfig.maxPulseUs)
    }

    /**
     * Get current ESC pulse (for telemetry).
     */
    fun getCurrentEscPulse(): Int = currentEscPulse

    /**
     * Get current steering pulse (for telemetry).
     */
    fun getCurrentSteerPulse(): Int = currentSteerPulse

    /**
     * Reset to neutral positions.
     */
    fun reset() {
        currentEscPulse = Config.motorConfig.neutralPulseUs
        currentSteerPulse = Config.servoConfig.centerPulseUs
    }

    /**
     * Apply expo curve: makes response less sensitive around center.
     * expo=0 is linear, expo=1 is cubic-like response.
     */
    private fun applyExpo(value: Float, expo: Float): Float {
        val absValue = abs(value)
        val curved = absValue * (1 - expo) + (absValue * absValue * absValue) * expo
        return curved * sign(value)
    }

    /**
     * Ramp current value toward target with max step.
     */
    private fun rampToward(current: Int, target: Int, maxStep: Int): Int {
        val diff = target - current
        return when {
            abs(diff) <= maxStep -> target
            diff > 0 -> current + maxStep
            else -> current - maxStep
        }
    }
}

