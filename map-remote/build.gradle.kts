import java.util.Properties

plugins {
    id("braveberry.android.library")
    id("braveberry.android.hilt")
    id("braveberry.jvm.retrofit")
}

//local properties 쓰기 위한 코드
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}
val kakaoApiKey = properties.getProperty("KAKAO_API_KEY") ?: ""


android {
    namespace = "com.tourdataproject.map_remote" //

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "KAKAO_API_KEY", "\"$kakaoApiKey\"")
    }
}
dependencies {
    testImplementation(libs.mockwebserver)
    implementation(project(":map-data"))
    implementation(project(":data-resource"))
    implementation(libs.kotlinx.coroutines.core) // 버전은 프로젝트에 맞게!


}

