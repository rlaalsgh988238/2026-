package com.tourdataproject.map_remote

import mu.KLogger
import mu.KotlinLogging
import java.io.File

class KakaoMapApiGetter(
    // 생성자로 로거를 받게 해서, 필요시 외부에서 주입할 수도 있고 기본값도 알아서 세팅되게 구성
    private val logger: KLogger = KotlinLogging.logger {}
) {
    private val keyFile = File("KeyFile/TestKey.txt")

    fun getKey(): String =
        runCatching {
            keyFile.readText().trim()
        }
            .onSuccess {
                // println 대신 info 레벨 로그로 깔끔하게 출력
                logger.info { "Kakao Map Key 로드 성공 (길이=${it.length})" }
            }
            .onFailure {
                // 에러 발생 시 예외 객체(it)까지 같이 넘겨서 스택 트레이스도 찍히게 처리
                logger.error(it) { "Kakao Map Key 읽기 실패" }
            }
            .getOrDefault("")
}