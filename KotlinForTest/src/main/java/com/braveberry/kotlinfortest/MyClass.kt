package com.braveberry.kotlinfortest

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.Duration

/**
 * 도농인력중개플랫폼 크롤러 클래스
 */
class MyClass {

    /**
     * 로그인 후 상세 공고 페이지의 HTML 소스를 가져오는 함수
     */
    fun fetchJobDetail(userId: String, userPwd: String, jobId: String): String? {
        // 1. 시스템 환경에 맞는 크롬 드라이버 자동 관리 및 빌드
        WebDriverManager.chromedriver().setup()

        // 2. 크롬 브라우저 옵션 설정
        val options = ChromeOptions().apply {
            addArguments("--remote-allow-origins=*")
            // 백그라운드 구동(창 숨기기)을 원하시면 아래 주석을 해제하세요.
            // addArguments("--headless")
        }

        val driver = ChromeDriver(options)

        // 브라우저 내부 요소 로딩을 위한 암묵적 대기(최대 15초)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15))

        return try {
            // 3. 제공해주신 실제 로그인 페이지 주소로 정확히 접속
            val loginUrl = "https://www.agriwork.kr/front/user/login.do"
            driver.get(loginUrl)
            Thread.sleep(2500) // 페이지 인프라 및 스크립트 안정화 대기

            // 4. 아이디/비밀번호 입력 폼 매핑 및 텍스트 주입
            val idField = driver.findElement(By.id("userId"))
            idField.clear()
            idField.sendKeys(userId)

            val pwdField = driver.findElement(By.id("userPwd"))
            pwdField.clear()
            pwdField.sendKeys(userPwd)

            // 5. 로그인 버튼 클릭 액션 트리거
            driver.findElement(By.id("loginBtn")).click()

            // 로그인 처리 후 세션 쿠키가 브라우저에 온전히 구워지도록 넉넉히 대기
            println("🔑 로그인 인증 요청 중... 잠시만 기다려주세요.")
            Thread.sleep(4000)

            // 6. 로그인 상태가 담긴 브라우저 컨텍스트를 유지하며 상세 페이지로 이동
            val targetUrl = "https://agriwork.kr"
            driver.get(targetUrl)
            Thread.sleep(3000) // 상세 데이터 렌더링 완성 대기

            // 7. 데이터가 로드된 최종 웹페이지 HTML 소스 반환
            driver.pageSource

        } catch (e: Exception) {
            println("❌ 크롤링 중 오류가 발생했습니다: ${e.message}")
            e.printStackTrace()
            null
        } finally {
            // 8. 메모리 방출을 위한 브라우저 완전 종료
            driver.quit()
        }
    }
}

/**
 * 모듈 테스트 및 검증용 메인 함수
 */
fun main() {
    val crawler = MyClass()

    // 💡 테스트용 사용자 계정 정보 정의
    val myId = "rlaalsgh9882"
    val myPwd = "" // 👈 꼭 가입하신 실제 비밀번호 문자열로 채워주세요.
    val targetJobId = "f5a3269d6a3f495183c384ce4374a06d"

    println("🚀 도농인력중개플랫폼 타겟 로그인 크롤링 프로세스를 개시합니다...")

    val htmlResult = crawler.fetchJobDetail(myId, myPwd, targetJobId)

    if (!htmlResult.isNullOrEmpty()) {
        println("✅ 성공적으로 페이지 소스를 캡처했습니다!")
        println("=== HTML 내용 상위 500자 ===")
        println(htmlResult.take(500) + "\n...")
    } else {
        println("❌ 데이터 수집에 실패했습니다. 계정 인증 정보나 시스템 차단 요소를 체크하세요.")
    }
}
