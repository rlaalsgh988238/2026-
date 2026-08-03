package com.braveberry.tourdataproject

import android.app.Application
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
class MyApp : Application() { // <-- 괄호() 추가 완료
    override fun onCreate() {
        super.onCreate()

        // 카카오 지도 SDK 초기화
        Log.d("KakaoKeyCheck", "내 맵 키 값: [${BuildConfig.KAKAO_MAP_KEY}]")
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY)
    }
}