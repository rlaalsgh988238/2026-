plugins {
    id("java-library")
    id("braveberry.jvm.hilt")    // ksp, javax.inject, dagger, dagger-compiler 설정

    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data-resource"))
    implementation(libs.kotlinx.coroutines.core)

}

