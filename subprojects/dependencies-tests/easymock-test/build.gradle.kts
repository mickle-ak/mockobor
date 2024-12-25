plugins {
    id("common-configuration")
}


val easymockVersion = "5.5.0"

dependencies {
    testImplementation("org.easymock:easymock:$easymockVersion")
}
