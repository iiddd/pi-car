package server.domain.ports

/**
 * Port for motor/throttle control - domain interface for ESC control.
 */
interface MotorController {
    /**
     * Set throttle percentage.
     * @param percent Value from -1.0 (full reverse) to 1.0 (full forward), 0 = neutral
     */
    fun setThrottle(percent: Float)

    /**
     * Stop the motor (set to neutral).
     */
    fun stop()
}
