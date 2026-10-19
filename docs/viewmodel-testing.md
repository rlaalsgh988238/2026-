> 이 문서는 이전 단계의 기록입니다. 현재 일정 편집 구조와 검증 방법은 [schedule-edit-architecture.md](schedule-edit-architecture.md)를 참고하세요.

# ViewModel 로컬 테스트 도입 — 첫 검토용 구현

## 작업 기준과 상태

- 기준 브랜치: `develope/plan`
- 기준 커밋: `8b23858d2caf58cf4f3ad452b4f603080a1d848c`
- 목표: 앱을 조작하지 않고 실제 ViewModel의 상태 전이와 비동기 출력을 반복 검증한다.
- 범위: 일정 편집과 지역 선택 ViewModel, 공통 테스트 도구.
- 작성한 테스트: 12개. **이 환경에서는 컴파일·테스트 통과를 확인하지 못했다.**
- 실행 시도: Gradle 8.14.5 다운로드 단계에서 `java.net.SocketException: Network is unreachable`로 종료됐다. Android SDK도 이 작업 환경에 설정되어 있지 않다.
- 앱 기능과 ViewModel의 동작은 수정하지 않았다. 공유 ViewModel의 책임 재배치는 다음 단계다.

## 먼저 읽을 파일

1. `presentation/src/test/java/com/tourdataproject/presentation/viewmodel/plan/scheduleEdit/ScheduleEditViewModelTest.kt`
   - 첫 번째 삭제 테스트에서 준비 → 입력 → State 검증을 읽는다.
   - 저장 테스트에서 수집 시작 → 입력 → `runCurrent()` → Effect 검증을 읽는다.
2. `presentation/src/test/java/com/tourdataproject/presentation/testing/MainDispatcherRule.kt`
3. `presentation/src/test/java/com/tourdataproject/presentation/viewmodel/plan/regionSelect/RegionSelectionViewModelTest.kt`
   - 가상 시간으로 300ms 검색 지연과 요청 취소를 검증한다.

## 설계 결정

### 실제 ViewModel과 실제 UseCase를 테스트한다

ViewModel을 Mock하면 검증할 로직까지 가짜가 되므로 실제 객체를 생성한다. 지역 선택에서는 실제 UseCase에 `FakeMapRepository`를 주입한다. 테스트가 네트워크·Room·API 키에 의존하지 않도록 외부 데이터 경계만 대체한다.

Fake의 사용하지 않는 메서드는 예외를 발생시킨다. 의도하지 않은 의존성 호출을 조용히 성공시키지 않기 위해서다. 로그만 기존 프로젝트의 MockK로 대체한다.

### MainDispatcherRule

`viewModelScope`가 사용하는 `Dispatchers.Main`을 `StandardTestDispatcher`로 교체하고 테스트 종료 후 복원한다. JUnit Rule 적용 이후인 `@Before`에서 ViewModel을 생성한다.

- `runCurrent()`: 현재 가상 시각에 예약된 작업을 실행한다.
- `advanceTimeBy(300)`: 가상 시각을 300ms 앞으로 이동한다.
- `advanceTimeBy()`의 목표 시각에 예약된 작업은 이어서 `runCurrent()`로 실행한다.
- `runTest`는 Rule이 설치한 Main Dispatcher의 스케줄러를 공유한다.
- 실제 `Thread.sleep()`이나 외부 네트워크 대기는 사용하지 않는다.

### State와 Effect는 검증 방법이 다르다

단순한 최종 State 검증은 `state.value`를 읽는다. 중간 Loading 상태를 검증할 때는 결과를 지연시켜 해당 시점의 값을 확인한다. 최종값만 확인한 테스트를 모든 중간 상태까지 검증한 것으로 설명하지 않는다.

SharedFlow Effect는 replay가 없으므로 입력 전에 수집을 시작한다. 예제는 `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler))`로 수집자를 즉시 시작한다. 수집자는 테스트 종료 시 자동 취소된다. Main은 Standard Dispatcher를 유지하므로 작업 실행 시점을 명시적으로 제어할 수 있다.

