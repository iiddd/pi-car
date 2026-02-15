# Pi-Car Hardware Setup

Complete hardware reference for the Pi-Car project.

---

## 📋 Bill of Materials

| Component | Model | Notes |
|-----------|-------|-------|
| **SBC** | Raspberry Pi 4 B+ | Main controller |
| **PWM Driver** | PCA9685 | I2C address `0x40`, 16-channel PWM |
| **Steering Servo** | Etronix ES060 | Standard hobby servo |
| **ESC** | Surpass Hobby 25A | Sensorless brushless ESC |
| **Motor** | Surpass Hobby 2430 | Brushless motor |
| **Camera** | Raspberry Pi Camera Module | For FPV video feed |
| **Power Supply** | Wanptek WPS3010H | Lab PSU (or 2S LiPo) |

---

## 🔌 Wiring Diagram

```
Raspberry Pi 4
    │
    ├── I2C (GPIO 2 & 3)
    │       │
    │       └── PCA9685 PWM Controller (0x40)
    │               │
    │               ├── Channel 0 → Steering Servo (signal wire)
    │               │
    │               └── Channel 1 → ESC (signal wire)
    │
    └── Camera Port → Pi Camera Module
    
Power:
    PSU/Battery (7.4V) ──┬── ESC (power input)
                         │       │
                         │       └── Motor (3-phase)
                         │
                         └── BEC (5V) ── Servo (V+)
                               │
                               └── PCA9685 VCC (optional)
```

### I2C Connection (PCA9685)

| Pi GPIO | PCA9685 | Description |
|---------|---------|-------------|
| GPIO 2 (Pin 3) | SDA | I2C Data |
| GPIO 3 (Pin 5) | SCL | I2C Clock |
| 3.3V (Pin 1) | VCC | Logic power |
| GND (Pin 6) | GND | Ground |

### PWM Channels

| Channel | Connected To | Signal Range |
|---------|--------------|--------------|
| 0 | Steering Servo | 1000-2000 µs |
| 1 | ESC (Motor) | 1300-1700 µs |

---

## ⚡ Power Configuration

### Lab Power Supply (Wanptek WPS3010H)

For safe development with adjustable current limiting:

| Setting | Value | Note |
|---------|-------|------|
| **Voltage** | 7.4V | Emulates 2S LiPo battery |
| **Current Limit** | 9A | Lower values may trigger ESC OCP |

### Battery Alternative

For mobile operation, use a **2S LiPo battery** (7.4V nominal):
- Capacity: 2200mAh or higher recommended
- C-rating: 25C minimum
- Always use a LiPo battery monitor/alarm

---

## 🎮 PWM Calibration Values

### Steering Servo (Channel 0)

| Position | Pulse Width (µs) |
|----------|------------------|
| **Left** | 1750 |
| **Center** | 1500 |
| **Right** | 1350 |
| **Min Limit** | 1300 |
| **Max Limit** | 1800 |

### ESC / Motor (Channel 1)

| Signal | Pulse Width (µs) | Description |
|--------|------------------|-------------|
| **Full Reverse** | 1300 | Maximum reverse throttle |
| **Reverse Start** | 1410 | Motor starts moving reverse |
| **Neutral** | 1500 | Motor stopped (arm position) |
| **Forward Start** | 1590 | Motor starts moving forward |
| **Full Forward** | 1700 | Maximum forward throttle |

### Dead Zone

The ESC has a dead zone around neutral where no motor movement occurs:
```
1410 µs ← REVERSE | DEAD ZONE | FORWARD → 1590 µs
              ↑         ↑         ↑
         reverse    1500 µs    forward
          start     neutral     start
```

---

## 📷 Camera Setup (WebRTC)

The Pi-Car uses **MediaMTX** for low-latency WebRTC video streaming.

### Installation

```bash
# Download MediaMTX (check for latest version)
wget https://github.com/bluenviron/mediamtx/releases/download/v1.x.x/mediamtx_v1.x.x_linux_arm64v8.tar.gz
tar -xzf mediamtx_*.tar.gz
sudo mv mediamtx /usr/local/bin/
```

