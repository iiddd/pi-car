package server.infrastructure.hardware

import com.diozero.api.I2CDevice
import com.diozero.devices.PCA9685
import server.domain.ports.PwmController

/**
 * Real PCA9685 hardware implementation.
 * This class directly interfaces with the PCA9685 PWM controller via I2C.
 */
class Pca9685PwmController(
    i2cBus: Int = 1,
    i2cAddress: Int = PCA9685.DEFAULT_ADDRESS
) : PwmController {

    private val device: I2CDevice = I2CDevice(i2cBus, i2cAddress)
    private val pca9685: PCA9685 = PCA9685(device)

    init {
        println("✅ PCA9685 PWM Controller initialized on bus $i2cBus, address 0x${i2cAddress.toString(16)}")
    }

    override fun setDutyUs(channel: Int, dutyUs: Int) {
        pca9685.setDutyUs(channel, dutyUs)
    }

    override fun close() {
        println("🔌 Closing PCA9685 PWM Controller")
        pca9685.close()
    }
}
