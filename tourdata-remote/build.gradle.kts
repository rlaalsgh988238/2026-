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
val tourApiKey = properties.getProperty("TOUR_API_KEY") ?: ""
val tourBaseUrl = properties.getProperty("TOUR_BASE_URL") ?: "https://apis.data.go.kr/B551011/KorService2/"


android {
    namespace = "com.tourdataproject.tourdata_remote"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "TOUR_API_KEY", "\"$tourApiKey\"")
        buildConfigField("String", "TOUR_BASE_URL", "\"$tourBaseUrl\"")
    }
}
