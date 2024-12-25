plugins {
    id("common-configuration")
}


val mockitoVersion = "5.14.2"

dependencies {
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}

// enable the mockito's inline-mock-maker (required for java 21+)
val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") { isTransitive = false }
}
tasks {
    test {
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
    }
}
