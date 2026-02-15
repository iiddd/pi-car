package server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import server.control.ControlLoop
import server.control.setupControlRoutes
import server.data.config.HardwareConfig
import server.di.mockModule
import server.di.productionModule
import server.domain.ports.MotorController
import server.domain.ports.SteeringController
import server.infrastructure.hardware.SafePwmController
import server.routes.setupCalibrationRoutes
import server.routes.setupDebugRoutes
import server.routes.setupWebSocketRoutes

fun main() {
    println("⚙️ Pi-Car Server Starting...")

    // Load YAML config explicitly
    val yamlConfig = io.ktor.server.config.yaml.YamlConfig("application.yaml")
    if (yamlConfig == null) {
        System.err.println("❌ FATAL: Could not find application.yaml!")
        System.err.println("   Ensure application.yaml exists in src/main/resources/")
        throw IllegalStateException("application.yaml not found")
    }

    embeddedServer(
        Netty,
        environment = applicationEngineEnvironment {
            config = yamlConfig
            connector {
                port = 8080
                host = "0.0.0.0"
            }
            module {
                loadHardwareConfig()
                configureKoin()
                configureCORS()
                configureSerialization()
                configureWebSockets()
                configureRouting()
            }
        }
    ).start(wait = true)
}

/**
 * Configure CORS to allow requests from any origin (for calibration tool access)
 */
fun Application.configureCORS() {
    install(CORS) {
        anyHost() // Allow requests from any origin
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
    println("✅ CORS enabled for all origins")
}

/**
 * Load hardware configuration from application.yaml
 *
 * FAIL FAST: The application will terminate if config cannot be loaded.
 * All hardware parameters MUST be defined in application.yaml.
 */
fun Application.loadHardwareConfig() {
    try {
        val hardwareConfig = environment.config.config("hardware")
        val servoConfig = hardwareConfig.config("servo")
        val motorConfig = hardwareConfig.config("motor")

        val config = HardwareConfig(
            servo = server.data.config.ServoConfig(
                channel = servoConfig.property("channel").getString().toInt(),
                minPulseUs = servoConfig.property("minPulseUs").getString().toInt(),
                maxPulseUs = servoConfig.property("maxPulseUs").getString().toInt(),
                centerPulseUs = servoConfig.property("centerPulseUs").getString().toInt(),
                leftPulseUs = servoConfig.property("leftPulseUs").getString().toInt(),
                rightPulseUs = servoConfig.property("rightPulseUs").getString().toInt()
            ),
            motor = server.data.config.MotorConfig(
                channel = motorConfig.property("channel").getString().toInt(),
                minPulseUs = motorConfig.property("minPulseUs").getString().toInt(),
                maxPulseUs = motorConfig.property("maxPulseUs").getString().toInt(),
                neutralPulseUs = motorConfig.property("neutralPulseUs").getString().toInt(),
                forwardMinPulseUs = motorConfig.property("forwardMinPulseUs").getString().toInt(),
                forwardMaxPulseUs = motorConfig.property("forwardMaxPulseUs").getString().toInt(),
                reverseMaxPulseUs = motorConfig.property("reverseMaxPulseUs").getString().toInt(),
                reverseMinPulseUs = motorConfig.property("reverseMinPulseUs").getString().toInt()
            )
        )
        Config.loadHardwareConfig(config)
    } catch (e: Exception) {
        System.err.println("❌ FATAL: Failed to load hardware config from application.yaml!")
        System.err.println("   Error: ${e.message}")
        System.err.println("")
        System.err.println("   All hardware configuration MUST be defined in application.yaml.")
        System.err.println("   Please ensure the file exists and contains all required properties.")
        System.err.println("")
        throw IllegalStateException("Cannot start without valid hardware configuration", e)
    }
}

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        // Use mock module if explicitly set, otherwise try production
        modules(if (Config.mockMode) mockModule else productionModule)
    }
    println("✅ Koin DI initialized (mockMode: ${Config.mockMode})")
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = java.time.Duration.ofSeconds(15)
        timeout = java.time.Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Application.configureRouting() {
    val carController: CarController by inject()
    val safePwmController: SafePwmController by inject()
    val steeringController: SteeringController by inject()
    val motorController: MotorController by inject()
    val controlLoop: ControlLoop by inject()

    // Start the 50Hz control loop
    val controlScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    controlLoop.start(controlScope)

    // Stop control loop on shutdown
    environment.monitor.subscribe(ApplicationStopped) {
        controlLoop.stop()
        controlScope.cancel()
    }

    routing {
        // API routes first (more specific)
        setupDebugRoutes()
        setupCalibrationRoutes(safePwmController, steeringController, motorController)
        setupWebSocketRoutes(carController)
        setupControlRoutes(controlLoop)

        // Serve static files from resources/static at root
        // This serves index.html, control.html, calibration-tool.html
        staticResources("/", "static")
    }

    println("🚀 Pi-Car Server running on http://0.0.0.0:8080")
    println("🎮 Remote control at http://0.0.0.0:8080/control.html")
    println("🔧 Calibration tool at http://0.0.0.0:8080/calibration-tool.html")
}