`ViewModelStore.clear()`를 `@After`에서 호출해 ViewModel의 수명을 종료한다. ViewModel 객체를 단순히 참조에서 제거하는 것만으로 `viewModelScope`가 정리되지는 않는다.

### Android 의존성은 필요한 경계만 대체한다

`PresentationLogRule`은 프로젝트의 `Log` 객체만 Mock한다. `unitTests.isReturnDefaultValues = true`로 모든 Android 호출에 기본값을 반환시키지 않는다. 그렇게 하면 테스트 대상이 새 Android API에 의존하게 돼도 놓칠 수 있기 때문이다.

Object Mock은 같은 JVM에서 공유되므로 이 테스트들을 메서드 단위로 병렬 실행하지 않는다. 테스트 종료 후 해당 객체만 `unmockkObject(Log)`로 복원한다.

### 빌드 변경은 최소화한다

- `presentation`에 기존 Version Catalog의 `kotlinx-coroutines-test`, `mockk`를 추가했다.
- `data-resource/FlowDataResource.kt`에서 사용하지 않는 `jdk.jfr.internal.OldObjectSample.emit` import를 제거했다. Flow의 `emit`과 관계없는 JDK 내부 API 의존이므로 JVM 모듈 접근 문제를 피하기 위한 정리다.
- 앱의 릴리스 서명 설정, API 키, SDK 버전은 바꾸지 않았다.

## 테스트 목록

| 대상 | 검증 내용 | 성격 |
| --- | --- | --- |
| 일정 편집 | 장소 삭제 후 나머지 장소의 데이터·숙소 유지 및 순번 재계산 | 기존 동작 보호 |
| 일정 편집 | 드래그 완료 후 순서·순번·장소 데이터 유지 | 기존 동작 보호 |
| 일정 편집 | 잘못된 드래그 인덱스로 상태가 변하지 않음 | 경계값 |
| 일정 편집 | 뒤로가기에서 저장 요청 없이 이동 Effect만 발생 | 출력 계약 |
| 일정 편집 | 저장 요청의 payload와 Effect 순서 | 현재 출력 계약, DB 성공 검증 아님 |
| 일정 편집 | OnInit 재전달 시 미저장 순서 변경이 덮어써짐 | 현재 문제 동작 기록 |
| 지역 선택 | 마지막 입력 300ms 후 마지막 검색어만 조회 | 가상 시간 |
| 지역 선택 | 진행 중인 이전 조회 취소와 새 결과 반영 | 취소 |
| 지역 선택 | Error 결과 이후 새 검색으로 회복 | 실패·회복 |
| 지역 선택 | Flow에서 예외 발생 시 로딩 해제와 입력 유지 | 예외 |
| 지역 선택 | 반영 완료 전 연속 클릭 시 조회·반영 요청 중복 방지 | 중복 실행 |
| 지역 선택 | 좌표 조회 실패 시 선택 유지, 재시도 성공 | 실패·재시도 |

`현재 동작 기록` 테스트가 통과해도 설계 문제가 해결된 것은 아니다. 정책을 변경할 때 이 테스트를 초안 보존을 기대하는 테스트로 변경하고 구현을 수정한다. 현재 Route가 실제로 OnInit을 재전달하는지까지 이 단위 테스트가 증명하는 것도 아니다.

## 로컬 실행

### 환경

- 프로젝트가 사용하는 AGP/Gradle과 호환되는 JDK. 현재 AGP 8.x는 JDK 17 이상이 필요하다. Java 바이트코드 타깃 11과 별개다.
- Android SDK 36과 프로젝트 빌드에 필요한 SDK 구성요소.
- `local.properties`의 올바른 `sdk.dir` 또는 정상적인 SDK 환경 설정.
- 최초 실행 시 Gradle/Maven 의존성을 받을 네트워크.

