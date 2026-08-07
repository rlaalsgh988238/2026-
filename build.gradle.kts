// Top-level build file
plugins {
    // 안드로이드 관련
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // 코틀린 관련 (android와 jvm은 보통 하나만 쓰지만, 멀티모듈을 위해 정의)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    // Compose (Kotlin 2.0 이상 필수)
    alias(libs.plugins.kotlin.compose) apply false

    // 도구 관련
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}
