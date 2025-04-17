package server.gpio

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import server.Config

object PiGPIOManager {
    private val pi4j: Context? = if (!Config.mockMode) Pi4J.newAutoContext() else null
    private val servo: Pwm? = if (!Config.mockMode) setupServo(18) else null
    private val esc: Pwm? = if (!Config.mockMode) setupESC(19) else null

    private fun setupServo(pin: Int): Pwm =
        pi4j!!.create(
            Pwm.newConfigBuilder(pi4j)
                .id("servo")
                .address(pin)
                .pwmType(PwmType.SOFTWARE)
                .initial(1500)
                .frequency(50)
                .build()
        )

    private fun setupESC(pin: Int): Pwm =
        pi4j!!.create(
            Pwm.newConfigBuilder(pi4j)
                .id("esc")
                .address(pin)
                .pwmType(PwmType.SOFTWARE)
                .initial(1500)
                .frequency(50)
                .build()
        )

    fun setServoAngle(angle: Int) {
        if (Config.mockMode) {
            println("Mock: setServoAngle($angle)")
        } else {
            val pulseWidth = 1000 + ((angle + 100).coerceIn(0, 200) * 5)
            servo?.dutyCycle(pulseWidth / 20000.0 * 100)
        }
    }

    fun setThrottle(value: Int) {
        if (Config.mockMode) {
            println("Mock: setThrottle($value)")
        } else {
            val pulseWidth = 1500 + value.coerceIn(-100, 100) * 5
            esc?.dutyCycle(pulseWidth / 20000.0 * 100)
        }
    }
}