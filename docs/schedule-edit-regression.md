> 이 문서는 이전 단계의 기록입니다. 현재 일정 편집 구조와 검증 방법은 [schedule-edit-architecture.md](schedule-edit-architecture.md)를 참고하세요.

# 일정 편집 초안 유실 수정

## 재현과 원인
사용자가 앱에서 A/B/C를 B/C/A로 바꾼 뒤 저장 전에 숙소를 삭제하면 A가 위로 복원되는 현상을 확인했다.
일정은 편집 ViewModel의 초안에만 반영하지만 숙소 삭제는 공유 ViewModel에 즉시 반영했다.
Route가 공유 dayPlans 변경을 감지해 OnInit을 다시 전달하면서 초안을 덮어썼다.

## 수정 계약
- 편집 ViewModel은 화면 진입 시 한 번만 원본을 받아 편집 세션의 초안을 소유한다.
- 일정 변경과 숙소 삭제는 저장 전까지 공유 상태에 반영하지 않는다.
- 저장 Effect에는 일정 목록과 삭제할 숙소 ID를 함께 담는다.
- Route는 toSharedIntent()로 단일 확정 명령을 전달한다.
- 공유 ViewModel은 한 번의 StateFlow.update에서 일정과 숙소 삭제를 반영한다.
- 기존 정책인 동일 숙소 ID의 모든 날짜 삭제를 유지한다. 당일만 삭제하도록 변경한 것이 아니다.
- 취소 시 공유 상태를 변경하지 않는다. 뒤로가기 후 새 화면 진입은 새 편집 ViewModel을 사용하는 기존 navigation scope를 전제로 한다.
- 다른 화면에서 동시에 원본을 편집하는 충돌 병합이나 프로세스 종료 후 초안 복구는 이번 범위가 아니다.

## 검증
기존 재초기화 테스트를 초안 보존 검증으로 변경했다.
ScheduleEditCommitTest의 4개 테스트는 실제 두 ViewModel과 Route에서 사용하는 명령 변환을 연결한다.
공유 dayPlans를 관찰해 OnInit을 재전달하는 연결을 테스트 코드에서 구성한다.
Compose의 LaunchedEffect나 실제 navigation을 실행하는 UI 테스트는 아니다.

1. 순서 변경 후 숙소 삭제: 초안 순서 유지, 공유 상태 불변, Effect 없음.
2. 저장: 일정과 숙소 삭제를 함께 반영, 다른 일정 보존, 같은 숙소 ID의 다중 날짜 삭제 정책 유지.
3. 취소 후 새 편집 ViewModel 생성: 원래 순서와 숙소 유지.
4. 일정만 저장: 모든 숙소 유지.

기존 테스트 12개는 사용자 환경에서 통과 확인했다. 이번 수정 후 예상 총수는 16개다.
작성 환경에서는 Gradle 배포 파일 다운로드가 Network is unreachable로 실패하여 이번 변경의 컴파일/실행을 검증하지 못했다.

## 로컬 확인
앞선 테스트 환경 패치가 적용된 코드에 schedule-edit-draft-fix.patch를 적용한다.

```bash
git apply --check schedule-edit-draft-fix.patch
git apply schedule-edit-draft-fix.patch
bash gradlew \
  -Dorg.gradle.java.home="/opt/homebrew/Cellar/openjdk@21/21.0.12/libexec/openjdk.jdk/Contents/Home" \
  --configure-on-demand :presentation:testDebugUnitTest \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.*' \
  --tests 'com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModelTest'
```

앱도 Android Studio에서 빌드한다. presentation 테스트만으로 app 모듈 컴파일은 검증되지 않는다.
실기기에서 순서 변경 → 숙소 삭제 → 순서 유지 → 저장 → 재진입을 확인한다.
별도 세션에서 순서 변경 → 숙소 삭제 → 취소 → 재진입 시 원본 유지도 확인한다.
같은 숙소가 여러 날짜에 있으면 저장 전 모두 유지되고 저장 후 같은 ID가 함께 제거되는지 확인한다.
