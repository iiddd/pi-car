package server.control

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Authoritative 50Hz control loop.
 *
 * Runs server-side and:
 * - Processes input from clients
 * - Applies GTA driver logic
 * - Maps to PWM via DriveByWireMapper
 * - Outputs to hardware via PwmOutput
 * - Enforces failsafe timeout
 * - Emits telemetry
 */
class ControlLoop(
    private val pwmOutput: PwmOutput,
    private val tickRateHz: Int = 50,
    private val failsafeTimeoutMs: Long = 250,
    private val telemetryRateHz: Int = 10  // Emit telemetry at 10Hz to avoid overwhelming clients
) {
    private val driver = GtaDriver()
    private val mapper = DriveByWireMapper()

    // Latest input (thread-safe)
    private val latestInput = AtomicReference<ControlInput?>(null)
    private val lastInputTime = AtomicLong(0)

    // Telemetry flow
    private val _telemetryFlow = MutableSharedFlow<Telemetry>(replay = 1)
    val telemetryFlow: SharedFlow<Telemetry> = _telemetryFlow

    // Loop state
    private var loopJob: Job? = null
    private var isRunning = false

    // Pause state for calibration mode
    @Volatile
    private var isPaused = false

    // Telemetry throttling
    private var tickCounter = 0
    private val telemetryEveryNTicks = tickRateHz / telemetryRateHz

    /**
     * Check if control loop is paused (calibration mode).
     */
    fun isPaused(): Boolean = isPaused

    /**
     * Pause the control loop (for calibration).
     * The loop keeps running but doesn't send PWM outputs.
     */
    fun pause() {
        isPaused = true
        println("⏸️ Control loop paused (calibration mode)")
    }

    /**
     * Resume the control loop after calibration.
     */
    fun resume() {
        isPaused = false
        // Reset driver and mapper to neutral when resuming
        driver.reset()
        mapper.reset()
        println("▶️ Control loop resumed")
    }

    /**
     * Start the control loop.
     */
    fun start(scope: CoroutineScope) {
        if (isRunning) return

        isRunning = true
        println("🎮 Control loop starting at ${tickRateHz}Hz")

        loopJob = scope.launch {
            val tickIntervalMs = 1000L / tickRateHz
            var lastTickTime = System.currentTimeMillis()

            while (isActive && isRunning) {
                val now = System.currentTimeMillis()
                val deltaMs = now - lastTickTime
                lastTickTime = now

                tick(deltaMs / 1000f)

                // Sleep for remainder of tick interval
                val elapsed = System.currentTimeMillis() - now
                val sleepMs = (tickIntervalMs - elapsed).coerceAtLeast(1)
                delay(sleepMs)
            }
        }
    }

    /**
     * Stop the control loop.
     */
    fun stop() {
        isRunning = false
        loopJob?.cancel()
        loopJob = null

        // Force neutral
        driver.reset()
        mapper.reset()
        pwmOutput.setEscPulseUs(mapper.getCurrentEscPulse())
        pwmOutput.setSteerPulseUs(mapper.getCurrentSteerPulse())

        println("🛑 Control loop stopped")
    }

    /**
     * Update input from client.
     */
    fun updateInput(input: ControlInput) {
        latestInput.set(input)
        lastInputTime.set(System.currentTimeMillis())
    }

    /**
     * Clear input (client disconnected).
     */
    fun clearInput() {
        latestInput.set(null)
    }

    /**
     * Process one tick of the control loop.
     */
    private suspend fun tick(deltaSeconds: Float) {
        // If paused (calibration mode), don't process or output anything
        if (isPaused) {
            return
        }

        val now = System.currentTimeMillis()
        val input = latestInput.get()
        val timeSinceInput = now - lastInputTime.get()

        // Check failsafe
        val isStale = input == null || timeSinceInput > failsafeTimeoutMs
        val deadmanActive = !isStale && (input?.deadman == true)

        // Update GTA driver
        val effectiveInput = if (isStale) null else input
        driver.tick(deltaSeconds, effectiveInput, deadmanActive)

        // Map to PWM
        val escPulse = mapper.mapThrottleToPulse(driver.speed)
        val steerPulse = mapper.mapSteerToPulse(driver.steer)

        // Output to hardware
        pwmOutput.setEscPulseUs(escPulse)
        pwmOutput.setSteerPulseUs(steerPulse)

        // Emit telemetry at reduced rate (10Hz instead of 50Hz)
        tickCounter++
        if (tickCounter >= telemetryEveryNTicks) {
            tickCounter = 0
            val telemetry = Telemetry(
                escPulseUs = escPulse,
                steerPulseUs = steerPulse,
                speed = driver.speed,
                steer = driver.steer,
                deadmanActive = deadmanActive,
                stale = isStale
            )
            _telemetryFlow.emit(telemetry)
        }
    }

    /**
     * Check if control loop is running.
     */
    fun isRunning(): Boolean = isRunning
}

