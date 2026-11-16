plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.gson)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
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
    commandLine("python3", "-m", "http.server", "8000")
}
