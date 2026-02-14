package server

import server.data.config.HardwareConfig
import server.data.config.MotorConfig
import server.data.config.ServoConfig

/**
 * Global configuration object.
 * Hardware config can be loaded from YAML and updated at runtime for calibration.
 */
object Config {
    var mockMode: Boolean = false

    // Hardware configuration with mutable values for runtime calibration
    var servoConfig: ServoConfig = ServoConfig()
    var motorConfig: MotorConfig = MotorConfig()

    /**
     * Load hardware configuration from parsed YAML config.
     */
    fun loadHardwareConfig(hardwareConfig: HardwareConfig) {
        servoConfig = hardwareConfig.servo
        motorConfig = hardwareConfig.motor
        println("✅ Hardware config loaded:")
        println("   Servo: channel=${servoConfig.channel}, center=${servoConfig.centerAngle}°")
        println("   Motor: channel=${motorConfig.channel}, neutral=${motorConfig.neutralPulseUs}µs")
    }

    /**
     * Update servo calibration values at runtime.
     */
    fun updateServoCalibration(
        minPulseUs: Int? = null,
        maxPulseUs: Int? = null,
        centerAngle: Float? = null,
        leftAngle: Float? = null,
        rightAngle: Float? = null
    ) {
        servoConfig = servoConfig.copy(
            minPulseUs = minPulseUs ?: servoConfig.minPulseUs,
            maxPulseUs = maxPulseUs ?: servoConfig.maxPulseUs,
            centerAngle = centerAngle ?: servoConfig.centerAngle,
            leftAngle = leftAngle ?: servoConfig.leftAngle,
            rightAngle = rightAngle ?: servoConfig.rightAngle
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
        reverseMaxPulseUs: Int? = null
    ) {
        motorConfig = motorConfig.copy(
            minPulseUs = minPulseUs ?: motorConfig.minPulseUs,
            maxPulseUs = maxPulseUs ?: motorConfig.maxPulseUs,
            neutralPulseUs = neutralPulseUs ?: motorConfig.neutralPulseUs,
            forwardMinPulseUs = forwardMinPulseUs ?: motorConfig.forwardMinPulseUs,
            reverseMaxPulseUs = reverseMaxPulseUs ?: motorConfig.reverseMaxPulseUs
        )
        println("🔧 Motor calibration updated: $motorConfig")
    }
}

