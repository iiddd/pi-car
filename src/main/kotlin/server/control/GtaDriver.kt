package server.control

import kotlin.math.abs
import kotlin.math.sign

/**
 * GTA-style driver logic.
 * Maintains normalized speed [-1..1] and steer [-1..1] values.
 *
 * Note: Reverse is limited to low speed for maneuvering only.
 */
class GtaDriver(
    // Throttle rates (per second)
    private val accelRate: Float = 0.8f,
    private val coastRate: Float = 1.2f,
    private val brakeRate: Float = 3.0f,
    private val reverseAccelRate: Float = 0.3f,   // Slow reverse acceleration (maneuvering only)
    // Steering rates (per second)
    private val steerRate: Float = 3.0f,
    private val steerReturnRate: Float = 5.0f,
    // Thresholds
    private val stopThreshold: Float = 0.05f,
    private val reverseThreshold: Float = 0.1f,
    // Reverse speed limit (0.3 = 30% of max, for maneuvering)
    private val maxReverseSpeed: Float = 0.3f
) {
    var speed: Float = 0f
        private set
    var steer: Float = 0f
        private set

    private var wasBrakingFromForward = false

    fun tick(deltaSeconds: Float, input: ControlInput?, deadmanActive: Boolean) {
        if (input == null || !deadmanActive) {
            coastToNeutral(deltaSeconds)
            return
        }

        if (input.throttle != null || input.steer != null) {
            handleAnalogInput(deltaSeconds, input)
            return
        }

        handleDigitalInput(deltaSeconds, input)
    }

    private fun handleAnalogInput(deltaSeconds: Float, input: ControlInput) {
        // Limit reverse target to maxReverseSpeed (maneuvering only)
        val targetThrottle = (input.throttle ?: 0f).coerceIn(-maxReverseSpeed, 1f)
        val targetSteer = input.steer ?: 0f

        val throttleDiff = targetThrottle - speed
        val maxThrottleChange = accelRate * deltaSeconds
        speed += throttleDiff.coerceIn(-maxThrottleChange * 2, maxThrottleChange * 2)
        speed = speed.coerceIn(-maxReverseSpeed, 1f)

        val steerDiff = targetSteer - steer
        val maxSteerChange = steerRate * deltaSeconds
        steer += steerDiff.coerceIn(-maxSteerChange * 2, maxSteerChange * 2)
        steer = steer.coerceIn(-1f, 1f)
    }

    private fun handleDigitalInput(deltaSeconds: Float, input: ControlInput) {
        updateThrottle(deltaSeconds, input.forward, input.backward)
        updateSteering(deltaSeconds, input.left, input.right)
    }

    private fun updateThrottle(deltaSeconds: Float, forward: Boolean, backward: Boolean) {
        when {
            forward && !backward -> {
                wasBrakingFromForward = false
                speed += accelRate * deltaSeconds
                speed = speed.coerceAtMost(1f)
            }
            backward && !forward -> {
                when {
                    speed > reverseThreshold -> {
                        wasBrakingFromForward = true
                        speed -= brakeRate * deltaSeconds
                        speed = speed.coerceAtLeast(0f)
                    }
                    speed <= reverseThreshold && !wasBrakingFromForward -> {
                        // Reverse acceleration - limited to maxReverseSpeed for maneuvering
                        speed -= reverseAccelRate * deltaSeconds
                        speed = speed.coerceAtLeast(-maxReverseSpeed)
                    }
                    wasBrakingFromForward && speed <= stopThreshold -> {
                        wasBrakingFromForward = false
                        speed = 0f
                    }
                    wasBrakingFromForward -> {
                        speed -= brakeRate * deltaSeconds
                        speed = speed.coerceAtLeast(0f)
                    }
                }
            }
            else -> {
                wasBrakingFromForward = false
                coastSpeed(deltaSeconds)
            }
        }
    }

    private fun updateSteering(deltaSeconds: Float, left: Boolean, right: Boolean) {
        when {
            left && !right -> {
                steer -= steerRate * deltaSeconds
                steer = steer.coerceAtLeast(-1f)
            }
            right && !left -> {
                steer += steerRate * deltaSeconds
                steer = steer.coerceAtMost(1f)
            }
            else -> returnSteerToCenter(deltaSeconds)
        }
    }

    private fun coastSpeed(deltaSeconds: Float) {
        if (abs(speed) < stopThreshold) {
            speed = 0f
        } else {
            speed -= sign(speed) * coastRate * deltaSeconds
            if (abs(speed) < stopThreshold) speed = 0f
        }
    }

    private fun returnSteerToCenter(deltaSeconds: Float) {
        if (abs(steer) < stopThreshold) {
            steer = 0f
        } else {
            steer -= sign(steer) * steerReturnRate * deltaSeconds
            if (abs(steer) < stopThreshold) steer = 0f
        }
    }

    private fun coastToNeutral(deltaSeconds: Float) {
        if (abs(speed) > stopThreshold) {
            speed -= sign(speed) * brakeRate * deltaSeconds
            if (abs(speed) < stopThreshold) speed = 0f
        } else {
            speed = 0f
        }
        returnSteerToCenter(deltaSeconds)
    }

    fun reset() {
        speed = 0f
        steer = 0f
        wasBrakingFromForward = false
    }
}

