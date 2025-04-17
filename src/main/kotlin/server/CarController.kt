package server

import server.hardware.PiGPIOManager

object CarController {
    fun handleCommand(command: String) {
        when {
            command.startsWith("steer:") -> {
                val value = command.removePrefix("steer:").toIntOrNull() ?: return
                PiGPIOManager.setServoAngle(value)
            }
            command.startsWith("throttle:") -> {
                val value = command.removePrefix("throttle:").toIntOrNull() ?: return
                PiGPIOManager.setThrottle(value)
            }
        }
    }
}