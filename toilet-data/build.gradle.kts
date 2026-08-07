plugins {
    id("braveberry.jvm.library")
    id("braveberry.jvm.hilt")
}

dependencies {
    implementation(project(":domain"))
}
