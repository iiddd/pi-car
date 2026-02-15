package server.control

import kotlinx.serialization.Serializable

/**
 * WebSocket message protocol for GTA-style control.
 * All messages use JSON serialization.
 */

// ============================================================
// INCOMING MESSAGES (Client -> Server)
// ============================================================

/**
 * Control input message from client.
 * Sent at up to 50Hz when inputs change.
 */
@Serializable
data class ControlInput(
    val type: String,           // "keys", "analog", "deadman"
    val deadman: Boolean,       // Must be true for any motion
    val forward: Boolean = false,   // W key
    val backward: Boolean = false,  // S key
    val left: Boolean = false,      // A key
    val right: Boolean = false,     // D key
    val throttle: Float? = null,    // Analog throttle [-1..1] (optional)
    val steer: Float? = null,       // Analog steer [-1..1] (optional)
    val timestamp: Long = System.currentTimeMillis()
)

// ============================================================
// OUTGOING MESSAGES (Server -> Client)
// ============================================================

/**
 * Telemetry message sent to client at control loop rate.
 */
@Serializable
data class Telemetry(
    val escPulseUs: Int,        // Current ESC pulse width
    val steerPulseUs: Int,      // Current steering pulse width
    val speed: Float,           // Normalized speed [-1..1]
    val steer: Float,           // Normalized steer [-1..1]
    val deadmanActive: Boolean, // Whether motion is enabled
    val stale: Boolean,         // True if input timed out
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Status message for connection state changes.
 */
@Serializable
data class StatusMessage(
    val type: String,           // "connected", "error", "info"
    val message: String,
    val mockMode: Boolean = false
)

// ============================================================
// CONTROL ENVELOPE (wraps all message types)
// ============================================================

/**
 * Message envelope for type discrimination.
 */
@Serializable
data class WsMessage(
    val type: String,           // "input", "telemetry", "status"
    val input: ControlInput? = null,
    val telemetry: Telemetry? = null,
    val status: StatusMessage? = null
)

