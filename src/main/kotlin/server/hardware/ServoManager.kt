package server.hardware

import server.domain.ports.PwmController
import server.domain.ports.SteeringController

/**
 * Servo manager implementing the SteeringController port.
 * Controls steering servo using PWM signals.
 * Uses pulse-based calibration for precise control.
 */
class ServoManager(
    private val pwmController: PwmController,
    private val servoChannel: Int = 0,
    private val minPulseUs: Int = 1000,
    private val maxPulseUs: Int = 2000,
    // Pulse-based calibration (precise steering positions)
    private val centerPulseUs: Int = 1500,
    private val leftPulseUs: Int = 1200,
    private val rightPulseUs: Int = 1800
) : SteeringController {

    init {
        println("⚙️ ServoManager initialized on channel $servoChannel")
    }

    override fun setAngle(angle: Float) {
        // Convert angle (0-180) to pulse using linear interpolation
        val pulse = angleToPulse(angle)
        println("🛞 Setting steering angle: $angle° -> Pulse: $pulse µs")
        pwmController.setDutyUs(servoChannel, pulse)
    }

    override fun center() {
        println("🛞 Centering steering -> $centerPulseUs µs")
        pwmController.setDutyUs(servoChannel, centerPulseUs)
    }

    override fun turnLeft() {
        println("↩️ Steering left -> $leftPulseUs µs")
        pwmController.setDutyUs(servoChannel, leftPulseUs)
    }

    override fun turnRight() {
        println("↪️ Steering right -> $rightPulseUs µs")
        pwmController.setDutyUs(servoChannel, rightPulseUs)
    }

    override fun shutdown() {
        println("🛑 Shutting down ServoManager (setting servo to neutral)")
        center()
    }

    /**
     * Convert angle (0-180) to pulse width using linear interpolation.
     * This is kept for backward compatibility with setAngle() interface.
     */
    fun angleToPulse(angle: Float): Int {
        val normalizedAngle = (angle.coerceIn(0f, 180f)) / 180f
        return minPulseUs + ((maxPulseUs - minPulseUs) * normalizedAngle).toInt()
    }
}