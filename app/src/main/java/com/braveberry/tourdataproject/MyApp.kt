package com.braveberry.tourdataproject

import android.app.Application
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY)
    }
}