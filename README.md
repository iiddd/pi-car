# Pi Car Controller

A remote-controlled car project based on a Raspberry Pi, controlled over WebSockets using [Ktor](https://ktor.io).  
The car uses a PCA9685 PWM driver to control a servo (steering) and a brushless ESC + motor (throttle).

---

## ✅ Features

- **WebRTC video streaming** for FPV control
- **GTA-style remote control** with keyboard/touch controls and 50Hz control loop
- WebSocket control interface (Ktor)
- Servo-based steering using Diozero + PCA9685
- Brushless ESC motor throttle control via PWM
- **Hardware calibration API and web UI** for tuning steering and motor parameters
- Configuration management via YAML (application.yaml)
- Modular hardware abstraction (`ServoManager`, `MotorManager`, `CarController`)
- **Safety features**: Deadman switch, 250ms failsafe timeout

---

## 🧠 Quick Overview

| Component | Model |
|-----------|-------|
| SBC | Raspberry Pi 4 B+ |
| PWM Driver | PCA9685 (I2C, address `0x40`) |
| Steering | Etronix ES060 servo |
| Motor | Surpass Hobby 2430 Brushless |
| ESC | Surpass Hobby 25A |
| Camera | Raspberry Pi Camera Module (WebRTC via MediaMTX) |

For complete hardware details, wiring diagrams, and setup instructions, see **[docs/HARDWARE.md](./docs/HARDWARE.md)**.

---

## 🚀 Quick Start

1. **Deploy to Pi**: Use the IntelliJ run configuration or `./scripts/deploy.sh`
2. **Start camera**: `sudo mediamtx /etc/mediamtx.yml` (on Pi)
3. **Open control page**: `http://<pi-ip>:8080/`
4. **Drive!** Hold SPACE (deadman) and use WASD to control

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
    minPulseUs: 1300
    maxPulseUs: 1800
    centerPulseUs: 1500
    leftPulseUs: 1750
    rightPulseUs: 1350
  motor:
    channel: 1
    neutralPulseUs: 1500
    forwardMinPulseUs: 1590
    forwardMaxPulseUs: 1700
    reverseMaxPulseUs: 1410
    reverseMinPulseUs: 1300
```

Use the calibration tool to find optimal values, then update this file.

---

## 🧯 Safety Tips

- Always start with **neutralThrottle()** to arm ESC
- Never exceed PSU current limit beyond motor rating
- Disconnect servo power (V+) if unnecessary — some ESCs backfeed voltage
- Monitor PSU for **OCP (Overcurrent Protection)** triggers during testing

---

## 📚 Documentation

- **[docs/HARDWARE.md](./docs/HARDWARE.md)** - Complete hardware setup, wiring, and camera configuration
- **[docs/CALIBRATION.md](./docs/CALIBRATION.md)** - Complete calibration API reference
- **[docs/QUICKSTART_CALIBRATION.md](./docs/QUICKSTART_CALIBRATION.md)** - 15-minute calibration quick start
- **[docs/SECURITY.md](./docs/SECURITY.md)** - Security setup guide (credentials & SSH keys)

**Web UI:** Open your browser to `http://<pi-ip>:8080/` when the server is running.
