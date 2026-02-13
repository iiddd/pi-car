# Copilot instructions for this repository

## Language
•	Generate Kotlin code only
•	Do not output code in other languages unless explicitly requested

## Naming Conventions (strict)
•	Do not abbreviate words in identifiers
•	Always use full, descriptive names for:
•	functions
•	methods
•	variables
•	parameters
•	classes
•	objects
•	constants

## Examples

✅ Correct:
•	pwmFrequency
•	throttlePulseDurationMicroseconds
•	motorChannel
•	carController
•	neutralThrottle

❌ Incorrect:
•	freq
•	thrDur
•	ch
•	ctrl
•	throt

## Kotlin Style Rules

### Named Arguments
•	Use named arguments unless there’s only one parameter, or it is a common standard library call,
or it's a java library method with no named parameters allowed 

### Lambdas
•	Always use explicit parameter names in lambdas
•	Avoid _ unless ignoring the parameter is necessary

### Architecture Assumptions
•	The application is a Ktor server, running on a Raspberry Pi
•	The architecture is modular, with clean separation of concerns:
•	CarController orchestrates input/output
•	MotorManager and ServoManager directly control hardware via I2C (PCA9685)
•	Hardware-related configuration is isolated from application logic

main()
├── initializes PCA9685 and devices
├── creates ServoManager, MotorManager
└── creates and uses CarController

### Patterns to follow
•	Use explicit initialization order
•	Group hardware interaction logic into clearly named classes
•	Avoid global mutable state
•	Avoid magic numbers — define constants for neutral/forward/reverse pulses, PWM frequency, etc.

## Output Expectations
•	Prefer correctness and clarity over brevity
•	Avoid speculative or placeholder APIs
•	Do not introduce abstraction layers unless explicitly asked
•	Deprecated APIs are not allowed unless required for hardware compatibility

## Hardware & Low-Level I/O
•	PWM values must be set in microseconds
•	ESCs and servos are connected via PCA9685 (I2C)
•	Neutral pulse typically starts at 1500 µs
•	Always wait a few seconds after setting neutral before sending throttle
•	Document hardware-specific behavior in code comments

## Code Formatting
•	Use standard Kotlin code style (Kotlin Coding Conventions)
•	Align named parameters vertically where helpful
•	Prefer multiline formatting for complex function calls

## Comments
•	Document hardware quirks inline using // comments
•	Use KDoc (/** */) for public-facing methods and classes