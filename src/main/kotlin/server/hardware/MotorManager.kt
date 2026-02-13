package server.hardware

import server.domain.ports.MotorController
import server.domain.ports.PwmController

/**
 * Motor/ESC manager implementing the MotorController port.
 * Controls brushless motor via ESC using PWM signals.
 */
class MotorManager(
    private val pwmController: PwmController,
    private val motorChannel: Int,
    private val minPulseUs: Int = 1000,
    private val maxPulseUs: Int = 2000,
    private val neutralPulseUs: Int = 1500
) : MotorController {

    init {
        println("⚙️ MotorManager initialized on channel $motorChannel")
    }

    override fun setThrottle(percent: Float) {
        val clampedThrottle = percent.coerceIn(-1.0f, 1.0f)

        val pulse = when {
            clampedThrottle > 0 -> neutralPulseUs + ((maxPulseUs - neutralPulseUs) * clampedThrottle).toInt()
            clampedThrottle < 0 -> neutralPulseUs + ((neutralPulseUs - minPulseUs) * clampedThrottle).toInt()
            else -> neutralPulseUs
        }

        println("🚀 Setting throttle: $percent -> Pulse: $pulse µs")
        pwmController.setDutyUs(motorChannel, pulse)
    }

    override fun stop() {
        println("🛑 Stopping motor (neutral)")
        pwmController.setDutyUs(motorChannel, neutralPulseUs)
    }

    /**
     * Calculate pulse width for given throttle (useful for testing).
     */
    fun calculatePulse(throttlePercent: Float): Int {
        val clampedThrottle = throttlePercent.coerceIn(-1.0f, 1.0f)
        return when {
            clampedThrottle > 0 -> neutralPulseUs + ((maxPulseUs - neutralPulseUs) * clampedThrottle).toInt()
            clampedThrottle < 0 -> neutralPulseUs + ((neutralPulseUs - minPulseUs) * clampedThrottle).toInt()
            else -> neutralPulseUs
        }
    }
}