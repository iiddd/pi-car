# Pi Car Controller

A remote-controlled car project based on a Raspberry Pi, controlled over WebSockets using [Ktor](https://ktor.io).  
The car uses a PCA9685 PWM driver to control a servo (steering) and a brushless ESC + motor (throttle).

---

## ✅ Features

- WebSocket control interface (Ktor)
- Servo-based steering using Diozero + PCA9685
- Brushless ESC motor throttle control via PWM
- **Hardware calibration API and web UI** for tuning steering and motor parameters
- Configuration management via YAML (application.yaml)
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

## 🚀 Deployment & Running

### Prerequisites
- SSH access to your Raspberry Pi configured (password-less recommended)
- `rsync` installed on your Mac

### Using IntelliJ Run Configurations

The project includes ready-to-use run configurations:

| Configuration | Description |
|---------------|-------------|
| **Deploy to Pi** | Build, deploy, and run on Raspberry Pi |
| **Deploy to Pi (Debug)** | Same as above, with remote debugging enabled |
| **Stop Pi-Car** | Stop the running application on Pi |
| **Pi-Car Status** | Check if app is running and test API |
| **Pi-Car Remote Debug** | Attach debugger to running app |

### Manual Deployment

```bash
# Build and deploy
./scripts/deploy.sh

# Deploy with debug enabled
./scripts/deploy.sh --debug

# Stop the app
./scripts/stop.sh

# Check status
./scripts/status.sh
```

### Remote Debugging (Works without Ultimate!)

1. Run **"Deploy to Pi (Debug)"** to start the app with debug agent
2. Run **"Pi-Car Remote Debug"** to attach the debugger
3. Set breakpoints in your code and debug as usual!

The app will listen for debugger on port 5005.

### Configuration

Edit `scripts/deploy.sh` to change Pi connection settings:
```bash
PI_HOST=# Your Pi SSH address
PI_PATH=# Deployment path
```

---

## 🔧 Hardware Calibration

The project includes a comprehensive calibration system to fine-tune your steering and motor settings.

### Quick Start - Web UI

1. Start the Pi-Car server (use **"Deploy to Pi"**)
2. Open `http://PI_IP:8080/` in your browser
3. The calibration tool loads automatically
4. Use the sliders to test and adjust values
5. Save your calibration settings

### API-Based Calibration

For advanced users, use the REST API directly. See [docs/CALIBRATION.md](./docs/CALIBRATION.md) for detailed instructions.

**Key Endpoints:**
- `GET /calibration` - Get current calibration values
- `POST /calibration/steering/angle` - Test steering at specific angle
- `POST /calibration/motor/throttle` - Test motor throttle
- `PATCH /calibration/steering` - Update steering calibration
- `PATCH /calibration/motor` - Update motor calibration

### Configuration File

Hardware settings are stored in `src/main/resources/application.yaml`:

```yaml
hardware:
  servo:
    channel: 0
    centerAngle: 120.0
    leftAngle: 90.0
    rightAngle: 150.0
  motor:
    channel: 1
    neutralPulseUs: 1500
    forwardMinPulseUs: 1600
    reverseMaxPulseUs: 1400
```

After calibration, update these values and restart the server.

---

## 🧯 Safety Tips

- Always start with **neutralThrottle()** to arm ESC
- Never exceed PSU current limit beyond motor rating
- Disconnect servo power (V+) if unnecessary — some ESCs backfeed voltage
- Monitor PSU for **OCP (Overcurrent Protection)** triggers during testing

---

## 📚 Documentation

Comprehensive documentation is available in the [`docs/`](./docs/) directory:

- **[docs/INDEX.md](./docs/INDEX.md)** - Documentation navigation hub
- **[docs/QUICKSTART_CALIBRATION.md](./docs/QUICKSTART_CALIBRATION.md)** - 15-minute calibration guide
- **[docs/CALIBRATION.md](./docs/CALIBRATION.md)** - Complete API reference
- **[docs/PROJECT_ANALYSIS.md](./docs/PROJECT_ANALYSIS.md)** - Architecture analysis & recommendations

**Calibration Tool:** Access at `http://<pi-ip>:8080/` when server is running.

Start with [docs/INDEX.md](./docs/INDEX.md) for a complete guide to all documentation.
