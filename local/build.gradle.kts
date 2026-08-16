plugins {
    id("braveberry.android.library")
    id("braveberry.android.hilt") // Hilt 의존성 + KSP 자동 주입
    id("braveberry.android.room") // Room 의존성 + KSP 자동 주입
}

android {
    namespace = "com.braveberry.local"
}

dependencies {
    implementation(project(":toilet-data"))
    implementation(project(":map-data"))
    implementation(project(":data-resource"))
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.gson)
}

