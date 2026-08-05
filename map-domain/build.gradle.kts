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
dependencies{
    // Javax Inject (@Inject 사용)
    implementation("javax.inject:javax.inject:1")

    // 🔥 [수정] 순수 코틀린 모듈에서는 안드로이드 Hilt 대신 Dagger 코어/컴파일러를 사용합니다.
    // version 번호는 프로젝트에 정의된 버전을 따르거나 명시해주시면 됩니다.
    implementation("com.google.dagger:dagger:2.51.1") // 또는 libs.dagger 등
    ksp("com.google.dagger:dagger-compiler:2.51.1")     // 또는 libs.dagger.compiler

}