package server.control

/**
 * PWM output interface for control loop.
 * Abstracts hardware access for testability.
 */
interface PwmOutput {
    /**
     * Set ESC pulse width in microseconds.
     */
    fun setEscPulseUs(us: Int)

    /**
     * Set steering servo pulse width in microseconds.
     */
    fun setSteerPulseUs(us: Int)
}

