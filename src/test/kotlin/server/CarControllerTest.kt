package server

import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import server.domain.ports.MotorController
import server.domain.ports.SteeringController
import kotlin.test.Test

class CarControllerTest {

    private val mockSteeringController = mockk<SteeringController>(relaxed = true)
    private val mockMotorController = mockk<MotorController>(relaxed = true)

    private val carController = CarController(
        steeringController = mockSteeringController,
        motorController = mockMotorController,
        registerShutdownHook = false
    )

    @Test
    fun `centerSteering should call steering controller center`() {
        carController.centerSteering()

        verify { mockSteeringController.center() }
    }

    @Test
    fun `steerLeft should call steering controller turnLeft`() {
        carController.steerLeft()

        verify { mockSteeringController.turnLeft() }
    }

    @Test
    fun `steerRight should call steering controller turnRight`() {
        carController.steerRight()

        verify { mockSteeringController.turnRight() }
    }

    @Test
    fun `setSteeringAngle should call steering controller setAngle`() {
        carController.setSteeringAngle(45f)

        verify { mockSteeringController.setAngle(45f) }
    }

    @Test
    fun `setThrottlePercent should call motor controller setThrottle`() {
        carController.setThrottlePercent(0.5f)

        verify { mockMotorController.setThrottle(0.5f) }
    }

    @Test
    fun `neutralThrottle should call motor controller stop`() {
        carController.neutralThrottle()

        verify { mockMotorController.stop() }
    }

    @Test
    fun `forwardThrottle should set throttle to 0_3`() {
        carController.forwardThrottle()

        verify { mockMotorController.setThrottle(0.3f) }
    }

    @Test
    fun `reverseThrottle should set throttle to -0_3`() {
        carController.reverseThrottle()

        verify { mockMotorController.setThrottle(-0.3f) }
    }

    @Test
    fun `shutdown should stop motor and shutdown steering`() {
        carController.shutdown()

        verifySequence {
            mockMotorController.stop()
            mockSteeringController.shutdown()
        }
    }
}
