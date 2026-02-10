package server.routes

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import server.CarController


fun Route.setupWebSocketRoutes() {
//    webSocket("/control") {
//        println("Client connected")
//        incoming.consumeEach { frame ->
//            if (frame is Frame.Text) {
//                val command = frame.readText()
//                CarController.handleCommand(command)
//            }
//        }
//    }
}