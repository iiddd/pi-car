package server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import server.routes.setupDebugRoutes
import server.routes.setupWebSocketRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::mainModule)
        .start(wait = true)
}

fun Application.mainModule() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    install(WebSockets)
    routing {
        setupWebSocketRoutes()
        setupDebugRoutes()
    }
}