
plugins {
    alias(libs.plugins.android.application)
}
/*
plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization") version "2.4.20"
}

 */

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

application {
    mainClass.set("tools.migrationscanner.MigrationScannerKt")
}

tasks.register("runAggregation", JavaExec::class) {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("tools.migrationscanner.MigrationAggregatorKt")
}
tasks.register<Exec>("serveDashboard") {
    group = "dashboard"
    description = "Serves the dashboard over HTTP so fetch() can load JSON files."
        commandLine( "python3", "-m", "http.server", "8000")
}
