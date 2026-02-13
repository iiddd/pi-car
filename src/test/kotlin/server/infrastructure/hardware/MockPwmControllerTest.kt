package server.infrastructure.hardware

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MockPwmControllerTest {

    @Test
    fun `setDutyUs stores the value for the channel`() {
        val controller = MockPwmController()

        controller.setDutyUs(0, 1500)

        assertEquals(1500, controller.getDutyUs(0))
    }

    @Test
    fun `setDutyUs stores multiple channels independently`() {
        val controller = MockPwmController()

        controller.setDutyUs(0, 1500)
        controller.setDutyUs(1, 1750)
        controller.setDutyUs(2, 1250)

        assertEquals(1500, controller.getDutyUs(0))
        assertEquals(1750, controller.getDutyUs(1))
        assertEquals(1250, controller.getDutyUs(2))
    }

    @Test
    fun `getDutyUs returns null for unset channels`() {
        val controller = MockPwmController()

        assertNull(controller.getDutyUs(5))
    }

    @Test
    fun `getAllStates returns all channel values`() {
        val controller = MockPwmController()

        controller.setDutyUs(0, 1500)
        controller.setDutyUs(1, 1750)

        val states = controller.getAllStates()
        assertEquals(2, states.size)
        assertEquals(1500, states[0])
        assertEquals(1750, states[1])
    }

    @Test
    fun `close clears all channel states`() {
        val controller = MockPwmController()

        controller.setDutyUs(0, 1500)
        controller.setDutyUs(1, 1750)
        controller.close()

        assertEquals(emptyMap(), controller.getAllStates())
    }
}
