import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    java
}

group = "io.github.mickle-ak.mockobor"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

dependencies {

    val mockoborVersion = "+"

    testImplementation("io.github.mickle-ak.mockobor:mockobor:$mockoborVersion")


    val junit5Version = "5.7.1"
    val assertjVersion = "3.19.0"

    testImplementation(platform("org.junit:junit-bom:$junit5Version"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5Version")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showExceptions = true
        events("passed", "skipped", "failed")
    }
    enableAssertions = true
    failFast = false

    systemProperty("mockito-mock-maker", System.getProperty("mockito-mock-maker", "inline"))
}
