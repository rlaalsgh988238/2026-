# 일정 편집: 원본 스냅샷과 편집 결과로 역할 분리

## 기존 수정과의 차이
앞선 수정은 숙소 삭제를 저장 시점으로 미루고 `initialized`로 반복 초기화를 막았다.
기존 16개 테스트와 사용자의 앱 재현 확인을 통해 해당 버그 수정은 검증됐다.
이번에는 초기화 반복을 요청하는 연결 자체를 없앤다.

- OnInit과 initialized 삭제.
- Route의 공유 dayPlans 관찰 → OnInit 재전달 삭제.
- 화면 UI가 받던 sharedState, onSharedIntent 삭제.
- ScheduleEditViewModel의 공유 ViewModel/Intent 의존 없음.
- 원본은 생성자에 한 번 주입하고, 편집 결과는 ScheduleEditResult로 반환.

## 역할과 데이터 흐름
| 구성 요소 | 책임 |
| --- | --- |
| MainActivity의 navigation destination | 날짜 인자 해석, 공유 원본 선택, 확정 콜백 연결 |
| ScheduleEditRoute | ViewModel 조회/생성, Effect 처리와 확정 응답 전달 |
| ScheduleEditViewModel | 한 화면 진입의 초안, 삭제·정렬, 저장 요청과 응답 상태 |
| ScheduleEditScreen | 편집 상태 렌더링과 사용자 입력 전달 |
| PlanSharedViewModel | 공유 계획 소유, 편집 결과 유효성 확인 및 원자적 반영 |

원본 → ViewModel 생성 → 초안 편집 → SaveRequested(result) → onCommit(result)
→ 공유 상태 반영 여부 반환 → OnSaveCompleted → 안내/뒤로가기.
취소 시에는 결과를 공유 상태에 전달하지 않는다.

## 왜 Factory인가
이 ViewModel은 주입할 Repository가 없고 생성 시점의 날짜 스냅샷만 필요하다.
이번 화면만 Hilt 자동 생성에서 명시적 Factory로 바꿨다. 다른 ViewModel의 Hilt 구성은 유지한다.
Compose의 viewModel()이 navigation entry의 ViewModelStore를 사용한다.
같은 entry/key에서는 Factory가 다시 전달되어도 기존 ViewModel을 반환한다.
따라서 원본 변경/리컴포지션을 편집 상태 초기화 요청으로 번역할 필요가 없다.
일반 객체 remember로 ViewModel을 생성하는 방식이 아니므로 화면 회전에도 ViewModel 수명을 유지한다.
실제 화면 회전과 뒤로가기 후 재진입은 기기에서 확인해야 한다.

## 저장의 의미
commitScheduleEdit는 DB 저장이 아닌 공유 계획에 대한 확정이다.
일정 목록과 삭제 숙소 ID를 한 번에 반영하고 Boolean으로 수락 여부를 반환한다.
대상 날짜가 사라졌으면 false를 반환하며 다른 날짜에도 변경을 적용하지 않는다.
기존 동일 숙소 ID의 여러 날짜 삭제 정책은 유지한다.
편집 완료는 같은 날짜 목록에 반영하고, 다른 날짜 일정과 여행 이름 등은 최신 공유 값을 유지한다.
저장 응답 대기 중에는 중복 저장과 편집 입력을 받지 않는다.

## 검증 범위
작성된 테스트는 일정 편집 단위 9개, 두 ViewModel 연결 6개, 기존 지역 선택 6개로 총 21개다.
- 실제 Factory/ViewModelProvider로 동일 owner/key에서 초안이 유지되는지 확인.
- 초기 목록의 외부 변경이 초안에 영향을 주지 않는지 확인.
- 성공 응답 전에는 성공 안내/이동이 없는지 확인.
- 거절 시 초안 유지 및 재시도, 중복 저장 억제 확인.
- 저장/취소/숙소 삭제, 공유 계획의 다른 항목 갱신 보존 확인.
- 대상 날짜가 사라지면 공유 상태 변경 없이 거절하는지 확인.

이번 구조 변경은 작성 환경의 Gradle 다운로드 네트워크 차단으로 컴파일/실행 검증하지 못했다.
이전 버전의 16개 통과와 이번 21개 통과를 혼동하지 않는다.
이 테스트는 실제 Compose/Navigation 실행 테스트가 아니다.

## 적용 및 실행
앞선 두 패치까지 적용하고 16개 테스트와 앱 수정 확인을 마친 코드에 추가 적용한다.

```bash
git apply --check schedule-edit-boundaries.patch
git apply schedule-edit-boundaries.patch
bash gradlew \
  -Dorg.gradle.java.home="/opt/homebrew/Cellar/openjdk@21/21.0.12/libexec/openjdk.jdk/Contents/Home" \
  --configure-on-demand :presentation:testDebugUnitTest \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.*' \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModelTest'
```

Android Studio에서 app도 빌드하고 다음을 확인한다.
1. 순서 변경 → 숙소 삭제 → 저장 → 재진입: 변경 유지.
2. 순서 변경 → 숙소 삭제 → 뒤로가기 → 재진입: 원본 유지.
3. 편집 도중 화면 회전: 초안 유지.

## 남은 범위
프로세스 종료 후 미저장 초안 복구는 추가하지 않았다. SavedStateHandle은 이전에도 날짜 인자에만 사용했다.
동일 날짜를 다른 곳에서 동시에 편집할 때의 버전 충돌/병합 정책도 추가하지 않았다.
Effect 전달 방식과 다른 화면의 공유 상태 접근은 기존 방식이므로 전체 앱 구조를 해결했다고 주장하지 않는다.
공유 ViewModel에서 유스케이스로 정책을 더 추출하는 작업은 여러 호출부에서 실제 재사용이 필요할 때 별도로 판단한다.
