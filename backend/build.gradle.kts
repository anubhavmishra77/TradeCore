plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
    application
}

group = "com.tradecore"
version = "0.1.0"

application {
    mainClass.set("com.tradecore.backend.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")

    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:10.15.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.15.0")

    implementation("redis.clients:jedis:5.1.3")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.1")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:postgresql:1.19.8")
}

tasks.test {
    useJUnitPlatform()
}
