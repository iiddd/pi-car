package server.routes

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import server.CarController

@Serializable
data class ControlCommand(
    val type: String,          // "steering", "throttle", "stop"
    val value: Float? = null   // angle for steering (-1 to 1), throttle percent (-1 to 1)
)

fun Route.setupWebSocketRoutes(carController: CarController?) {
    webSocket("/control") {
        println("🔗 WebSocket client connected")

        if (carController == null) {
            send("⚠️ Running in mock mode - commands will be logged but not executed")
        } else {
            send("✅ Connected to Pi-Car controller")
        }

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    println("📩 Received: $text")

                    try {
                        val command = Json.decodeFromString<ControlCommand>(text)
                        handleCommand(carController, command)
                        send("✅ Command executed: ${command.type}")
                    } catch (e: Exception) {
                        println("❌ Invalid command: ${e.message}")
                        send("❌ Invalid command format")
                    }
                }
            }
        } finally {
            println("🔌 WebSocket client disconnected")
            carController?.neutralThrottle()
            carController?.centerSteering()
        }
    }
}

private fun handleCommand(carController: CarController?, command: ControlCommand) {
    when (command.type) {
        "steering" -> {
            val value = command.value ?: 0f
            // Convert -1..1 to steering angle (90..150, center at 120)
            val angle = 120f + (value * 30f)
            println("🛞 Steering: $value → angle $angle")
            carController?.setSteeringAngle(angle)
        }
        "throttle" -> {
            val value = command.value ?: 0f
            println("🚀 Throttle: $value")
            carController?.setThrottlePercent(value)
        }
        "stop" -> {
            println("🛑 Emergency stop!")
            carController?.neutralThrottle()
            carController?.centerSteering()
        }
        else -> {
            println("❓ Unknown command type: ${command.type}")
        }
    }
}