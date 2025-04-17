import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
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
    implementation("io.ktor:ktor-server-core:3.1.2")
    implementation("io.ktor:ktor-server-netty:3.1.2")
    implementation("io.ktor:ktor-server-websockets:3.1.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.2")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("com.pi4j:pi4j-core:3.0.1")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:3.0.1")
}

// ✅ Target JVM 17 (compile with JDK 22 but emit Java 17-compatible bytecode)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

// ✅ Fat JAR
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.iiddd.server.MainKt"
    }
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}