package server

import com.diozero.api.I2CDevice
import com.diozero.devices.PCA9685
fun main() {
    println("⚙️ ESC Test Program Starting...")

    val device = I2CDevice(1, PCA9685.DEFAULT_ADDRESS)
    val pca9685 = PCA9685(device)

    val motorChannel = 1

    // Step 1: Neutral pulse (arming)
    println("🛑 Sending NEUTRAL pulse (1500 µs)")
    pca9685.setDutyUs(motorChannel, 1500)
    Thread.sleep(5000) // wait 5 sec for ESC to arm

    // Step 2: Gentle throttle
    println("🚀 Sending FORWARD throttle (1600 µs)")
    pca9685.setDutyUs(motorChannel, 1600)
    Thread.sleep(3000)

    // Step 3: Back to neutral
    println("🛑 Sending NEUTRAL again (1500 µs)")
    pca9685.setDutyUs(motorChannel, 1500)
    Thread.sleep(3000)

    println("✅ ESC Test Completed!")
}