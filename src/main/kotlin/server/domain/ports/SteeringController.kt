package server.domain.ports

/**
 * Port for steering/servo control - domain interface for servo operations.
 */
interface SteeringController {
    /**
     * Set steering angle.
     * @param angle The steering angle in degrees
     */
    fun setAngle(angle: Float)

    /**
     * Center the steering (neutral position).
     */
    fun center()

    /**
     * Turn left (predefined angle).
     */
    fun turnLeft()

    /**
     * Turn right (predefined angle).
     */
    fun turnRight()

    /**
     * Shutdown and reset to safe position.
     */
    fun shutdown()
}
