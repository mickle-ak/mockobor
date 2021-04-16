plugins {
    `java-library`
    id("io.freefair.lombok") version "5.3.0"
}

group = "org.mockobor"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    jcenter()
}


dependencies {

    val junit5_version = "5.7.1"
    val assertj_version = "3.19.0"
    val mockito_version = "3.8.0"

    compileOnly("org.mockito:mockito-core:$mockito_version")

    testImplementation(platform("org.junit:junit-bom:$junit5_version"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5_version")
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockito_version")
    testImplementation("org.assertj:assertj-core:$assertj_version")
}


tasks {
    val test = "test"(Test::class) {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            showExceptions = true
            events("passed", "skipped", "failed")
        }
        enableAssertions = true
        failFast = false
        ignoreFailures = true
    }

    // to start all tests second time with standard mockito mock maker
    val testWithMockitoStandardMockMaker = register<Test>("testWithMockitoStandardMockMaker") {
        useJUnitPlatform()
        systemProperty("mockito-mock-maker", "standard")
        shouldRunAfter(test)
    }
    "check" {
        dependsOn(testWithMockitoStandardMockMaker)
    }

    // disable strict checking of javadoc in java 8+
    named<Javadoc>("javadoc") {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}
