package server.hardware

import com.pi4j.context.Context
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CConfig

class PCA9685Controller(private val pi4j: Context, private val address: Int = 0x40) {

    private val device: I2C

    init {
        val config: I2CConfig = I2C.newConfigBuilder(pi4j)
            .bus(1)
            .device(address)
            .id("pca9685")
            .name("PWM Controller")
            .build()

        device = pi4j.create(config)

        println("⚙️ Initializing PCA9685 controller...")

        setPWMFreq(50f) // This handles sleep, prescale, restart

        println("✅ PCA9685 ready for use.")
    }

    private fun setPWMFreq(freqHz: Float) {
        val prescaleval = 25000000.0 / 4096.0 / freqHz - 1.0
        val prescale = prescaleval.toInt().coerceIn(3, 255)

        val MODE1 = 0x00
        val PRESCALE = 0xFE

        // Step 1: Read current MODE1
        val oldMode = device.readRegister(MODE1)
        println("🕵️‍♂️ Current MODE1: 0x%02X".format(oldMode))

        // Step 2: Enter sleep
        val sleepMode = (oldMode.toInt() and 0x7F or 0x10).toByte()
        device.writeRegister(MODE1, sleepMode)
        Thread.sleep(5)

        // Step 3: Write prescale
        println("⚙️ Writing prescale $prescale")
        device.writeRegister(PRESCALE, prescale.toByte())
        Thread.sleep(5)

        // Step 4: Wake up by restoring old mode (likely 0x01 or 0x00)
        device.writeRegister(MODE1, 0x00.toByte()) // wake
        Thread.sleep(5)

        // Step 5: Enable auto-increment (AI) first
        device.writeRegister(MODE1, 0x20.toByte()) // AI = 1
        Thread.sleep(5)

        // Step 6: Restart + AI
        device.writeRegister(MODE1, 0xA1.toByte()) // Restart + AI
        Thread.sleep(5)

        val modeAfter = device.readRegister(MODE1)
        println("✅ Final MODE1 after wake + restart: 0x%02X".format(modeAfter))
    }

    fun setPulseMicroseconds(channel: Int, pulseMicroseconds: Int) {
        val pulseLength = 1_000_000 / 50 / 4096  // = 4.88 µs per bit
        val pulse = pulseMicroseconds / pulseLength
        setPWM(channel, 0, pulse)
    }

    fun setPWM(channel: Int, on: Int, off: Int) {
        val base = 0x06 + 4 * channel
        device.writeRegister(base, (on and 0xFF).toByte())
        device.writeRegister(base + 1, ((on shr 8) and 0xFF).toByte())
        device.writeRegister(base + 2, (off and 0xFF).toByte())
        device.writeRegister(base + 3, ((off shr 8) and 0xFF).toByte())
    }

    fun setServoAngle(channel: Int, angle: Int) {
        val clamped = angle.coerceIn(0, 180)
        val pulse = 1000 + (clamped * 1000 / 180) // 1000–2000 µs
        println("📤 Real: setServoAngle($angle) → channel $channel, pulse $pulse µs")
        setPulseMicroseconds(channel, pulse)
    }

    fun setThrottle(channel: Int, value: Int) {
        val clamped = value.coerceIn(-100, 100)
        val pulse = 1520 + clamped * 5 // e.g. -100 to +100 → ±500 µs
        println("📤 Real: setThrottle($value) → channel $channel, pulse $pulse µs")
        setPulseMicroseconds(channel, pulse)
    }
}