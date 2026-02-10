package server

import server.hardware.MotorManager
import server.hardware.ServoManager

class CarController(
    val servoManager: ServoManager,
    val motorManager: MotorManager
) {

    init {
        println("⚙️ Initializing Car Controller...")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("🛑 Shutdown hook triggered: Stopping motor and centering steering")
            shutdown()
        })
        println("✅ Car Controller ready")
    }

    // --- 🛞 Steering Methods ---
    fun centerSteering() {
        println("🛞 Centering steering")
        servoManager.centerSteering()
    }

    fun steerLeft() {
        println("↩️ Steering left")
        servoManager.turnLeft()
    }

    fun steerRight() {
        println("↪️ Steering right")
        servoManager.turnRight()
    }

    // --- 🚀 Throttle Methods ---
    fun setThrottlePercent(percent: Float) {
        motorManager.setThrottle(percent)
    }

    fun neutralThrottle() {
        motorManager.stopMotor()
    }

    fun forwardThrottle() {
        motorManager.setThrottle(0.3f)
    }

    fun reverseThrottle() {
        motorManager.setThrottle(-0.3f)
    }

    // --- 🛑 Shutdown ---
    fun shutdown() {
        println("🛑 Shutting down CarController...")
        motorManager.stopMotor()
        servoManager.shutdown()
    }
}