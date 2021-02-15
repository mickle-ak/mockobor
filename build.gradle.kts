plugins {
    `java-library`
}

group = "de.mockobor"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    jcenter()
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.mockito:mockito-core:3.7.7")
    testRuntimeOnly("org.mockito:mockito-core:3.7.7")
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showExceptions = true
        events("passed", "skipped", "failed")
    }
}

