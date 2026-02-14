package server.infrastructure.hardware

import server.domain.ports.PwmController

/**
 * A wrapper around PwmController that enforces channel-specific pulse limits.
 * This prevents accidental damage from out-of-bounds PWM values in production.
 *
 * The calibration tool's direct pulse endpoint should use the underlying
 * controller directly to bypass these limits.
 */
class SafePwmController(
    private val delegate: PwmController,
    private val channelLimits: Map<Int, ChannelLimits> = emptyMap(),
    private val globalMinPulseUs: Int = 500,
    private val globalMaxPulseUs: Int = 2500
) : PwmController {

    data class ChannelLimits(
        val minPulseUs: Int,
        val maxPulseUs: Int,
        val name: String = "Channel"
    )

    override fun setDutyUs(channel: Int, dutyUs: Int) {
        val limits = channelLimits[channel]

        val (clampedPulse, wasClipped) = if (limits != null) {
            val clamped = dutyUs.coerceIn(limits.minPulseUs, limits.maxPulseUs)
            val clipped = clamped != dutyUs
            if (clipped) {
                println("⚠️ WARNING: ${limits.name} (ch$channel) pulse $dutyUs µs clamped to $clamped µs (limits: ${limits.minPulseUs}-${limits.maxPulseUs})")
            }
            clamped to clipped
        } else {
            // Apply global limits if no channel-specific limits
            val clamped = dutyUs.coerceIn(globalMinPulseUs, globalMaxPulseUs)
            val clipped = clamped != dutyUs
            if (clipped) {
                println("⚠️ WARNING: Channel $channel pulse $dutyUs µs clamped to $clamped µs (global limits: $globalMinPulseUs-$globalMaxPulseUs)")
            }
            clamped to clipped
        }

        delegate.setDutyUs(channel, clampedPulse)
    }

    override fun close() {
        delegate.close()
    }

    /**
     * Direct access to underlying controller - USE WITH CAUTION!
     * This bypasses safety limits and should only be used for calibration.
     */
    fun unsafeSetDutyUs(channel: Int, dutyUs: Int) {
        println("🔧 [CALIBRATION] Direct pulse: channel $channel → $dutyUs µs (bypassing limits)")
        delegate.setDutyUs(channel, dutyUs)
    }
}

