# 🦽 변수없길
장애인 화장실 정보와 함께 준비하는, 교통약자를 위한 여행 플래너

변수없길은 여행지를 고르고 일정을 구성하면서 주변 장애인 화장실과 관광지의 편의시설 정보를 확인할 수 있는 Android 애플리케이션입니다.
여행 전에는 날짜별 방문 장소와 숙소를 계획하고, 이동 중에는 현재 위치를 기준으로 주변 화장실을 찾아 카카오맵 길찾기로 연결할 수 있습니다.
Google Play에서 보기 · GitHub
📱 앱 미리보기
Google Play에 등록된 앱 소개 이미지입니다.
<p align="center">
  <img src="https://play-lh.googleusercontent.com/NVk83Xuma2urGrpKesJyOJJ2lkSRuSLc5SqUxLc68wzrE8xhMmokBD06_bAmAB4Q_IIs7J8hJimKh3t2T3Em=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 1" />
  <img src="https://play-lh.googleusercontent.com/IikdMZnlJhpXLF0yAe_M-LObFRR9KGRsMTLhG5x9BxRSFFFrYqsNgjbk05foxSfKaIpX8-Mq-82IlJlOwfs7_tw=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 2" />
  <img src="https://play-lh.googleusercontent.com/44xAoqemaNnyrsBgFBdA9YRi3Ay_R5rri2VNY_FvROi014aA8tfrAkQdsa6S39otieJA_ozOaHgozIOBpaftEg=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 3" />
</p>
<p align="center">
  <img src="https://play-lh.googleusercontent.com/akuGGc-49snuacSqecNK1sQKJ1OrPaR1Gzj6ikjWBOp4xwedkVyp8Zb_6ScAQl-KytSkkbRRTtKl9cS3WzkQ-Q=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 4" />
  <img src="https://play-lh.googleusercontent.com/TSqMD9Hc3o0cLdG10RMs7FJFF4zuK3OXH0k3bhxWvafTKILYfRWobNzaAvMH4QxKx7bWOZRlokUOTPwRR-_pseY=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 5" />
  <img src="https://play-lh.googleusercontent.com/rqUt4LnbQqEebgYlBpiD0bC-fZ6fjowqCFUvKGk4LoPvpFUvwiQgQL-M3WtS6mowNiU7j_q714SaWESmcPNq=w526-h296" width="30%" alt="변수없길 앱 소개 이미지 6" />
</p>

💡 주요 기능
여행 일정 만들기·수정하기
- 여행 지역과 시작일·종료일을 선택해 날짜별 일정을 생성합니다.
- 장소를 검색해 일정에 추가하고, 방문 순서와 메모를 관리합니다.
- 숙소와 체크인·체크아웃 날짜를 선택해 해당 일차에 반영합니다.
- 저장한 여행의 이름·지역·기간을 수정하고 여행 목록을 관리합니다.
  관광지 편의시설 정보 확인
- 관광데이터 API를 연동해 장소의 무장애 관광 정보를 조회합니다.
- 장애인 화장실, 주차장, 엘리베이터, 휠체어 및 출입 관련 정보를 확인할 수 있습니다.
- 조회한 편의시설 정보를 여행 일정의 장소 정보와 함께 표시합니다.
  지도에서 일정 살펴보기
- 일차별 방문 장소와 숙소를 카카오 지도에 표시합니다.
- 방문 순서를 나타내는 마커와 장소 간 연결선을 제공합니다.
- 지도와 일정 목록을 함께 보면서 선택한 장소의 상세 정보를 확인할 수 있습니다.
  내 주변 화장실 찾기
- 현재 위치 기준 반경 1km 내 화장실을 조회하고 거리순으로 표시합니다.
- 화장실 이름·주소·거리를 확인할 수 있습니다.
- 선택한 화장실까지의 길찾기를 카카오맵 앱으로 연결합니다.
  여행 계획 저장 및 작성 상태 백업
