import java.util.Properties

plugins {
    id("braveberry.android.application") // 기본 앱 설정 (core.ktx, 테스트 등 포함 권장)
    id("braveberry.android.compose")     // Compose 관련 의존성 + buildFeatures 자동 주입
    id("braveberry.android.hilt")        // Hilt + KSP 자동 주입
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())
val kakaoMapKey = properties.getProperty("KAKAO_MAP_KEY") ?: ""

android {
    namespace = "com.braveberry.tourdataproject"

    defaultConfig {
        applicationId = "com.braveberry.tourdataproject"
        versionCode = 3
        versionName = "1.1.1"
        buildConfigField("String", "KAKAO_MAP_KEY", "\"$kakaoMapKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        // compose = true는 플러그인으로 이동했으므로 생략
        buildConfig = true
    }
}

dependencies {
    // 카카오맵
    implementation(libs.kakao.map)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
    implementation(project(":presentation"))
    implementation(project(":map-data"))
    implementation(project(":map-remote"))
    implementation(project(":local"))
}
