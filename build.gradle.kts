// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    // ksp와 hilt 플러그인을 루트 build.gradle.kts에서 적용하지 않고, 각 모듈의 build.gradle.kts에서 적용하도록 변경
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}