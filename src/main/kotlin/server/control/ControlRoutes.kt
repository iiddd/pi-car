package server.control

import io.ktor.server.application.call
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.Config
import java.util.concurrent.atomic.AtomicInteger

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// Track active connections
private val activeConnections = AtomicInteger(0)

/**
 * Setup WebSocket routes for GTA-style control.
 */
fun Route.setupControlRoutes(controlLoop: ControlLoop) {

    // Main control WebSocket endpoint
    webSocket("/ws") {
        val connectionId = activeConnections.incrementAndGet()
        println("🎮 Control client #$connectionId connected")

        // Send welcome message
        val statusMsg = WsMessage(
            type = "status",
            status = StatusMessage(
                type = "connected",
                message = "Connected to Pi-Car control server",
                mockMode = Config.mockMode
            )
        )
        send(json.encodeToString(statusMsg))

        // Start telemetry sender
        val telemetryJob = launch {
            controlLoop.telemetryFlow.collectLatest { telemetry ->
                try {
                    val msg = WsMessage(
                        type = "telemetry",
                        telemetry = telemetry
                    )
                    send(json.encodeToString(msg))
                } catch (e: Exception) {
                    // Connection closed
                }
            }
        }

        try {
            // Process incoming messages
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    processMessage(text, controlLoop)
                }
            }
        } catch (e: Exception) {
            println("⚠️ Control client #$connectionId error: ${e.message}")
        } finally {
            telemetryJob.cancel()
            controlLoop.clearInput()
            activeConnections.decrementAndGet()
            println("🔌 Control client #$connectionId disconnected")
        }
    }

    // Health check endpoint
    get("/health") {
        call.respondText("OK")
    }

    // Control loop status
    get("/control/status") {
        val status = mapOf(
            "running" to controlLoop.isRunning(),
            "paused" to controlLoop.isPaused()
        )
        call.respond(status)
    }

    // Pause control loop (for calibration)
    post("/control/pause") {
        controlLoop.pause()
        call.respondText("✅ Control loop paused - calibration mode enabled")
    }

    // Resume control loop
    post("/control/resume") {
        controlLoop.resume()
        call.respondText("✅ Control loop resumed")
    }
}

private fun processMessage(text: String, controlLoop: ControlLoop) {
    try {
        // First try to parse as direct ControlInput (which has type: "keys" or "analog")
        val input = json.decodeFromString<ControlInput>(text)

        // Valid control input types
        if (input.type == "keys" || input.type == "analog") {
            controlLoop.updateInput(input)
            return
        }

        // If not a control input, try as WsMessage wrapper
        val message = json.decodeFromString<WsMessage>(text)
        when (message.type) {
            "input" -> {
                message.input?.let { wrappedInput ->
                    controlLoop.updateInput(wrappedInput)
                }
            }
            else -> {
                // Silently ignore unknown types (like pings, etc.)
            }
        }
    } catch (e: Exception) {
        // Log only for debugging, don't spam console
        // println("❌ Failed to parse control message: ${e.message}")
    }
}

