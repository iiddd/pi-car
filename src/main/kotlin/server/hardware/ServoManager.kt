package server.hardware

import server.domain.ports.PwmController
import server.domain.ports.SteeringController

/**
 * Servo manager implementing the SteeringController port.
 * Controls steering servo using PWM signals.
 */
class ServoManager(
    private val pwmController: PwmController,
    private val servoChannel: Int = 0,
    private val minPulseUs: Int = 1000,
    private val maxPulseUs: Int = 2000,
    private val minAngle: Float = 0f,
    private val maxAngle: Float = 180f,
    private val centerAngle: Float = 120f,
    private val leftAngle: Float = 90f,
    private val rightAngle: Float = 150f
) : SteeringController {

    init {
        println("⚙️ ServoManager initialized on channel $servoChannel")
    }

    override fun setAngle(angle: Float) {
        val clampedAngle = angle.coerceIn(minAngle, maxAngle)
        val pulse = angleToPulse(clampedAngle)
        println("🛞 Setting steering angle: $angle° -> Pulse: $pulse µs")
        pwmController.setDutyUs(servoChannel, pulse)
    }

    override fun center() {
        println("🛞 Centering steering")
        setAngle(centerAngle)
    }

    override fun turnLeft() {
        println("↩️ Steering left")
        setAngle(leftAngle)
    }

    override fun turnRight() {
        println("↪️ Steering right")
        setAngle(rightAngle)
    }

    override fun shutdown() {
        println("🛑 Shutting down ServoManager (setting servo to neutral)")
        center()
    }

    /**
     * Convert angle to pulse width (useful for testing).
     */
    fun angleToPulse(angle: Float): Int {
        val normalizedAngle = (angle - minAngle) / (maxAngle - minAngle)
        return minPulseUs + ((maxPulseUs - minPulseUs) * normalizedAngle).toInt()
    }
}