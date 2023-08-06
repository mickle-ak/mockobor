import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


group = "io.github.mickle-ak.mockobor"
version = scmVersion.version
description = "Mocked Observable Observation - java library to simulate sending of events " +
        "from a mocked collaborator to a tested object."


plugins {
    // build
    `java-library`
    jacoco

    // versioning
    id("pl.allegro.tech.build.axion-release") version "1.14.4"

    // publishing
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"

    // IDE
    idea
    eclipse
}


repositories {
    mavenCentral()
}


// ==================================================================================
// ==================================== build =======================================
// ==================================================================================

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

dependencies {

    val mockitoVersion = "5.4.0"
    val easymockVersion = "5.1.0"

    val eclipseAnnotationVersion = "2.2.700"

    val lombokVersion = "1.18.28"
    val junit5Version = "5.10.0"
    val assertjVersion = "3.24.2"

    implementation("org.eclipse.jdt:org.eclipse.jdt.annotation:$eclipseAnnotationVersion")

    compileOnly("org.mockito:mockito-core:$mockitoVersion")
    compileOnly("org.easymock:easymock:$easymockVersion")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation(platform("org.junit:junit-bom:$junit5Version"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5Version")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.easymock:easymock:$easymockVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
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
    options.overview = "src/main/javadoc/overview.html"
}

// configure jacoco report task
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports.xml.required = true
    reports.html.required = true
    doLast {
        println("full jacoco report: " + reports.html.entryPoint.absolutePath)
    }
}


// ==================================================================================
// ============================ versioning / changelog ==============================
// ==================================================================================

// see https://axion-release-plugin.readthedocs.io/en/latest/configuration/tasks/
// usage:
// ./gradlew currentVersion
// ./gradlew createRelease

scmVersion {
    hooks {
        pre( // update version in dependency examples
                "fileUpdate", mapOf(
                "file" to "README.md",
                "pattern" to KotlinClosure2({ v: Any, _: Any -> """(<version>|mockobor:)$v(</version>|"\))""" }),
                "replacement" to KotlinClosure2({ v: Any, _: Any -> """$1$v$2""" })
        )
        )
        pre( // update version in change log
                "fileUpdate", mapOf(
                "file" to "CHANGELOG.md",
                "pattern" to KotlinClosure2({ _: Any, _: Any -> """- \*\*In the next Version\*\*""" }),
                "replacement" to KotlinClosure2({ v: Any, _: Any -> "- **In the next Version**\n\n- **$v** (${currentDate()})" })
        )
        )
        pre("commit")
    }
    checks {
        uncommittedChanges = false
        aheadOfRemote = false
    }
}

tasks.currentVersion {
    doNotTrackState("axion-release-plugin uses old deprecated api (accessing unreadable inputs or outputs)")
}

fun currentDate() = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.now())

tasks.create("getLastChangesFromChangelog") {
    doLast {
        val readmeText = File("CHANGELOG.md").readText(Charsets.UTF_8)
        val versionPattern = """[\d+.]+"""
        val regex = Regex("""- \*\*$versionPattern\*\*.*?\n+(.*?)(?:\n*- \*\*[\d.]+\*\*|\n\n|$)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(readmeText)
        if (matchResult != null && matchResult.groupValues.size > 1) {
            println(matchResult.groupValues[1])
        }
    }
}


// ==================================================================================
// ================================== publishing ====================================
// ==================================================================================

nexusPublishing {
    this.repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            username = findProperty("sonatypeUsername")?.toString()
            password = base64Decode("sonatypePassword64")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mockobor") {
            from(components["java"])
            pom {

                name = "Mockobor"
                description = project.description
                url = "https://github.com/mickle-ak/mockobor"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "mickle-ak"
                        name = "Mikhail Kiselev"
                        email = "mickle.ak@gmail.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/mickle-ak/mockobor.git"
                    developerConnection = "scm:git:ssh://github.com:mickle-ak/mockobor.git"
                    url = "https://github.com/mickle-ak/mockobor"
                }
            }
        }
    }
}

signing {
    isRequired = findProperty("signingKey64") != null && findProperty("signingPassword64") != null
    useInMemoryPgpKeys(base64Decode("signingKey64"), base64Decode("signingPassword64"))
    sign(publishing.publications)
}

fun base64Decode(prop: String): String? {
    return findProperty(prop)?.let {
        String(Base64.getDecoder().decode(it.toString())).trim()
    }
}

fun findProperty(prop: String) = project.findProperty(prop) ?: System.getenv(prop)


/*
tasks.create("testEnvironment") {
    doLast {
        println("RELEASE_VERSION=" + findProperty("RELEASE_VERSION"))
        println("signingKey64=" + findProperty("signingKey64"))
        println("signingPassword64=" + findProperty("signingPassword64"))
        println("sonatypeUsername=" + findProperty("sonatypeUsername"))
        println("sonatypePassword64=" + findProperty("sonatypePassword64"))
    }
}
*/


// =================================================================================
// ====================================  IDE  ======================================
// =================================================================================

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

eclipse {
    classpath {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
