package server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import server.data.config.HardwareConfig
import server.di.mockModule
import server.di.productionModule
import server.domain.ports.MotorController
import server.domain.ports.PwmController
import server.domain.ports.SteeringController
import server.routes.setupCalibrationRoutes
import server.routes.setupDebugRoutes
import server.routes.setupWebSocketRoutes

fun main() {
    println("⚙️ Pi-Car Server Starting...")

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        loadHardwareConfig()
        configureKoin()
        configureCORS()
        configureSerialization()
        configureWebSockets()
        configureRouting()
    }.start(wait = true)
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
 */
fun Application.loadHardwareConfig() {
    try {
        val hardwareConfig = environment.config.config("hardware").let { config ->
            HardwareConfig(
                servo = server.data.config.ServoConfig(
                    channel = config.property("servo.channel").getString().toInt(),
                    minPulseUs = config.property("servo.minPulseUs").getString().toInt(),
                    maxPulseUs = config.property("servo.maxPulseUs").getString().toInt(),
                    minAngle = config.property("servo.minAngle").getString().toFloat(),
                    maxAngle = config.property("servo.maxAngle").getString().toFloat(),
                    centerAngle = config.property("servo.centerAngle").getString().toFloat(),
                    leftAngle = config.property("servo.leftAngle").getString().toFloat(),
                    rightAngle = config.property("servo.rightAngle").getString().toFloat()
                ),
                motor = server.data.config.MotorConfig(
                    channel = config.property("motor.channel").getString().toInt(),
                    minPulseUs = config.property("motor.minPulseUs").getString().toInt(),
                    maxPulseUs = config.property("motor.maxPulseUs").getString().toInt(),
                    neutralPulseUs = config.property("motor.neutralPulseUs").getString().toInt(),
                    forwardMinPulseUs = config.property("motor.forwardMinPulseUs").getString().toInt(),
                    reverseMaxPulseUs = config.property("motor.reverseMaxPulseUs").getString().toInt()
                )
            )
        }
        Config.loadHardwareConfig(hardwareConfig)
    } catch (e: Exception) {
        println("⚠️ Failed to load hardware config from YAML: ${e.message}")
        println("🔧 Using default hardware configuration")
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
    val pwmController: PwmController by inject()
    val steeringController: SteeringController by inject()
    val motorController: MotorController by inject()

    routing {
        // API routes first (more specific)
        setupDebugRoutes()
        setupCalibrationRoutes(pwmController, steeringController, motorController)
        setupWebSocketRoutes(carController)

        // Serve static files from resources/static at root
        // This serves calibration-tool.html at /calibration-tool.html
        staticResources("/", "static")
    }

    println("🚀 Pi-Car Server running on http://0.0.0.0:8080")
    println("🔧 Calibration tool available at http://0.0.0.0:8080/calibration-tool.html")
}
