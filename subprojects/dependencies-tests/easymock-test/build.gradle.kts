plugins {
    id("common-configuration")
}

dependencies {
    val easymockVersion = "4.3"

    testImplementation("org.easymock:easymock:$easymockVersion")
}
