plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Selenium WebDriver (핵심 라이브러리)
    implementation("org.seleniumhq.selenium:selenium-java:4.25.0")

    // WebDriver Manager (크롬 드라이버 자동 관리용)
    implementation("io.github.bonigarcia:webdrivermanager:5.9.2")
}