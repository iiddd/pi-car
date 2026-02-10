package server.hardware

import com.diozero.devices.PCA9685

class MotorManager(
    private val pca9685: PCA9685,
    private val motorChannel: Int, // Which channel the ESC is connected to
    private val minPulseUs: Int = 1000, // Minimum pulse for throttle
    private val maxPulseUs: Int = 2000, // Maximum pulse for throttle
    private val neutralPulseUs: Int = 1500 // Neutral (no movement)
) {
    init {
        println("⚙️ MotorManager initialized on channel $motorChannel")
    }

    fun setThrottle(throttlePercent: Float) {
        val clampedThrottle = throttlePercent.coerceIn(-1.0f, 1.0f)

        val pulse = when {
            clampedThrottle > 0 -> neutralPulseUs + ((maxPulseUs - neutralPulseUs) * clampedThrottle).toInt()
            clampedThrottle < 0 -> neutralPulseUs + ((neutralPulseUs - minPulseUs) * clampedThrottle).toInt()
            else -> neutralPulseUs
        }

        println("🚀 Setting throttle: $throttlePercent -> Pulse: $pulse µs")
        pca9685.setDutyUs(motorChannel, pulse)
    }

    fun stopMotor() {
        println("🛑 Stopping motor (neutral)")
        pca9685.setDutyUs(motorChannel, neutralPulseUs)
    }
}