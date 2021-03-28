plugins {
    `java-library`
    id("io.freefair.lombok") version "5.3.0"
}

group = "de.mockobor"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    jcenter()
}


dependencies {

    compileOnly("org.mockito:mockito-core:3.7.7")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.7.7")
    testImplementation("org.assertj:assertj-core:3.19.0")
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showExceptions = true
        events("passed", "skipped", "failed")
    }
}

