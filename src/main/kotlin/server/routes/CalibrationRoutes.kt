package server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.Config
import server.data.calibration.*
import server.infrastructure.hardware.SafePwmController
import server.domain.ports.SteeringController
import server.domain.ports.MotorController

/**
 * Calibration routes for testing and tuning hardware PWM values.
 * These endpoints allow real-time adjustment of servo and motor parameters.
 */
fun Route.setupCalibrationRoutes(
    safePwmController: SafePwmController,
    steeringController: SteeringController,
    motorController: MotorController
) {
    route("/calibration") {

        // Get current calibration values
        get {
            println("📋 GET /calibration - Fetching current calibration values")
            val response = CalibrationResponse(
                servo = ServoCalibration(
                    channel = Config.servoConfig.channel,
                    minPulseUs = Config.servoConfig.minPulseUs,
                    maxPulseUs = Config.servoConfig.maxPulseUs,
                    minAngle = Config.servoConfig.minAngle,
                    maxAngle = Config.servoConfig.maxAngle,
                    centerAngle = Config.servoConfig.centerAngle,
                    leftAngle = Config.servoConfig.leftAngle,
                    rightAngle = Config.servoConfig.rightAngle
                ),
                motor = MotorCalibration(
                    channel = Config.motorConfig.channel,
                    minPulseUs = Config.motorConfig.minPulseUs,
                    maxPulseUs = Config.motorConfig.maxPulseUs,
                    neutralPulseUs = Config.motorConfig.neutralPulseUs,
                    forwardMinPulseUs = Config.motorConfig.forwardMinPulseUs,
                    forwardMaxPulseUs = Config.motorConfig.forwardMaxPulseUs,
                    reverseMaxPulseUs = Config.motorConfig.reverseMaxPulseUs,
                    reverseMinPulseUs = Config.motorConfig.reverseMinPulseUs
                )
            )
            call.respond(response)
        }

        // Set raw PWM pulse (direct hardware control - BYPASSES SAFETY LIMITS)
        // This is intentional for calibration purposes only
        post("/pulse") {
            println("🔧 POST /calibration/pulse - Setting raw PWM pulse (CALIBRATION MODE)")
            try {
                val request = call.receive<SetPulseRequest>()
                println("   Channel: ${request.channel}, Pulse: ${request.pulseUs}µs")

                if (request.channel !in 0..15) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid channel: ${request.channel}")
                    return@post
                }

                if (request.pulseUs !in 500..2500) {
                    call.respond(HttpStatusCode.BadRequest, "Unsafe pulse width: ${request.pulseUs}µs (range: 500-2500)")
                    return@post
                }

                // Use unsafe method to bypass SafePwmController limits for calibration
                safePwmController.unsafeSetDutyUs(request.channel, request.pulseUs)
                call.respondText("✅ Set channel ${request.channel} to ${request.pulseUs}µs", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                println("❌ Error setting pulse: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Test steering at specific angle
        post("/steering/angle") {
            println("🛞 POST /calibration/steering/angle")
            try {
                val request = call.receive<SetSteeringAngleRequest>()
                println("   Setting steering to ${request.angle}°")
                steeringController.setAngle(request.angle)
                call.respondText("✅ Steering set to ${request.angle}°", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                println("❌ Error setting steering angle: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Test motor throttle
        post("/motor/throttle") {
            println("🚀 POST /calibration/motor/throttle")
            try {
                val request = call.receive<SetThrottleRequest>()
                println("   Setting throttle to ${request.throttlePercent * 100}%")
                motorController.setThrottle(request.throttlePercent)
                call.respondText("✅ Throttle set to ${request.throttlePercent * 100}%", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                println("❌ Error setting throttle: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Update servo calibration
        patch("/steering") {
            println("🔧 PATCH /calibration/steering - Updating servo calibration")
            try {
                val request = call.receive<UpdateServoCalibrationRequest>()
                Config.updateServoCalibration(
                    minPulseUs = request.minPulseUs,
                    maxPulseUs = request.maxPulseUs,
                    centerAngle = request.centerAngle,
                    leftAngle = request.leftAngle,
                    rightAngle = request.rightAngle
                )
                call.respondText("✅ Servo calibration updated", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                println("❌ Error updating servo calibration: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Update motor calibration
        patch("/motor") {
            println("🔧 PATCH /calibration/motor - Updating motor calibration")
            try {
                val request = call.receive<UpdateMotorCalibrationRequest>()
                Config.updateMotorCalibration(
                    minPulseUs = request.minPulseUs,
                    maxPulseUs = request.maxPulseUs,
                    neutralPulseUs = request.neutralPulseUs,
                    forwardMinPulseUs = request.forwardMinPulseUs,
                    forwardMaxPulseUs = request.forwardMaxPulseUs,
                    reverseMaxPulseUs = request.reverseMaxPulseUs,
                    reverseMinPulseUs = request.reverseMinPulseUs
                )
                call.respondText("✅ Motor calibration updated", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                println("❌ Error updating motor calibration: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Quick test endpoints
        post("/steering/center") {
            println("🛞 POST /calibration/steering/center")
            steeringController.center()
            call.respondText("✅ Steering centered", status = HttpStatusCode.OK)
        }

        post("/steering/left") {
            println("🛞 POST /calibration/steering/left")
            steeringController.turnLeft()
            call.respondText("✅ Steering turned left", status = HttpStatusCode.OK)
        }

        post("/steering/right") {
            println("🛞 POST /calibration/steering/right")
            steeringController.turnRight()
            call.respondText("✅ Steering turned right", status = HttpStatusCode.OK)
        }

        post("/motor/stop") {
            println("🛑 POST /calibration/motor/stop")
            motorController.stop()
            call.respondText("✅ Motor stopped", status = HttpStatusCode.OK)
        }
    }
}

