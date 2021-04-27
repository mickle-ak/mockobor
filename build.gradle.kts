import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.util.*


group = "io.github.mickle-ak.mockobor"
version = "1.0"
description = "Mocked Observable Observation - library to simplifying some aspects of unit testing with java."


plugins {
    // build
    `java-library`
    jacoco

    // publishing
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

repositories {
    mavenCentral()
}


// ==================================================================================
// ==================================== build =======================================
// ==================================================================================

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

dependencies {

    val junit5_version = "5.7.1"
    val assertj_version = "3.19.0"
    val mockito_version = "3.8.0"
    val easymock_version = "4.3"
    val lombok_version = "1.18.20"

    compileOnly("org.mockito:mockito-core:$mockito_version")
    compileOnly("org.easymock:easymock:$easymock_version")
    compileOnly("org.projectlombok:lombok:$lombok_version")
    annotationProcessor("org.projectlombok:lombok:$lombok_version")

    testImplementation(platform("org.junit:junit-bom:$junit5_version"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5_version")
    testImplementation("org.assertj:assertj-core:$assertj_version")
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockito_version")
    testImplementation("org.easymock:easymock:$easymock_version")
    testCompileOnly("org.projectlombok:lombok:$lombok_version")
    testAnnotationProcessor("org.projectlombok:lombok:$lombok_version")
}

// configure test starter
tasks.test {
    useJUnitPlatform()
    systemProperty("mockito-mock-maker", System.getProperty("mockito-mock-maker", "inline"))

    testLogging {
        events("skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
    enableAssertions = true
    failFast = false

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // will match the outermost suite
                println("Test result: ${result.resultType} " +
                        "(${result.testCount} tests, " +
                        "${result.successfulTestCount} successes, " +
                        "${result.failedTestCount} failures, " +
                        "${result.skippedTestCount} skipped)")
            }
        }
    })
}

// disable strict checking of javadoc in java 8+
tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

// configure jacoco report task
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    doLast {
        println("full jacoco report: " + reports.html.entryPoint.absolutePath)
    }
}


// ==================================================================================
// ================================== publishing ====================================
// ==================================================================================

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("sonatypeUsername")?.toString())
            password.set(base64Decode("sonatypePassword64"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mockobor") {
            from(components["java"])
            pom {

                name.set("${project.group}:${project.name}")
                description.set(project.description)
                url.set("https://github.com/mickle-ak/mockobor")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("mickle-ak")
                        name.set("Mikhail Kiselev")
                        email.set("mickle.ak@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/mickle-ak/mockobor.git")
                    developerConnection.set("scm:git:ssh://github.com:mickle-ak/mockobor.git")
                    url.set("http://github.com/mickle-ak/mockobor")
                }
            }
        }
    }
}

signing {
    // use
    useInMemoryPgpKeys(base64Decode("signingKey64"), base64Decode("signingPassword64"))
    sign(publishing.publications)
}

fun base64Decode(prop: String): String? {
    return project.findProperty(prop)?.let {
        String(Base64.getDecoder().decode(it.toString())).trim()
    }
}
