plugins {
    id("common-configuration")
}

dependencies {
    val easymock_version = "4.3"
    testImplementation("org.easymock:easymock:$easymock_version")
}
