# Quick Start: Calibrate Your Pi-Car in 15 Minutes

This guide will get your RC car perfectly calibrated in about 15 minutes.

## Prerequisites ✓

- [ ] Pi-Car server is running on Raspberry Pi
- [ ] Car wheels are OFF THE GROUND (safety first!)
- [ ] Power supply is connected (7.4V, 9A)
- [ ] Computer/tablet on same network as Pi

## Step 1: Open the Calibration Tool (1 min)

1. On your computer, open a web browser
2. Navigate to `http://<pi-ip-address>:8080/` (e.g., `http://REMOVED_IP:8080/`)
3. The calibration tool will load automatically
4. Leave the "Server URL" field **empty** (it uses the same origin)

You should see: ✅ Connected! Mock Mode: false

## Step 2: Calibrate Steering (5 min)

### Find Center Position

1. Move the **Angle slider** to 120°
2. Look at your car's wheels - are they straight?
3. If not, adjust the slider until wheels are perfectly straight
4. Note the angle value (e.g., 118.5°)
5. Enter this value in **"Center Angle"** field

### Find Left Turn

1. Move the slider down to 90°
2. Observe the wheels turning left
3. Find the maximum comfortable left turn (not forcing the servo)
4. Note this angle (e.g., 85°)
5. Enter in **"Left Angle"** field

### Find Right Turn

1. Move the slider up to 150°
2. Observe the wheels turning right
3. Find the maximum comfortable right turn
4. Note this angle (e.g., 148°)
5. Enter in **"Right Angle"** field

### Save

Click **"💾 Save Steering Config"**

✅ You should see: "Steering calibration updated"

## Step 3: Calibrate Motor (8 min)

### ⚠️ SAFETY CHECK
- [ ] Wheels are OFF THE GROUND
- [ ] No obstacles near the car
- [ ] Emergency stop button is visible

### Arm the ESC

1. Click **"🔧 Arm ESC (Neutral)"**
2. Wait and listen for beep sequence (2-3 seconds)
3. ESC should emit beeps indicating it's armed

### Test Forward Throttle

1. Move **Throttle slider** to 10%
2. Click **"Set Throttle"**
3. Observe if the motor starts spinning
4. If not, try 15%, then 20%, etc.
5. Find the MINIMUM value where motor starts moving

**Note this value!** This helps you understand your ESC's dead zone.

### Test Increasing Throttle

1. Gradually test: 25%, 50%, 75%
2. Observe motor speed at each level
3. Never go above 75% during initial calibration!

### Emergency Stop

Click **"🛑 STOP"** when done testing

### Optional: Find Forward Threshold (Advanced)

If you want precise control over the minimum forward speed:

1. Go to **"Advanced: Direct PWM Control"**
2. Set **Channel** to 1 (motor channel)
3. Start with **Pulse Width** 1550 µs
4. Click **"Set Direct PWM"**
5. Increase in 10µs increments until motor starts
6. Note the exact value (e.g., 1580µs)
7. Enter in **"Forward Min Pulse"** field
8. Click **"💾 Save Motor Config"**

## Step 4: Save to Configuration File (1 min)

1. In the calibration tool, your current values are displayed
2. Open `src/main/resources/application.yaml` in a text editor
3. Update the values:

```yaml
hardware:
  servo:
    centerAngle: 118.5   # Your calibrated value
    leftAngle: 85.0      # Your calibrated value
    rightAngle: 148.0    # Your calibrated value
  
  motor:
    forwardMinPulseUs: 1580  # Your calibrated value (if you found it)
```

4. Save the file
5. Restart your Pi-Car server

## Step 5: Test Everything (2 min)

### Steering Test

In the calibration tool:
1. Click **"Center"** - wheels should be straight ✓
2. Click **"Left"** - wheels turn left comfortably ✓
3. Click **"Right"** - wheels turn right comfortably ✓

### Motor Test

1. Click **"Arm ESC"** - wait 2-3 seconds
2. Set throttle to 25%
3. Click **"Set Throttle"** - motor spins ✓
4. Click **"STOP"** - motor stops ✓

## 🎉 Done!

Your Pi-Car is now calibrated and ready to drive!

---

## Troubleshooting

### Problem: "Connection failed"
- Check server URL is correct
- Verify Pi-Car server is running: `curl http://PI_IP:8080/status`
- Ensure firewall allows port 8080

### Problem: Servo jitters
- Reduce PWM range in YAML:
  ```yaml
  servo:
    minPulseUs: 1100  # Instead of 1000
    maxPulseUs: 1900  # Instead of 2000
  ```
- Check power supply can deliver enough current

### Problem: ESC won't arm
- Ensure 1500µs neutral signal is sent
- Wait full 2-3 seconds
- Check ESC power connection
- Verify ESC switch is ON

### Problem: Motor doesn't start
- ESC must be armed first (neutral → wait → throttle)
- Try higher throttle (some ESCs need 15-20% minimum)
- Check `forwardMinPulseUs` in config
- Verify power supply current limit (should be at least 5A)

### Problem: Wheels turn in wrong direction
- Swap left and right angle values in config
- Or physically adjust servo horn position

---

## Tips for Best Results

💡 **Steering:**
- Don't force the servo to extreme angles (risk of stripping gears)
- Leave some margin from mechanical limits
- Center angle is most important - get it perfect!

💡 **Motor:**
- Always arm ESC before throttle
- Start with LOW throttle values (10-20%)
- Some ESCs have built-in delay before motor starts
- Forward threshold varies by ESC brand (typically 1550-1650µs)

💡 **Testing:**
- Keep wheels off ground until you trust the calibration
- Use the web UI stop button frequently
- Test at low speed first, gradually increase
- Save calibration values after any adjustment

---

## Next Steps

Once calibrated:
1. Test driving with WebSocket controller
2. Implement your control interface (gamepad, phone app, etc.)
3. Add autonomous features if desired
4. Have fun! 🏎️💨

**Need help?** Check:
- `CALIBRATION.md` - Full API reference with curl examples
- `PROJECT_ANALYSIS.md` - Architecture and design decisions
- `README.md` - Project overview

