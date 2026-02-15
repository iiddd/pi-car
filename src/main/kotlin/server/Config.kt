package server

import server.data.config.HardwareConfig
import server.data.config.MotorConfig
import server.data.config.ServoConfig

/**
 * Global configuration object.
 *
 * SINGLE SOURCE OF TRUTH: All hardware config MUST be loaded from application.yaml.
 * The application will fail to start if config is not loaded before accessing.
 */
object Config {
    var mockMode: Boolean = false

    // Track if config has been loaded
    private var _configLoaded: Boolean = false

    // Private backing fields - will throw if accessed before loading
    private var _servoConfig: ServoConfig? = null
    private var _motorConfig: MotorConfig? = null

    /**
     * Servo configuration from application.yaml.
     * @throws IllegalStateException if config has not been loaded
     */
    var servoConfig: ServoConfig
        get() = _servoConfig ?: throw IllegalStateException(
            "❌ Config not loaded! Ensure application.yaml is present and loadHardwareConfig() is called before accessing servoConfig"
        )
        set(value) { _servoConfig = value }

    /**
     * Motor configuration from application.yaml.
     * @throws IllegalStateException if config has not been loaded
     */
    var motorConfig: MotorConfig
        get() = _motorConfig ?: throw IllegalStateException(
            "❌ Config not loaded! Ensure application.yaml is present and loadHardwareConfig() is called before accessing motorConfig"
        )
        set(value) { _motorConfig = value }

    /**
     * Check if hardware config has been loaded.
     */
    val isConfigLoaded: Boolean get() = _configLoaded

    /**
     * Load hardware configuration from parsed YAML config.
     * This MUST be called before accessing servoConfig or motorConfig.
     */
    fun loadHardwareConfig(hardwareConfig: HardwareConfig) {
        _servoConfig = hardwareConfig.servo
        _motorConfig = hardwareConfig.motor
        _configLoaded = true
        println("✅ Hardware config loaded from application.yaml:")
        println("   Servo: channel=${servoConfig.channel}, range=${servoConfig.minPulseUs}-${servoConfig.maxPulseUs}µs")
        println("   Servo positions: left=${servoConfig.leftPulseUs}µs, center=${servoConfig.centerPulseUs}µs, right=${servoConfig.rightPulseUs}µs")
        println("   Motor: channel=${motorConfig.channel}, range=${motorConfig.minPulseUs}-${motorConfig.maxPulseUs}µs, neutral=${motorConfig.neutralPulseUs}µs")
        println("   Motor dead zone: reverse=${motorConfig.reverseMaxPulseUs}µs ← neutral → forward=${motorConfig.forwardMinPulseUs}µs")
    }

    /**
     * Update servo calibration values at runtime.
     */
    fun updateServoCalibration(
        minPulseUs: Int? = null,
        maxPulseUs: Int? = null,
        centerPulseUs: Int? = null,
        leftPulseUs: Int? = null,
        rightPulseUs: Int? = null
    ) {
        servoConfig = servoConfig.copy(
            minPulseUs = minPulseUs ?: servoConfig.minPulseUs,
            maxPulseUs = maxPulseUs ?: servoConfig.maxPulseUs,
            centerPulseUs = centerPulseUs ?: servoConfig.centerPulseUs,
            leftPulseUs = leftPulseUs ?: servoConfig.leftPulseUs,
            rightPulseUs = rightPulseUs ?: servoConfig.rightPulseUs
        )
        println("🔧 Servo calibration updated: $servoConfig")
    }

    /**
     * Update motor calibration values at runtime.
     */
    fun updateMotorCalibration(
        minPulseUs: Int? = null,
        maxPulseUs: Int? = null,
        neutralPulseUs: Int? = null,
        forwardMinPulseUs: Int? = null,
        forwardMaxPulseUs: Int? = null,
        reverseMaxPulseUs: Int? = null,
        reverseMinPulseUs: Int? = null
    ) {
        motorConfig = motorConfig.copy(
            minPulseUs = minPulseUs ?: motorConfig.minPulseUs,
            maxPulseUs = maxPulseUs ?: motorConfig.maxPulseUs,
            neutralPulseUs = neutralPulseUs ?: motorConfig.neutralPulseUs,
            forwardMinPulseUs = forwardMinPulseUs ?: motorConfig.forwardMinPulseUs,
            forwardMaxPulseUs = forwardMaxPulseUs ?: motorConfig.forwardMaxPulseUs,
            reverseMaxPulseUs = reverseMaxPulseUs ?: motorConfig.reverseMaxPulseUs,
            reverseMinPulseUs = reverseMinPulseUs ?: motorConfig.reverseMinPulseUs
        )
        println("🔧 Motor calibration updated: $motorConfig")
    }
}
