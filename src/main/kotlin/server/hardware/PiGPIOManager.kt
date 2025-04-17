package server.hardware

import com.pi4j.Pi4J
import com.pi4j.context.Context
import server.Config

object PiGPIOManager {

    private val pi4j: Context? = if (!Config.mockMode) Pi4J.newAutoContext() else null
    private val pwm: PCA9685Controller? = pi4j?.let { PCA9685Controller(it) }

    fun setServoAngle(angle: Int) {
        if (Config.mockMode) {
            println("🟡 Mock: servoAngle($angle)")
        } else {
            pwm?.setServoAngle(0, angle)
        }
    }

    fun setThrottle(value: Int) {
        if (Config.mockMode) {
            println("🟡 Mock: throttle($value)")
        } else {
            pwm?.setThrottle(0, value)
        }
    }
}