package server.routes

import server.Config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import server.data.debug.MockModeRequest
import server.data.status.StatusResponse

fun Route.setupDebugRoutes() {
    // 🔍 Status endpoint
    get("/status") {
        println("⚙️ /status GET received")
        val response = StatusResponse(
            status = "ready",
            mockMode = Config.mockMode
        )
        println("🔍 /status → $response")
        call.respond(response)
    }

    // ⚙️ Toggle mock mode
    post("/mock-mode") {
        println("⚙️ /mock-mode POST received")
        try {
            val request = call.receive<MockModeRequest>()
            Config.mockMode = request.mockMode
            println("✅ Mock mode updated → ${request.mockMode}")
            call.respondText("Mock mode set to ${request.mockMode}")
        } catch (_: SerializationException) {
            println("❌ Invalid mock mode request body")
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }
    }
}