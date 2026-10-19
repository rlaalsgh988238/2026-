package com.tourdataproject.presentation.testing

import com.tourdataproject.presentation.utility.Log
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.rules.ExternalResource

/**
 * JVM에 없는 android.util.Log 호출만 테스트 경계에서 대체합니다.
 * 모든 Android API에 기본값을 반환시키는 returnDefaultValues는 사용하지 않습니다.
 * 전역 객체를 Mock하므로 이 Rule을 사용하는 테스트를 같은 JVM에서 병렬 실행하지 않습니다.
 */
class PresentationLogRule : ExternalResource() {
    override fun before() {
        mockkObject(Log)
        every { Log.d(any<String>()) } just Runs
        every { Log.d(any<String>(), any<String>()) } just Runs
        every { Log.e(any<String>()) } just Runs
        every { Log.e(any<String>(), any<String>()) } just Runs
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } just Runs
        every { Log.i(any<String>()) } just Runs
        every { Log.i(any<String>(), any<String>()) } just Runs
    }

    override fun after() {
        unmockkObject(Log)
    }
}
