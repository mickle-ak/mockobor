plugins {
    id("common-configuration")
}

dependencies {
    val easymockVersion = "5.1.0"

    testImplementation("org.easymock:easymock:$easymockVersion")
}
