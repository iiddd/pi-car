# Calibration System - Implementation Summary

## What Was Implemented

I've implemented a comprehensive hardware calibration system for your Pi-Car project. Here's what you now have:

### 1. Configuration Management (YAML-based)

**File:** `src/main/resources/application.yaml`

All hardware parameters are now stored in YAML configuration:
- Servo settings (angles, PWM range)
- Motor/ESC settings (neutral, forward/reverse thresholds)
- Easy to modify without code changes

### 2. Calibration REST API

**New Routes:** `/calibration/*`

**Key Endpoints:**
- `GET /calibration` - Get current calibration values
- `POST /calibration/pulse` - Set raw PWM pulse (direct hardware control)
- `POST /calibration/steering/angle` - Test steering at specific angle
- `POST /calibration/motor/throttle` - Test motor throttle
- `PATCH /calibration/steering` - Update steering calibration at runtime
- `PATCH /calibration/motor` - Update motor calibration at runtime
- Quick test endpoints: `/steering/center`, `/steering/left`, `/steering/right`, `/motor/stop`

### 3. Web-Based Calibration Tool

**Served from:** `http://<pi-ip>:8080/calibration-tool.html`

A beautiful, user-friendly web interface with:
- Real-time steering angle adjustment with slider
- Throttle control with visual feedback
- Quick action buttons (Center, Left, Right, ARM ESC, STOP)
- Direct PWM control for advanced tuning
- Save calibration settings
- Status indicators for all operations

**How to use:**
1. Start your Pi-Car server
2. Open `http://<pi-ip>:8080/` in any browser
3. Click "Load Current Config"
4. Use sliders and buttons to test
5. Save your preferred values

### 4. Comprehensive Documentation

**Files:**
- `docs/CALIBRATION.md` - Detailed step-by-step calibration guide with curl examples
- `README.md` - Updated with calibration section

### 5. Runtime Configuration Updates

The system supports:
- Hot-reload of calibration values (no restart needed for testing)
- Config object that can be updated via API
- Final values saved to YAML for persistence

## Architecture Changes

### New Files Created:
```
src/main/kotlin/server/
├── data/
│   ├── config/
│   │   └── HardwareConfig.kt          # YAML config data classes
│   └── calibration/
│       └── CalibrationData.kt         # API request/response models
├── routes/
│   └── CalibrationRoutes.kt           # Calibration API endpoints
└── Config.kt                           # Enhanced with hardware config

docs/
├── calibration-tool.html              # Web UI for calibration
├── CALIBRATION.md                     # Detailed guide
├── QUICKSTART_CALIBRATION.md          # Quick start
├── PROJECT_ANALYSIS.md                # Analysis & recommendations
├── CALIBRATION_SUMMARY.md             # This file
├── IMPLEMENTATION_COMPLETE.md         # Complete delivery summary
└── INDEX.md                           # Documentation hub
```

### Modified Files:
- `Config.kt` - Now manages hardware configuration with runtime updates
- `KtorApplication.kt` - Loads YAML config, registers calibration routes
- `AppModule.kt` - DI modules now use Config values
- `application.yaml` - Hardware configuration added
- `README.md` - Calibration documentation added

## Best Way to Calibrate

### Recommended Approach: Web UI (Easiest)

1. **Start the server** on your Raspberry Pi
2. **Open** `docs/calibration-tool.html` in a browser (works on any device on same network)
3. **Set server URL** if not localhost
4. **Click "Load Current Config"** to see current values

#### For Steering:
5. Use the **angle slider** to test different steering positions
6. Find your perfect **center**, **left**, and **right** angles
7. Enter values and click **"Save Steering Config"**

#### For Motor:
8. Click **"Arm ESC"** and wait 2-3 seconds
9. Use **throttle slider** to test (start with 10-25%)
10. Fine-tune the neutral and forward threshold values
11. Click **"Save Motor Config"**

#### Finalize:
12. Once satisfied, **copy values** to `application.yaml`
13. **Restart server** to persist changes

### Alternative: API-Based (Advanced)

Use curl commands from `CALIBRATION.md` if you prefer command-line control or want to script the calibration process.

## Safety Features Built-In

1. **PWM Range Validation** - Prevents unsafe pulse widths (500-2500µs range enforced)
2. **Channel Validation** - Ensures only valid PCA9685 channels (0-15)
3. **Emergency Stop** - Always available via web UI or API
4. **Throttle Clamping** - Motor throttle limited to -100% to +100%
5. **Mock Mode Fallback** - If hardware fails, falls back to mock mode

## Example Calibration Workflow

```bash
# 1. Check connection
curl http://localhost:8080/status

# 2. Get current config
curl http://localhost:8080/calibration

# 3. Test steering center
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 120.0}'

# 4. Adjust if needed
curl -X POST http://localhost:8080/calibration/steering/angle \
  -H "Content-Type: application/json" \
  -d '{"angle": 118.5}'

# 5. Save the calibration
curl -X PATCH http://localhost:8080/calibration/steering \
  -H "Content-Type: application/json" \
  -d '{"centerAngle": 118.5}'

# 6. Arm ESC
curl -X POST http://localhost:8080/calibration/motor/stop

# Wait 2-3 seconds...

# 7. Test throttle
curl -X POST http://localhost:8080/calibration/motor/throttle \
  -H "Content-Type: application/json" \
  -d '{"throttlePercent": 0.2}'

# 8. Stop motor
curl -X POST http://localhost:8080/calibration/motor/stop
```

## Benefits of This Approach

✅ **No Code Changes Needed** - All configuration in YAML  
✅ **Real-time Testing** - Test changes immediately without restart  
✅ **User-Friendly** - Beautiful web UI for non-technical users  
✅ **API Available** - Automation and scripting possible  
✅ **Safe** - Multiple safety validations built-in  
✅ **Well Documented** - Complete guides provided  
✅ **Testable** - Works in mock mode for development  

## Next Steps

1. Build and deploy: `./gradlew build`
2. Start server on Raspberry Pi
3. Open `docs/calibration-tool.html` in browser
4. Calibrate your hardware
5. Update `application.yaml` with final values
6. Enjoy your perfectly calibrated RC car! 🏎️

---

**Note:** Remember to keep your wheels off the ground when calibrating the motor!

