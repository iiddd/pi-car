//package server
//
//import com.pi4j.Pi4J
//import server.hardware.PCA9685Controller
//
//fun main() {
//    val pi4j = Pi4J.newAutoContext()
//    val pwm = PCA9685Controller(pi4j)
//
//    // Send neutral signal to channel 0
//    pwm.setPulseMicroseconds(channel = 0, pulseMicroseconds = 1520)
//
//    println("✅ Test signal sent to channel 0 (1520 µs)")
//}