### Configuration

Create/edit `/etc/mediamtx.yml`:

```yaml
logLevel: info

webrtcAddress: :8889
webrtcEncryption: no

paths:
  cam:
    source: rpiCamera
    rpiCameraWidth: 1280
    rpiCameraHeight: 720
    rpiCameraFPS: 60
    rpiCameraBitrate: 4000000
    rpiCameraIDRPeriod: 15
    rpiCameraProfile: baseline
```

### Running the Camera Server

```bash
# Start camera server (requires sudo for camera access)
sudo mediamtx /etc/mediamtx.yml
```

### Accessing the Video Feed

- **Direct access**: `http://<pi-ip>:8889/cam/`
- **Via control page**: Automatically embedded in `http://<pi-ip>:8080/control.html`

### Auto-start on Boot (systemd)

Create `/etc/systemd/system/mediamtx.service`:

```ini
[Unit]
Description=MediaMTX Camera Server
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/mediamtx /etc/mediamtx.yml
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable mediamtx
sudo systemctl start mediamtx
```

---

## 🔧 I2C Setup on Raspberry Pi

### Enable I2C

```bash
sudo raspi-config
# Navigate to: Interface Options → I2C → Enable
```

### Verify I2C Devices

```bash
# Install i2c-tools if needed
sudo apt install i2c-tools

# Scan for devices (PCA9685 should appear at 0x40)
sudo i2cdetect -y 1
```

Expected output:
```
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- -- 
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
40: 40 -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
70: -- -- -- -- -- -- -- --
```

### I2C Troubleshooting

If you see I2C errors like `RuntimeIOException: Error calling pigpioImpl.i2cWriteByteData(), response: -82`:

**Common Causes:**
1. **Electrical noise** - Motor/ESC switching can interfere with I2C signals
2. **Loose connections** - Check all I2C wires (SDA, SCL, GND)
3. **Long wires** - Keep I2C wires short (< 30cm ideally)
4. **Power supply noise** - High current draw can cause voltage drops

**Solutions:**
1. **Add pull-up resistors** - 4.7kΩ on SDA and SCL to 3.3V (some boards have these built-in)
2. **Use shielded cables** - For I2C lines near motor wires
3. **Separate power** - Use dedicated 5V for PCA9685 logic
4. **Add capacitors** - 100µF on ESC power input to reduce noise
5. **Reduce I2C speed** - If persistent, try 100kHz instead of 400kHz

**Check I2C bus health:**
```bash
# Check for I2C errors in kernel log
dmesg | grep -i i2c

# Re-scan after error
sudo i2cdetect -y 1
```

The Pi-Car software includes automatic retry logic for transient I2C errors.

---

## 🚀 ESC Arming Procedure

The ESC must be "armed" before it will respond to throttle commands:

1. **Power on** the ESC/motor power supply
2. **Send neutral signal** (1500 µs) to the ESC
3. **Wait 2-3 seconds** for arming beeps
4. ESC is now ready to accept throttle commands

The calibration tool automates this with the "Arm ESC" button.

---

## ⚠️ Safety Considerations

### Power

- Always set PSU current limit before testing
- Start with low current limit (2A) and increase as needed
- Use a fuse or circuit breaker for battery-powered operation
- Never exceed motor rated voltage

### ESC

- Always arm with neutral before applying throttle
- Never apply full throttle immediately
- Disconnect motor when calibrating ESC parameters
- Some ESCs backfeed voltage - disconnect servo V+ if issues occur

### Servo

- Never exceed servo torque limits
- Avoid mechanical binding (can burn out servo)
- Set software limits within mechanical range

### General

- Keep wheels off ground during calibration
- Have a way to quickly cut power (switch or PSU button)
- Test failsafe behavior before mobile operation

---

## 🔗 Related Documentation

- [CALIBRATION.md](./CALIBRATION.md) - Software calibration procedures
- [QUICKSTART_CALIBRATION.md](./QUICKSTART_CALIBRATION.md) - 15-minute calibration guide
- [SECURITY.md](./SECURITY.md) - Secure deployment setup

