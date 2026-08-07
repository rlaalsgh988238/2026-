plugins {
    id("braveberry.android.library")
    id("braveberry.android.hilt") // Hilt 의존성 + KSP 자동 주입
    id("braveberry.android.room") // Room 의존성 + KSP 자동 주입
}

android {
    namespace = "com.braveberry.localDB"
}

dependencies {
    // 안드로이드 기본 코어
    implementation(libs.androidx.core.ktx)

    // 테스트용 (로컬 단위 테스트 & Room DB 기기 테스트)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // 다른 모듈 참조
    implementation(project(":toilet-data"))
}

