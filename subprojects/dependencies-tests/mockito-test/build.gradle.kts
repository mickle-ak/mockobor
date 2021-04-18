plugins {
    id("common-configuration")
}

dependencies {
    val mockito_version = "2.28.2" // "3.8.0"
    testImplementation("org.mockito:mockito-junit-jupiter:$mockito_version")
}
