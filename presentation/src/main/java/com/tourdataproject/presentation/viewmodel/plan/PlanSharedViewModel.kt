package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.ViewModel
import com.tourdataproject.presentation.model.RegionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanSharedViewModel @Inject constructor() : ViewModel() {
    // 최종적으로 저장될 데이터 구조
    data class PlanData(
        val selectedRegion: RegionUiModel? = null,
        val selectedDate: LocalDate? = null
    )

    private val _planData = MutableStateFlow(PlanData())
    val planData = _planData.asStateFlow()

    fun updateRegion(region: RegionUiModel) {
        _planData.update { it.copy(selectedRegion = region) }
    }

    fun updateDate(date: LocalDate) {
        _planData.update { it.copy(selectedDate = date) }
    }
}