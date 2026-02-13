package server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import server.di.mockModule
import server.di.productionModule
import server.routes.setupDebugRoutes
import server.routes.setupWebSocketRoutes
import kotlin.time.Duration.Companion.seconds

fun main() {
    println("⚙️ Pi-Car Server Starting...")

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureKoin()
        configureSerialization()
        configureWebSockets()
        configureRouting()
    }.start(wait = true)
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
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Application.configureRouting() {
    val carController: CarController by inject()

    routing {
        setupDebugRoutes()
        setupWebSocketRoutes(carController)
    }

    println("🚀 Pi-Car Server running on http://0.0.0.0:8080")
}