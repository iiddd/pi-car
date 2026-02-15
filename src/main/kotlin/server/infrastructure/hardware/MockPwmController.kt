package server.infrastructure.hardware

import server.domain.ports.PwmController

/**
 * Mock PWM controller for testing and development without hardware.
 * Logs PWM operations periodically without actually controlling hardware.
 */
class MockPwmController : PwmController {

    private val channelStates = mutableMapOf<Int, Int>()
    private var logCounter = 0
    private val LOG_EVERY_N_CALLS = 100  // Log once every ~2 seconds at 50Hz

    init {
        println("🧪 Mock PWM Controller initialized")
    }

    override fun setDutyUs(channel: Int, dutyUs: Int) {
        channelStates[channel] = dutyUs

        // Only log periodically to avoid spam
        logCounter++
        if (logCounter >= LOG_EVERY_N_CALLS) {
            logCounter = 0
            val statesStr = channelStates.entries.sortedBy { it.key }
                .joinToString(", ") { "ch${it.key}=${it.value}µs" }
            println("🧪 [MOCK] PWM: $statesStr")
        }
    }

    override fun close() {
        println("🧪 [MOCK] PWM Controller closed")
        channelStates.clear()
    }

    /**
     * Get the current duty cycle for a channel (useful for testing).
     */
    fun getDutyUs(channel: Int): Int? = channelStates[channel]

    /**
     * Get all channel states (useful for testing).
     */
    fun getAllStates(): Map<Int, Int> = channelStates.toMap()
}
