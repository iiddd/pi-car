package server.infrastructure.hardware

import server.domain.ports.PwmController

/**
 * Mock PWM controller for testing and development without hardware.
 * Logs all PWM operations without actually controlling hardware.
 */
class MockPwmController : PwmController {

    private val channelStates = mutableMapOf<Int, Int>()

    init {
        println("🧪 Mock PWM Controller initialized")
    }

    override fun setDutyUs(channel: Int, dutyUs: Int) {
        channelStates[channel] = dutyUs
        println("🧪 [MOCK] Channel $channel → $dutyUs µs")
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
