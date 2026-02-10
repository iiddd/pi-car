# Pi Car Controller

A remote-controlled car project based on a Raspberry Pi, controlled over WebSockets using [Ktor](https://ktor.io).  
The car uses a PCA9685 PWM driver to control a servo (steering) and a brushless ESC + motor (throttle).

---

## ✅ Features

- WebSocket control interface (Ktor)
- Servo-based steering using Diozero + PCA9685
- Brushless ESC motor throttle control via PWM
- Modular hardware abstraction (`ServoManager`, `MotorManager`, `CarController`)
- Kotlin-based server-side logic with clean project structure
- Lab power supply support for safe development

---

## 🧠 Project Recap

> Last tested: **February 2026**

### Core Components

| Component           | Details                                                                 |
|--------------------|-------------------------------------------------------------------------|
| SBC                | Raspberry Pi 4 B+                                                       |
| PWM Driver         | PCA9685 over I2C (default address `0x40`)                               |
| Servo              | Etronix ES060                                                           |
| ESC                | Surpass Hobby 25A sensorless brushless ESC (powered separately)         |
| Motor              | Surpass Hobby 2430 Brushless                                            |
| Power Supply       | Wanptek WPS3010H (lab PSU with adjustable current limit)                |

### Architecture
main()
├─ PCA9685 (1 instance)
├─ ServoManager (channel 0) → steeringServo
└─ MotorManager (channel 1) → ESC throttle

### ✅ Current Progress

- ✅ Steering fully functional with ServoManager (angle tested)
- ✅ ESC control verified (requires 1500 µs neutral → 1600 µs forward)
- ✅ Diozero PCA9685 driver working correctly
- ✅ Proper power sequencing implemented (ESC arming requires ~2 sec neutral)
- ✅ PSU current limit set to avoid OCP triggers during motor spin-up

---

## ⚙️ Lab PSU Configuration (Wanptek WPS3010H)

| Setting        | Value              | Note                                                |
|----------------|--------------------|-----------------------------------------------------|
| **Voltage**    | `7.4V`             | Emulates 2S LiPo battery                            |
| **Current**    | `9A`               | Lower values (e.g. 2A) may cause ESC shutdown (OCP) |

---

## 🔌 PWM Timing Settings (PCA9685 @ 50Hz)

| Signal Type     | Microseconds | Notes                                      |
|-----------------|--------------|--------------------------------------------|
| **Neutral**     | `1500 µs`    | Required to arm ESC (wait ~2 seconds)      |
| **Forward**     | `1600 µs`    | ESC starts spinning the motor forward      |
| **Reverse**     | `1400 µs`    | Reverse spin (optional, depending on ESC)  |

---

## 🚀 How to Run

1. Power the ESC with 7.4V via Deans (T-plug).
2. Boot the Raspberry Pi and run the Kotlin server (`main()`).
3. Servo and ESC will be initialized.
4. The ESC **must receive a neutral (1500 µs)** signal and **wait 2 seconds** to arm.
5. Use `CarController.forwardThrottle()` to test forward motion.

---

## 🧯 Safety Tips

- Always start with **neutralThrottle()** to arm ESC
- Never exceed PSU current limit beyond motor rating
- Disconnect servo power (V+) if unnecessary — some ESCs backfeed voltage
- Monitor PSU for **OCP (Overcurrent Protection)** triggers during testing