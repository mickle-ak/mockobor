plugins {
    id("common-configuration")
}

dependencies {
    val mockitoVersion = "5.4.0"

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}
