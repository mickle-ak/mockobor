import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.mickle-ak"
version = "1.0-SNAPSHOT"


java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}


repositories {
    mavenCentral()
    jcenter()
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


tasks {

    // configure test starter
    named<Test>("test") {
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

    // disable strict checking of javadoc in java 8+
    named<Javadoc>("javadoc") {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

publishing {
    publications {
        create<MavenPublication>("mockobor") {
            from(components["java"])
        }
    }
}
        
