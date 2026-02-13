package server.hardware

import io.mockk.mockk
import io.mockk.verify
import server.domain.ports.PwmController
import kotlin.test.Test
import kotlin.test.assertEquals

class MotorManagerTest {

    private val mockPwmController = mockk<PwmController>(relaxed = true)
    private val motorChannel = 1
    private val motorManager = MotorManager(
        pwmController = mockPwmController,
        motorChannel = motorChannel,
        minPulseUs = 1000,
        maxPulseUs = 2000,
        neutralPulseUs = 1500
    )

    @Test
    fun `setThrottle with 0 should send neutral pulse`() {
        motorManager.setThrottle(0f)

        verify { mockPwmController.setDutyUs(motorChannel, 1500) }
    }

    @Test
    fun `setThrottle with 1 should send max forward pulse`() {
        motorManager.setThrottle(1.0f)

        verify { mockPwmController.setDutyUs(motorChannel, 2000) }
    }

    @Test
    fun `setThrottle with -1 should send max reverse pulse`() {
        motorManager.setThrottle(-1.0f)

        verify { mockPwmController.setDutyUs(motorChannel, 1000) }
    }

    @Test
    fun `setThrottle with 0_5 should send half forward pulse`() {
        motorManager.setThrottle(0.5f)

        // neutral (1500) + 0.5 * (2000 - 1500) = 1500 + 250 = 1750
        verify { mockPwmController.setDutyUs(motorChannel, 1750) }
    }

    @Test
    fun `setThrottle with -0_5 should send half reverse pulse`() {
        motorManager.setThrottle(-0.5f)

        // neutral (1500) + (-0.5) * (1500 - 1000) = 1500 - 250 = 1250
        verify { mockPwmController.setDutyUs(motorChannel, 1250) }
    }

    @Test
    fun `setThrottle clamps values above 1`() {
        motorManager.setThrottle(1.5f)

        verify { mockPwmController.setDutyUs(motorChannel, 2000) }
    }

    @Test
    fun `setThrottle clamps values below -1`() {
        motorManager.setThrottle(-1.5f)

        verify { mockPwmController.setDutyUs(motorChannel, 1000) }
    }

    @Test
    fun `stop should send neutral pulse`() {
        motorManager.stop()

        verify { mockPwmController.setDutyUs(motorChannel, 1500) }
    }

    @Test
    fun `calculatePulse returns correct values`() {
        assertEquals(1500, motorManager.calculatePulse(0f))
        assertEquals(2000, motorManager.calculatePulse(1f))
        assertEquals(1000, motorManager.calculatePulse(-1f))
        assertEquals(1750, motorManager.calculatePulse(0.5f))
        assertEquals(1250, motorManager.calculatePulse(-0.5f))
    }
}
