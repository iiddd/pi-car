import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.iiddd"
version = "1.0.0"

application {
    mainClass.set("com.iiddd.server.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor dependencies
    implementation(libs.bundles.ktor)

    // Diozero dependencies
    implementation(libs.bundles.diozero)

    // Logging
    implementation(libs.logback.classic)

    // Pi4J dependencies
    implementation(libs.bundles.pi4j)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}