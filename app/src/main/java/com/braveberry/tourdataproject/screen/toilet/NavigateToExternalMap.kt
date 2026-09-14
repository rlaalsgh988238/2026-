package com.braveberry.tourdataproject.screen.toilet

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun navigateToExternalMap(
    context: Context,
    startLat: Double,
    startLng: Double,
    destLat: Double,
    destLng: Double
) {
    // 여기에 넣은 이유 -> Context, Intent 같은 안드로이드 프레임 워크 종속적인 함수이기 때문
    //viewModel의 역할과 다르다고 생각한다
    val url = "kakaomap://route?sp=$startLat,$startLng&ep=$destLat,$destLng&by=FOOT"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "카카오맵이 설치되어 있지 않습니다. 플레이스토어로 이동합니다.", Toast.LENGTH_SHORT).show()
        val playStoreIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=net.daum.android.map")
        )
        context.startActivity(playStoreIntent)
    }
}