이 테스트에는 에뮬레이터, 지도 API 키, 실제 위치, Room 데이터가 필요하지 않다. Android 모듈을 컴파일하기 위한 SDK는 여전히 필요하다.

### 실행 명령

저장소 루트에서 실행한다. Windows는 `bash gradlew` 대신 `gradlew.bat`를 사용한다.

```bash
# 첫 단계: 일정 편집 테스트만 실행
bash gradlew --configure-on-demand :presentation:testDebugUnitTest \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.ScheduleEditViewModelTest'

# 다음 단계: 비동기 지역 선택 테스트
bash gradlew --configure-on-demand :presentation:testDebugUnitTest \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModelTest'

# presentation의 모든 로컬 테스트
bash gradlew --configure-on-demand :presentation:testDebugUnitTest
```

`--configure-on-demand`는 필요한 모듈 위주로 구성해 관련 없는 `app`의 기존 서명 속성 검사를 피하기 위한 옵션이다. 이 환경에서는 다운로드 단계에서 중단되어 이 프로젝트에서의 효과를 실행 검증하지 못했다. IDE 전체 Sync에서는 여전히 `app/build.gradle.kts`의 기존 릴리스 서명 속성 요구가 적용될 수 있다. 오류가 발생하면 기존 로컬 설정을 확인하고, 테스트를 위해 실제 서명 비밀번호를 공유하거나 가짜 값을 커밋하지 않는다.

Android Studio에서는 Gradle 동기화 후 테스트 클래스 왼쪽 실행 버튼을 사용할 수 있다.

성공적으로 실행되면 결과는 다음 위치에서 확인한다.

```text
presentation/build/reports/tests/testDebugUnitTest/index.html
presentation/build/test-results/testDebugUnitTest/
```

## 첫 실행에서 확인할 순서

1. Gradle 다운로드/의존성 오류인지 확인한다. 이 단계의 실패는 테스트 실패가 아니다.
2. 컴파일 오류인지 확인한다. 본 변경은 작성 환경에서 컴파일 검증을 완료하지 못했다.
3. 테스트 assertion 실패라면 기대값과 실제값을 비교한다. 통과만을 위해 기대값을 실제값에 맞추지 않는다.
4. `Method ... not mocked`라면 필요한 Android 경계를 확인한다. 전체 기본값 반환 옵션으로 숨기지 않는다.
5. SharedFlow 이벤트가 보이지 않으면 이벤트 발생 전에 수집을 시작했는지 확인한다.

## 이 단계가 보장하지 않는 것

- Compose Route의 LaunchedEffect, Navigation, 실제 생명주기와의 연결.
- 공유 ViewModel과 개별 ViewModel 사이의 실제 상태 반영 성공.
- Room 저장, 실제 API 호출, 프로세스 종료 후 복원.
- 지도 렌더링·제스처·레이아웃.

## 다음 단계

1. 위 테스트를 로컬에서 실행하고 통과/실패 결과를 남긴다.
2. 일정 편집의 임시 상태·확정·취소 정책을 결정한다.
3. 재초기화 시 초안 보존을 기대하는 테스트를 먼저 작성/변경한다.
4. 공유 ViewModel을 유지하면서 편집 결과 반영 경계를 개선한다.
5. 화면 연결과 숙소의 여러 일차 삭제 정책은 별도 통합 검증으로 확인한다.

## 포트폴리오에서의 위치

환경 설정만으로 큰 설계 성과를 주장하지 않는다. 로컬 실행을 확인한 뒤, 상태·Effect·가상 시간·취소·실패 시나리오 중 실제 검증한 항목을 설명한다. 이후 리팩터링에서 이 테스트가 어떤 회귀를 막았는지 연결하면 하나의 기술 사례가 된다. 아직 실행 전이므로 현재는 테스트 코드 작성 및 환경 구성 단계로 기록한다.
