// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
}
allprojects {
    configurations.all {
        resolutionStrategy {
            // Hilt와 KSP가 꼬이지 않도록 최신 JavaPoet 강제 고정
            force("com.squareup:javapoet:1.13.0")
        }
    }
}
buildscript {
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
    }
}