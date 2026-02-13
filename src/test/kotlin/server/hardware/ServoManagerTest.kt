package server.hardware

import io.mockk.mockk
import io.mockk.verify
import server.domain.ports.PwmController
import kotlin.test.Test
import kotlin.test.assertEquals

class ServoManagerTest {

    private val mockPwmController = mockk<PwmController>(relaxed = true)
    private val servoChannel = 0
    private val servoManager = ServoManager(
        pwmController = mockPwmController,
        servoChannel = servoChannel,
        minPulseUs = 1000,
        maxPulseUs = 2000,
        minAngle = 0f,
        maxAngle = 180f,
        centerAngle = 120f,
        leftAngle = 90f,
        rightAngle = 150f
    )

    @Test
    fun `center should set steering to center angle`() {
        servoManager.center()

        // 120° with 0-180 range: (120/180) * (2000-1000) + 1000 = 1666
        verify { mockPwmController.setDutyUs(servoChannel, 1666) }
    }

    @Test
    fun `turnLeft should set steering to left angle`() {
        servoManager.turnLeft()

        // 90° with 0-180 range: (90/180) * (2000-1000) + 1000 = 1500
        verify { mockPwmController.setDutyUs(servoChannel, 1500) }
    }

    @Test
    fun `turnRight should set steering to right angle`() {
        servoManager.turnRight()

        // 150° with 0-180 range: (150/180) * (2000-1000) + 1000 = 1833
        verify { mockPwmController.setDutyUs(servoChannel, 1833) }
    }

    @Test
    fun `setAngle with 0 should send minimum pulse`() {
        servoManager.setAngle(0f)

        verify { mockPwmController.setDutyUs(servoChannel, 1000) }
    }

    @Test
    fun `setAngle with 180 should send maximum pulse`() {
        servoManager.setAngle(180f)

        verify { mockPwmController.setDutyUs(servoChannel, 2000) }
    }

    @Test
    fun `setAngle with 90 should send middle pulse`() {
        servoManager.setAngle(90f)

        // 90° with 0-180 range: (90/180) * (2000-1000) + 1000 = 1500
        verify { mockPwmController.setDutyUs(servoChannel, 1500) }
    }

    @Test
    fun `setAngle clamps values above max`() {
        servoManager.setAngle(200f)

        verify { mockPwmController.setDutyUs(servoChannel, 2000) }
    }

    @Test
    fun `setAngle clamps values below min`() {
        servoManager.setAngle(-10f)

        verify { mockPwmController.setDutyUs(servoChannel, 1000) }
    }

    @Test
    fun `shutdown should center the steering`() {
        servoManager.shutdown()

        verify { mockPwmController.setDutyUs(servoChannel, 1666) }
    }

    @Test
    fun `angleToPulse returns correct values`() {
        assertEquals(1000, servoManager.angleToPulse(0f))
        assertEquals(2000, servoManager.angleToPulse(180f))
        assertEquals(1500, servoManager.angleToPulse(90f))
        assertEquals(1666, servoManager.angleToPulse(120f))
    }
}
