# Hardware Calibration Guide

This guide will help you calibrate the steering servo and motor ESC for your RC car.

## Prerequisites

1. Start the Pi-Car server
2. Have `curl` or any HTTP client ready (Postman, Insomnia, etc.)
3. Ensure the car is safely positioned (wheels off the ground for motor testing)

## Calibration Process

### 1. Get Current Configuration

```bash
curl http://localhost:8080/calibration
```

This shows your current calibration values.

### 2. Steering Calibration

#### Step 2.1: Find the Center Position

Test different angles to find where the servo is perfectly centered:

```bash
# Test center (start with config default, likely 120°)
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 120.0}'

# Adjust if needed
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 115.0}'
```

Once you find the perfect center angle, update the configuration:

```bash
curl -X PATCH http://localhost:8080/calibration/steering \
  -H "Content-Type: application/json" \
  -d '{"centerAngle": 115.0}'
```

#### Step 2.2: Find Left Turn Angle

```bash
# Test left turn
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 90.0}'

# Adjust as needed for maximum comfortable left turn
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 85.0}'
```

Update when satisfied:

```bash
curl -X PATCH http://localhost:8080/calibration/steering \
  -H "Content-Type: application/json" \
  -d '{"leftAngle": 85.0}'
```

#### Step 2.3: Find Right Turn Angle

```bash
# Test right turn
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 150.0}'

# Adjust as needed
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 145.0}'
```

Update when satisfied:

```bash
curl -X PATCH http://localhost:8080/calibration/steering \
  -H "Content-Type: application/json" \
  -d '{"rightAngle": 145.0}'
```

#### Step 2.4: Fine-tune PWM Range (if needed)

If the servo doesn't reach full range or jitters at extremes:

```bash
# Test with raw PWM pulses
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 0, "pulseUs": 1500}'

# Try different values between 1000-2000µs
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 0, "pulseUs": 1100}'

curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 0, "pulseUs": 1900}'
```

Update PWM range if needed:

```bash
curl -X PATCH http://localhost:8080/calibration/steering \
  -H "Content-Type: application/json" \
  -d '{"minPulseUs": 1100, "maxPulseUs": 1900}'
```

### 3. Motor/ESC Calibration

#### Step 3.1: Arm the ESC

First, ensure the ESC is armed with neutral signal:

```bash
curl -X POST http://localhost:8080/calibration/motor/stop
```

Wait 2-3 seconds for the ESC to arm (listen for the beep sequence).

#### Step 3.2: Find Forward Threshold

According to your README, forward starts at 1600µs. Test this:

```bash
# Test neutral (should be 1500µs by default)
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1500}'

# Test forward start (default 1600µs)
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1600}'

# Fine-tune if needed
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1580}'
```

Update the forward minimum:

```bash
curl -X PATCH http://localhost:8080/calibration/motor \
  -H "Content-Type: application/json" \
  -d '{"forwardMinPulseUs": 1580}'
```

#### Step 3.3: Find Reverse Threshold (if applicable)

```bash
# Test reverse (default 1400µs)
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1400}'

# Fine-tune
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1420}'
```

Update if needed:

```bash
curl -X PATCH http://localhost:8080/calibration/motor \
  -H "Content-Type: application/json" \
  -d '{"reverseMaxPulseUs": 1420}'
```

#### Step 3.4: Test Throttle Percentages

```bash
# Test 25% throttle
curl -X POST http://localhost:8080/calibration/motor/throttle \
  -H "Content-Type: application/json" \
  -d '{"throttlePercent": 0.25}'

# Test 50% throttle
curl -X POST http://localhost:8080/calibration/motor/throttle \
  -H "Content-Type: application/json" \
  -d '{"throttlePercent": 0.5}'

# Test 100% throttle (BE CAREFUL!)
curl -X POST http://localhost:8080/calibration/motor/throttle \
  -H "Content-Type: application/json" \
  -d '{"throttlePercent": 1.0}'

# Stop
curl -X POST http://localhost:8080/calibration/motor/stop
```

### 4. Save Your Calibration

Once you're happy with the calibration, update the `application.yaml` file with your final values:

```yaml
hardware:
  servo:
    channel: 0
    minPulseUs: 1100  # Your calibrated value
    maxPulseUs: 1900  # Your calibrated value
    minAngle: 0.0
    maxAngle: 180.0
    centerAngle: 115.0  # Your calibrated value
    leftAngle: 85.0     # Your calibrated value
    rightAngle: 145.0   # Your calibrated value
  
  motor:
    channel: 1
    minPulseUs: 1000
    maxPulseUs: 2000
    neutralPulseUs: 1500
    forwardMinPulseUs: 1580  # Your calibrated value
    reverseMaxPulseUs: 1420  # Your calibrated value
```

Restart the server to load the new configuration.

## Quick Test Commands

### Center everything:
```bash
curl -X POST http://localhost:8080/calibration/steering/center
curl -X POST http://localhost:8080/calibration/motor/stop
```

### Test basic movements:
```bash
# Steering
curl -X POST http://localhost:8080/calibration/steering/left
curl -X POST http://localhost:8080/calibration/steering/center
curl -X POST http://localhost:8080/calibration/steering/right

# Motor (make sure ESC is armed first!)
curl -X POST http://localhost:8080/calibration/motor/stop
# Wait 2-3 seconds
curl -X POST http://localhost:8080/calibration/motor/throttle -H "Content-Type: application/json" -d '{"throttlePercent": 0.3}'
curl -X POST http://localhost:8080/calibration/motor/stop
```

## Safety Tips

⚠️ **IMPORTANT SAFETY WARNINGS:**

1. **Always** have the car elevated or wheels off the ground when testing the motor
2. **Always** arm the ESC (set to neutral and wait 2-3 seconds) before applying throttle
3. Start with **low throttle values** (0.1-0.3) when testing
4. Keep the emergency stop command ready: `curl -X POST http://localhost:8080/calibration/motor/stop`
5. Ensure adequate power supply current (your PSU is set to 9A, which is good)
6. Don't exceed PWM pulse range of 1000-2000µs for standard servos/ESCs

## Troubleshooting

### ESC doesn't arm
- Ensure you're sending exactly 1500µs neutral signal
- Wait at least 2-3 seconds
- Check power supply is delivering 7.4V

### Servo jitters
- Reduce PWM range (e.g., 1100-1900 instead of 1000-2000)
- Check power supply can deliver enough current
- Verify center angle is mechanically comfortable for the servo

### Motor doesn't respond
- Verify ESC is armed (neutral → wait → forward)
- Check that forward threshold is correct (try 1550, 1600, 1650)
- Ensure power supply current limit isn't being hit (OCP)

## Advanced: Direct PWM Control

For precise calibration, you can set exact PWM pulse widths:

```bash
# Servo channel (0) - Test various pulses
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 0, "pulseUs": 1500}'

# Motor channel (1) - Test various pulses
curl -X POST http://localhost:8080/calibration/pulse \
  -H "Content-Type: application/json" \
  -d '{"channel": 1, "pulseUs": 1500}'
```

Valid pulse range: 500-2500µs (but typically use 1000-2000µs for safety)

