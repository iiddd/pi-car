package server

import server.data.config.HardwareConfig
import server.data.config.MotorConfig
import server.data.config.ServoConfig

/**
 * Test utilities for setting up Config before tests run.
 * Since application.yaml is the single source of truth, tests need to
 * explicitly provide configuration.
 */
object TestConfig {

    /**
     * Default test configuration matching the values in application.yaml.
     * Use this for most tests.
     */
    val defaultServoConfig = ServoConfig(
        channel = 0,
        minPulseUs = 1200,
        maxPulseUs = 1800,
        minAngle = 0f,
        maxAngle = 180f,
        centerAngle = 120f,
        leftAngle = 90f,
        rightAngle = 150f
    )

    val defaultMotorConfig = MotorConfig(
        channel = 1,
        minPulseUs = 1300,
        maxPulseUs = 1700,
        neutralPulseUs = 1500,
        forwardMinPulseUs = 1590,
        forwardMaxPulseUs = 1700,
        reverseMaxPulseUs = 1410,
        reverseMinPulseUs = 1300
    )

    /**
     * Initialize Config with default test values.
     * Call this in @BeforeEach or at the start of each test.
     */
    fun initializeTestConfig() {
        Config.loadHardwareConfig(
            HardwareConfig(
                servo = defaultServoConfig,
                motor = defaultMotorConfig
            )
        )
    }

    /**
     * Initialize Config with custom values for specific test scenarios.
     */
    fun initializeTestConfig(
        servoConfig: ServoConfig = defaultServoConfig,
        motorConfig: MotorConfig = defaultMotorConfig
    ) {
        Config.loadHardwareConfig(
            HardwareConfig(
                servo = servoConfig,
                motor = motorConfig
            )
        )
    }
}

