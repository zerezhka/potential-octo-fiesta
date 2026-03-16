plugins {
    kotlin("jvm") version "2.2.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass = "app.MainKt"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// Run individual levels: ./gradlew runLevel1, runLevel2, ...
(1..8).forEach { level ->
    tasks.register<JavaExec>("runLevel$level") {
        group = "levels"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("level$level.MainKt")
    }
}
