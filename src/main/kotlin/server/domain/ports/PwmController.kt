package server.domain.ports

/**
 * Port for PWM control - abstracts hardware-specific PWM operations.
 * This interface allows testing without real hardware.
 */
interface PwmController {
    /**
     * Set the PWM duty cycle in microseconds for a specific channel.
     * @param channel The PWM channel (0-15 for PCA9685)
     * @param dutyUs The duty cycle in microseconds (typically 1000-2000 for servos/ESCs)
     */
    fun setDutyUs(channel: Int, dutyUs: Int)

    /**
     * Close the PWM controller and release resources.
     */
    fun close()
}
