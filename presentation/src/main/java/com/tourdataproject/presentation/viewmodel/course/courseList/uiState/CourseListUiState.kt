package com.tourdataproject.presentation.viewmodel.course.courseList.uiState

import android.util.Log
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// 1. 전체 리스트 화면 상태
data class CourseListUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    // 🌟 원본 모델이 아니라 화면 그리기 전용 가벼운 모델을 리스트로 가집니다!
    val courses: List<CourseListItemState> = emptyList()
)

// 2. 리스트 카드 하나를 그리기 위한 전용 가벼운 모델
data class CourseListItemState(
    val courseId: String,
    val courseName: String,
    val datePeriod: String,
    val dDayText: String
)

// 3. 단발성 네비게이션 및 이벤트
sealed interface CourseListEffect {
    object NavigateToCreatePlan : CourseListEffect
    object NavigateToRestroomGuide : CourseListEffect
    data class NavigateToCourseDetail(val courseId: String) : CourseListEffect
    data class ShowToast(val message: String) : CourseListEffect
}

// ================= 여기서부터 ViewModel이 사용할 매퍼(조립 공정) =================

// 4. DB에서 가져온 원본 리스트(TravelCourseUiModel)를 UI State로 예쁘게 포장해주는 확장 함수
fun List<TravelCourseUiModel>.toCourseListState(): CourseListUiState {
    return try {
        if (this.isEmpty()) {
            return CourseListUiState(isLoading = false, courses = emptyList())
        }

        val mappedCourses = this.map { course ->
            CourseListItemState(
                courseId = course.courseId,
                courseName = course.courseName.ifEmpty { "이름 없는 코스" },
                datePeriod = course.datePeriod.ifEmpty { "날짜 미정" },
                dDayText = calculateDDayForList(course.rawStartDate) // 매핑할 때 D-Day 미리 계산!
            )
        }

        CourseListUiState(
            isLoading = false,
            isError = false,
            courses = mappedCourses
        )
    } catch (e: Exception) {
        Log.e("CrashCatch", "🚨 CourseList 매퍼에서 크래시 발생: ${e.message}", e)
        CourseListUiState(
            isLoading = false,
            isError = true,
            errorMessage = "코스 목록을 처리하는 중 문제가 발생했습니다."
        )
    }
}

// 5. D-Day 계산 로직 (이제 ViewModel 동네에서 책임집니다)
private fun calculateDDayForList(startDateMillis: Long): String {
    if (startDateMillis == 0L) return "D-Day 미정"
    return try {
        val today = LocalDate.now(ZoneId.systemDefault())
        val startDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val daysBetween = ChronoUnit.DAYS.between(today, startDate)

        when {
            daysBetween > 0 -> "D-$daysBetween"
            daysBetween == 0L -> "D-Day"
            else -> "D+${kotlin.math.abs(daysBetween)}"
        }
    } catch (e: Exception) {
        "D-Day 오류"
    }
}