package server

import server.domain.ports.MotorController
import server.domain.ports.SteeringController

/**
 * Main car controller orchestrating steering and motor operations.
 * Depends on abstractions (interfaces) for testability.
 */
class CarController(
    private val steeringController: SteeringController,
    private val motorController: MotorController,
    registerShutdownHook: Boolean = true
) {

    init {
        println("⚙️ Initializing Car Controller...")
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(Thread {
                println("🛑 Shutdown hook triggered: Stopping motor and centering steering")
                shutdown()
            })
        }
        println("✅ Car Controller ready")
    }

    // --- 🛞 Steering Methods ---
    fun centerSteering() {
        println("🛞 Centering steering")
        steeringController.center()
    }

    fun steerLeft() {
        println("↩️ Steering left")
        steeringController.turnLeft()
    }

    fun steerRight() {
        println("↪️ Steering right")
        steeringController.turnRight()
    }

    fun setSteeringAngle(angle: Float) {
        steeringController.setAngle(angle)
    }

    // --- 🚀 Throttle Methods ---
    fun setThrottlePercent(percent: Float) {
        motorController.setThrottle(percent)
    }

    fun neutralThrottle() {
        motorController.stop()
    }

    fun forwardThrottle() {
        motorController.setThrottle(0.3f)
    }

    fun reverseThrottle() {
        motorController.setThrottle(-0.3f)
    }

    // --- 🛑 Shutdown ---
    fun shutdown() {
        println("🛑 Shutting down CarController...")
        motorController.stop()
        steeringController.shutdown()
    }
}