- Room을 이용해 여행 코스를 기기에 저장합니다.
- 작성 상태 변경을 관찰하고, 500ms debounce를 적용해 로컬에 임시 백업합니다.
- 신규 여행 계획 흐름이 시작될 때 저장된 백업에서 여행 코스·현재 작성 일차·선택 중인 날짜를 복원합니다.
  🛠 기술 스택
  구분	사용 기술
  Language	Kotlin
  UI	Jetpack Compose, Material 3
  상태 관리	ViewModel, Intent / State / Effect 기반 MVI 패턴
  비동기 처리	Coroutines, Flow, StateFlow, SharedFlow
  Navigation	Navigation Compose, Navigation Graph 범위 SharedViewModel
  Architecture	Clean Architecture 기반 계층 분리, Multi Module
  Dependency Injection	Hilt
  Local Data	Room
  Network	Retrofit, OkHttp, Gson
  Map / Location	Kakao Maps SDK, Kakao Local API, Google Play Services Location
  관광 정보	한국관광공사 관광데이터 API
  Test	JUnit, MockK, kotlinx-coroutines-test, MockWebServer, Android Instrumented Test
  Build	Gradle Kotlin DSL, Version Catalog, Convention Plugin


🏗 모듈 구성
UI, 상태 관리, 비즈니스 로직, 데이터 구현의 책임을 Gradle 모듈로 나누었습니다. Repository 인터페이스와 비즈니스 모델은 domain에 두고, 각 데이터 모듈에서 구현체를 제공합니다.
모듈	주요 역할
app	Compose 화면, Navigation 구성, 앱 진입점 및 의존성 조립
presentation	ViewModel, UI State / Intent / Effect, Presentation Model 및 Mapper
domain	UseCase, Repository 인터페이스, 비즈니스 모델
course-data	여행 코스 Repository 구현 및 데이터 모델
map-data	지도·위치·지역 Repository 구현 및 DataSource 인터페이스
map-remote	Kakao Local API 통신 및 응답 변환
tourdata-data	관광 정보 Repository 구현 및 DataSource 인터페이스
tourdata-remote	관광데이터 API 통신 및 응답 변환
toilet-data	화장실 Repository 구현 및 조회·필터링 로직
system-data	작성 상태 백업·복구 및 DB 초기화 상태 관련 Repository 구현
local	Room DB·DAO, 로컬 DataSource 구현, 위치 및 CSV 데이터 처리
data-resource	비동기 작업의 Loading / Success / Error 표현과 Flow 유틸리티
build-logic	Android·Kotlin·Compose·Hilt 등 공통 빌드 설정을 위한 Convention Plugin


🔍 주요 구현
1. 여행 계획 흐름에 맞춘 상태 공유
   여행 계획은 지역 선택, 날짜 선택, 일정 구성, 장소 검색 등 여러 화면에 걸쳐 만들어집니다.
   각 화면은 plan_graph의 BackStackEntry를 기준으로 생성한 PlanSharedViewModel을 공유합니다. 상태의 범위를 개별 화면이나 Activity 전체가 아닌 여행 계획 Navigation Graph에 맞추었습니다. 기존 여행의 지역·날짜 수정은 별도의 course_edit_graph로 구분합니다.
- [Navigation 구성](app/src/main/java/com/braveberry/tourdataproject/MainActivity.kt)
- [PlanSharedViewModel](presentation/src/main/java/com/tourdataproject/presentation/viewmodel/plan/PlanSharedViewModel.kt)
2. 사용자 입력·화면 상태·일회성 동작 분리
   여행 계획의 사용자 입력을 Intent로 전달하고, ViewModel에서 StateFlow의 상태를 갱신합니다. 화면 이동과 Toast 등 일회성 동작은 SharedFlow 기반 Effect로 분리했습니다.
- [State / Intent / Effect 정의](presentation/src/main/java/com/tourdataproject/presentation/viewmodel/plan/PlanSharedState.kt)
3. 로컬 백업과 완료된 여행 데이터 구분
   작성 중인 여행 상태는 백업 모델로 변환해 로컬에 보관합니다. 연속 입력에는 debounce(500ms)를 적용하고, ViewModel 초기화 시 백업을 조회합니다. 최종 저장 흐름에서는 여행 코스를 저장한 뒤 임시 백업 삭제를 수행합니다.
- [백업 Repository 구현](system-data/src/main/java/com/braveberry/system_data/impl/SystemRepositoryImpl.kt)
- [로컬 백업 DataSource](local/src/main/java/com/braveberry/local/impl/SystemDataSourceImpl.kt)
4. 조회·수정 작업의 비동기 상태 관리
   여행 코스를 다시 조회하면 이전 조회 Job을 취소합니다. 여행 날짜 수정 저장에는 진행 여부와 대상 코스 ID 검증을 적용하고, 해당 작업의 취소와 일반 오류를 구분해 처리합니다.
   🧪 테스트 구성
   Repository의 데이터 변환·호출 동작, 위치 및 접근성 계산, API 응답 처리, Room 데이터 처리를 위한 테스트를 포함합니다.
   테스트 영역	예시
   Repository	CourseRepositoryImplTest, MapRepositoryImplTest, ToiletRepositoryImplTest
   계산 로직	LocationParamCalculatorTest, FilterCalculatorTest, CalculateAccessibilityUseCaseTest
   API	KakaoMapApiTest, TourApiServiceTest, TourDataSourceImplTest
   로컬 DB	AppDatabaseTest, CourseDataSourceImplTest, CourseDatabaseTest


