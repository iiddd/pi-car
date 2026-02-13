package server.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
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
 * Koin DI module for production environment.
 * Uses real PCA9685 hardware controller.
 */
val productionModule = module {
    // PWM Controller - real hardware
    single<PwmController> {
        try {
            Pca9685PwmController(i2cBus = 1)
        } catch (e: Exception) {
            println("⚠️ Failed to initialize PCA9685: ${e.message}")
            println("🧪 Falling back to Mock PWM Controller")
            Config.mockMode = true
            MockPwmController()
        }
    }

    // Motor Controller
    single<MotorController> {
        MotorManager(
            pwmController = get(),
            motorChannel = 1
        )
    }

    // Steering Controller
    single<SteeringController> {
        ServoManager(
            pwmController = get(),
            servoChannel = 0
        )
    }

    // Car Controller
    singleOf(::CarController)
}

/**
 * Koin DI module for mock/development environment.
 * Uses mock PWM controller for testing without hardware.
 */
val mockModule = module {
    // PWM Controller - mock
    single<PwmController> { MockPwmController() }

    // Motor Controller
    single<MotorController> {
        MotorManager(
            pwmController = get(),
            motorChannel = 1
        )
    }

    // Steering Controller
    single<SteeringController> {
        ServoManager(
            pwmController = get(),
            servoChannel = 0
        )
    }

    // Car Controller
    singleOf(::CarController)
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
