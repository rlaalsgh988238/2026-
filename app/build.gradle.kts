import java.util.Properties

plugins {
    id("braveberry.android.application")
    id("braveberry.android.compose")
    id("braveberry.android.hilt")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")

    if (propertiesFile.exists()) {
        propertiesFile.reader(Charsets.UTF_8).use { reader ->
            load(reader)
        }
    }
}

fun requiredLocalProperty(name: String): String =
    localProperties.getProperty(name)
        ?.takeIf { it.isNotBlank() }
        ?: error("local.properties에 $name 설정이 필요합니다.")

val kakaoMapKey = localProperties
    .getProperty("KAKAO_MAP_KEY")
    .orEmpty()

android {
    namespace = "com.braveberry.tourdataproject"

    defaultConfig {
        applicationId = "com.braveberry.tourdataproject"

        versionCode = 4
        versionName = "vp1.0.1"

        buildConfigField(
            "String",
            "KAKAO_MAP_KEY",
            "\"$kakaoMapKey\""
        )
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                "/Users/minhokim/Documents/변수없길/변수없길키.jks"
            )

            keyAlias = "key0"

            storePassword = requiredLocalProperty(
                "RELEASE_STORE_PASSWORD"
            )

            keyPassword = requiredLocalProperty(
                "RELEASE_KEY_PASSWORD"
            )
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // 카카오맵
    implementation(libs.kakao.map)

    // Hilt Navigation
    implementation(libs.androidx.hilt.navigation.compose)

    // Orbit
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)

    // 프로젝트 모듈
    implementation(project(":presentation"))
    implementation(project(":map-data"))
    implementation(project(":map-remote"))
    implementation(project(":tourdata-data"))
    implementation(project(":tourdata-remote"))
    implementation(project(":local"))
}