실제 외부 API를 호출하는 테스트는 API 키와 네트워크가 필요합니다. 실행 환경 설정 후 목적에 맞는 테스트를 선택해 실행합니다.
⚙️ 개발 환경 및 실행
환경
항목	저장소 설정
기본 브랜치	Dev
Kotlin	2.0.21
Android Gradle Plugin	8.13.2
Android SDK	compileSdk / targetSdk 36
최소 지원 버전	Android 12 · API 31
Java / Kotlin 바이트코드 타깃	11


Android Studio에서 프로젝트를 열고 Android SDK 36을 설치합니다. Gradle 실행용 JDK는 프로젝트의 Android Gradle Plugin과 호환되는 버전을 사용합니다. 바이트코드 타깃 11과 Gradle 실행용 JDK 버전은 서로 다른 설정입니다.
1. 저장소 클론
   git clone --branch Dev https://github.com/rlaalsgh988238/2026-.git
   cd 2026-
2. API 설정
   프로젝트 루트의 local.properties에 Android Studio가 생성한 sdk.dir을 유지하고 다음 값을 추가합니다.
   KAKAO_MAP_KEY=YOUR_KAKAO_NATIVE_APP_KEY
   KAKAO_API_KEY=YOUR_KAKAO_REST_API_KEY
   TOUR_API_KEY=YOUR_TOUR_API_KEY
   TOUR_BASE_URL=https://apis.data.go.kr/B551011/KorService2/
- KAKAO_MAP_KEY: Kakao Maps SDK 초기화에 사용합니다.
- KAKAO_API_KEY: Kakao Local API 요청에 사용합니다. 코드에서 KakaoAK 접두사를 붙이므로 키 값만 입력합니다.
- TOUR_API_KEY: 관광데이터 API 요청에 사용합니다.
- TOUR_BASE_URL: 관광데이터 API 기본 주소이며, 생략 시 위 주소를 사용합니다.
  지도 실행에는 Kakao Developers의 앱·Android 플랫폼 설정도 필요합니다. API 키와 local.properties는 저장소에 커밋하지 않습니다.
3. 현재 릴리스 서명 설정 확인
   현재 app/build.gradle.kts는 Gradle 구성 시 RELEASE_STORE_PASSWORD와 RELEASE_KEY_PASSWORD를 필수로 읽고, 개발자의 로컬 키스토어 경로를 참조합니다. 따라서 API 키만 추가하면 바로 실행되는 구조는 아닙니다.
- 로컬 디버그 실행: 릴리스 서명 설정을 선택적으로 구성하도록 조정하거나, 로컬 작업 사본에서 signingConfigs의 release 블록과 buildTypes.release의 서명 연결을 제외한 뒤 동기화합니다. 디버그 빌드에는 개발자의 릴리스 키가 필요하지 않습니다.
- 릴리스 빌드: 사용 권한이 있는 키스토어 경로와 별칭으로 설정을 변경하고, 위 두 비밀번호 속성을 local.properties에 추가합니다.
4. 빌드 및 실행
   설정을 마친 뒤 Android Studio에서 app의 debug 변형을 선택해 API 31 이상 기기 또는 에뮬레이터에서 실행합니다.
# macOS / Linux
chmod +x gradlew
./gradlew :app:assembleDebug
주변 화장실 조회에는 위치 권한과 기기의 위치 기능이 필요합니다. 외부 길찾기는 카카오맵 앱과 연동됩니다.
🔗 관련 링크
- Google Play
- 프로젝트 저장소
  기능 및 개발 환경 설명은 Dev 브랜치 기준입니다. 지도상의 장소 연결선은 방문 순서를 보여주며, 실제 도로 경로나 휠체어 통행 가능 경로를 검증하는 기능은 아닙니다. 편의시설 정보는 제공 데이터의 범위와 갱신 상태에 따라 달라질 수 있습니다.