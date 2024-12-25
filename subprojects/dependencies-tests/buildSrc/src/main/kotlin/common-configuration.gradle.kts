import org.gradle.api.JavaVersion.VERSION_11


group = "io.github.mickle-ak.mockobor"

// test/compile dependencies versions
val junit5Version = "5.11.4"
val assertjVersion = "3.27.0"


plugins {
    java
}

repositories {
    mavenLocal()
    mavenCentral()
}


java {
    sourceCompatibility = VERSION_11
    targetCompatibility = VERSION_11
}

dependencies {

    val mockoborVersion = "+"

    testImplementation("io.github.mickle-ak.mockobor:mockobor:$mockoborVersion")

    testImplementation(platform("org.junit:junit-bom:$junit5Version"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5Version")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("mockito-mock-maker", System.getProperty("mockito-mock-maker", "inline"))
    jvmArgs("-Xshare:off")

    testLogging {
        events("skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    enableAssertions = true
    failFast = false

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // will match the outermost suite
                println(
                        "Test result: ${result.resultType} " +
                                "(${result.testCount} tests, " +
                                "${result.successfulTestCount} successes, " +
                                "${result.failedTestCount} failures, " +
                                "${result.skippedTestCount} skipped)"
                )
            }
        }
    })
}
