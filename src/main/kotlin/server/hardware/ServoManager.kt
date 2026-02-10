package server.hardware

import com.diozero.api.ServoTrim
import com.diozero.devices.PCA9685
import com.diozero.devices.ServoController

class ServoManager(
    private val pca9685: PCA9685,
    private val servoChannel: Int = 0
) {
    private val controller = ServoController(pca9685)

    private val e3003Trim = ServoTrim(1000, 2000)

    private val steeringServo = controller.getServo(servoChannel, e3003Trim, 120)

    fun centerSteering() {
        steeringServo.angle = 120f
    }

    fun turnLeft() {
        steeringServo.angle = 90f
    }

    fun turnRight() {
        steeringServo.angle = 150f
    }

    fun setSteeringAngle(angle: Float) {
        steeringServo.angle = angle
    }

    fun shutdown() {
        println("🛑 Shutting down ServoManager (setting servo to neutral)")
        steeringServo.angle = 120f
    }
}