package server.di

import org.koin.dsl.module
import server.CarController
import server.Config
import server.domain.ports.MotorController
import server.domain.ports.PwmController
import server.domain.ports.SteeringController
import server.hardware.MotorManager
import server.hardware.ServoManager
import server.infrastructure.hardware.MockPwmController
import server.infrastructure.hardware.Pca9685PwmController

/**
 * Common dependencies (Motor, Steering, Car controllers).
 * Defined as a function to be included in both production and mock modules.
 * These depend on PwmController being already defined.
 */
private fun org.koin.core.module.Module.commonDependencies() {
    // Motor Controller - using config values
    single<MotorController> {
        MotorManager(
            pwmController = get(),
            motorChannel = Config.motorConfig.channel,
            minPulseUs = Config.motorConfig.minPulseUs,
            maxPulseUs = Config.motorConfig.maxPulseUs,
            neutralPulseUs = Config.motorConfig.neutralPulseUs
        )
    }

    // Steering Controller - using config values
    single<SteeringController> {
        ServoManager(
            pwmController = get(),
            servoChannel = Config.servoConfig.channel,
            minPulseUs = Config.servoConfig.minPulseUs,
            maxPulseUs = Config.servoConfig.maxPulseUs,
            minAngle = Config.servoConfig.minAngle,
            maxAngle = Config.servoConfig.maxAngle,
            centerAngle = Config.servoConfig.centerAngle,
            leftAngle = Config.servoConfig.leftAngle,
            rightAngle = Config.servoConfig.rightAngle
        )
    }

    // Car Controller - explicit definition to handle default parameter
    single {
        CarController(
            steeringController = get(),
            motorController = get()
            // registerShutdownHook defaults to true
        )
    }
}

/**
 * Koin DI module for production environment.
 * Uses real PCA9685 hardware controller with fallback to mock.
 */
val productionModule = module {
    // PWM Controller - real hardware (MUST be defined first!)
    single<PwmController> {
        try {
            Pca9685PwmController(i2cBus = 1)
        } catch (e: Throwable) {
            // Catch Throwable because diozero throws ServiceConfigurationError (extends Error, not Exception)
            println("⚠️ Failed to initialize PCA9685: ${e.message}")
            println("🧪 Falling back to Mock PWM Controller")
            Config.mockMode = true
            MockPwmController()
        }
    }

    // Common dependencies (depend on PwmController)
    commonDependencies()
}

/**
 * Koin DI module for mock/development environment.
 * Uses mock PWM controller for testing without hardware.
 */
val mockModule = module {
    // PWM Controller - mock (MUST be defined first!)
    single<PwmController> { MockPwmController() }

    // Common dependencies (depend on PwmController)
    commonDependencies()
}

/**
 * Koin DI module for unit testing.
 * All dependencies can be easily mocked.
 */
val testModule = module {
    // In tests, you typically provide mocks via MockK
    // This module is a starting point that can be overridden
    single<PwmController> { MockPwmController() }
}
