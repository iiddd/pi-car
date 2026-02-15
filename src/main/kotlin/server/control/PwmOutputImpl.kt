package server.control

import server.Config
import server.domain.ports.PwmController
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Real PWM output implementation using PwmController (Diozero + PCA9685).
 *
 * Includes error handling for I2C communication failures which can occur
 * due to electrical noise from motor/ESC interference.
 */
class RealPwmOutput(
    private val pwmController: PwmController,
    private val maxRetries: Int = 2,
    private val errorCooldownMs: Long = 100
) : PwmOutput {

    // Track consecutive errors for logging
    private val consecutiveErrors = AtomicInteger(0)
    private val lastErrorTime = AtomicLong(0)
    private val totalErrors = AtomicLong(0)

    override fun setEscPulseUs(us: Int) {
        safeSetDuty(Config.motorConfig.channel, us, "ESC")
    }

    override fun setSteerPulseUs(us: Int) {
        safeSetDuty(Config.servoConfig.channel, us, "Steer")
    }

    /**
     * Attempt to set PWM with retry logic for I2C errors.
     * I2C can fail due to electrical noise from motor/ESC.
     */
    private fun safeSetDuty(channel: Int, us: Int, name: String) {
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                pwmController.setDutyUs(channel, us)

                // Success - reset error counter
                if (consecutiveErrors.get() > 0) {
                    println("✅ I2C recovered after ${consecutiveErrors.get()} errors")
                    consecutiveErrors.set(0)
                }
                return

            } catch (e: Exception) {
                lastException = e
                totalErrors.incrementAndGet()

                // Brief delay before retry
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(5)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }

        // All retries failed
        val errorCount = consecutiveErrors.incrementAndGet()
        val now = System.currentTimeMillis()

        // Rate-limit error logging to avoid spam
        if (now - lastErrorTime.get() > errorCooldownMs) {
            lastErrorTime.set(now)
            println("⚠️ I2C error on $name (channel $channel): ${lastException?.message} [consecutive: $errorCount, total: ${totalErrors.get()}]")
        }

        // After many consecutive errors, log a warning about possible hardware issues
        if (errorCount == 10) {
            println("🔴 Multiple I2C failures! Check: loose wires, electrical noise, I2C bus health")
        }
    }

    /**
     * Get error statistics for diagnostics.
     */
    fun getErrorStats(): Map<String, Long> = mapOf(
        "consecutiveErrors" to consecutiveErrors.get().toLong(),
        "totalErrors" to totalErrors.get()
    )
}

/**
 * Mock PWM output for local development (no GPIO).
 * Logs values for debugging.
 */
class MockPwmOutput : PwmOutput {
    var lastEscPulse: Int = 1500
        private set
    var lastSteerPulse: Int = 1500
        private set

    private var logCounter = 0

    override fun setEscPulseUs(us: Int) {
        lastEscPulse = us
        maybeLog()
    }

    override fun setSteerPulseUs(us: Int) {
        lastSteerPulse = us
        maybeLog()
    }

    // Log every 50 calls (once per second at 50Hz)
    private fun maybeLog() {
        logCounter++
        if (logCounter >= 50) {
            logCounter = 0
            println("🎮 MockPWM: ESC=${lastEscPulse}µs, Steer=${lastSteerPulse}µs")
        }
    }
}

