
plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ksp)
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
    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // JSON Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Javax Inject (@Inject 사용)
    implementation("javax.inject:javax.inject:1")

    // 🔥 [수정] 순수 코틀린 모듈에서는 안드로이드 Hilt 대신 Dagger 코어/컴파일러를 사용합니다.
    // version 번호는 프로젝트에 정의된 버전을 따르거나 명시해주시면 됩니다.
    implementation("com.google.dagger:dagger:2.51.1") // 또는 libs.dagger 등
    ksp("com.google.dagger:dagger-compiler:2.51.1")     // 또는 libs.dagger.compiler

    // 1. mu.KotlinLogging 사용을 위한 라이브러리
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // 2. main() 함수에서 테스트할 때 콘솔에 실제로 로그를 찍어줄 엔진
    implementation("org.slf4j:slf4j-simple:2.0.9")
}