package server.di

import org.koin.dsl.module
import server.CarController
import server.Config
import server.control.ControlLoop
import server.control.MockPwmOutput
import server.control.PwmOutput
import server.control.RealPwmOutput
import server.domain.ports.MotorController
import server.domain.ports.PwmController
import server.domain.ports.SteeringController
import server.hardware.MotorManager
import server.hardware.ServoManager
import server.infrastructure.hardware.MockPwmController
import server.infrastructure.hardware.Pca9685PwmController
import server.infrastructure.hardware.SafePwmController

/**
 * Creates a SafePwmController with channel limits from Config.
 */
private fun createSafePwmController(delegate: PwmController): SafePwmController {
    val channelLimits = mapOf(
        Config.servoConfig.channel to SafePwmController.ChannelLimits(
            minPulseUs = Config.servoConfig.minPulseUs,
            maxPulseUs = Config.servoConfig.maxPulseUs,
            name = "Servo"
        ),
        Config.motorConfig.channel to SafePwmController.ChannelLimits(
            minPulseUs = Config.motorConfig.minPulseUs,
            maxPulseUs = Config.motorConfig.maxPulseUs,
            name = "Motor"
        )
    )
    println("🔒 SafePwmController initialized with limits:")
    println("   Servo (ch${Config.servoConfig.channel}): ${Config.servoConfig.minPulseUs}-${Config.servoConfig.maxPulseUs} µs")
    println("   Motor (ch${Config.motorConfig.channel}): ${Config.motorConfig.minPulseUs}-${Config.motorConfig.maxPulseUs} µs")
    return SafePwmController(delegate, channelLimits)
}

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
            centerPulseUs = Config.servoConfig.centerPulseUs,
            leftPulseUs = Config.servoConfig.leftPulseUs,
            rightPulseUs = Config.servoConfig.rightPulseUs
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

    // Control Loop for GTA-style driving
    single { ControlLoop(pwmOutput = get()) }
}

/**
 * Koin DI module for production environment.
 * Uses real PCA9685 hardware controller with fallback to mock.
 */
val productionModule = module {
    // Raw PWM Controller - real hardware with fallback
    single<PwmController> {
        val rawController = try {
            Pca9685PwmController(i2cBus = 1)
        } catch (e: Throwable) {
            // Catch Throwable because diozero throws ServiceConfigurationError (extends Error, not Exception)
            println("⚠️ Failed to initialize PCA9685: ${e.message}")
            println("🧪 Falling back to Mock PWM Controller")
            Config.mockMode = true
            MockPwmController()
        }
        // Wrap with SafePwmController for bounds checking
        createSafePwmController(rawController)
    }

    // Also provide SafePwmController directly for calibration endpoints
    single<SafePwmController> { get<PwmController>() as SafePwmController }

    // PwmOutput for control loop - uses SafePwmController
    single<PwmOutput> {
        if (Config.mockMode) {
            MockPwmOutput()
        } else {
            RealPwmOutput(get())
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
    // PWM Controller - mock wrapped with SafePwmController
    single<PwmController> { createSafePwmController(MockPwmController()) }

    // Also provide SafePwmController directly for calibration endpoints
    single<SafePwmController> { get<PwmController>() as SafePwmController }

    // PwmOutput for control loop - mock
    single<PwmOutput> { MockPwmOutput() }

    // Common dependencies (depend on PwmController)
    commonDependencies()
}

/**
 * Koin DI module for unit testing.
 * All dependencies can be easily mocked.
 */
val testModule = module {
    // In tests, we also enforce safety limits
    single<PwmController> { createSafePwmController(MockPwmController()) }
    single<SafePwmController> { get<PwmController>() as SafePwmController }
}
