import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.iiddd"
version = "1.0.0"

application {
    mainClass.set("server.KtorApplicationKt")
}

repositories {
    mavenCentral()
}

// Force consistent Netty version to resolve conflicts between Ktor and Diozero
val nettyVersion = "4.2.9.Final"
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty") {
            useVersion(nettyVersion)
            because("Align all Netty modules to the same version for Ktor compatibility")
        }
    }
}

dependencies {
    // Ktor dependencies
    implementation(libs.bundles.ktor)

    // Diozero dependencies (PCA9685, I2C, PWM)
    implementation(libs.bundles.diozero)

    // Logging
    implementation(libs.logback.classic)


    // Koin DI
    implementation(libs.bundles.koin)

    // Testing
    testImplementation(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}