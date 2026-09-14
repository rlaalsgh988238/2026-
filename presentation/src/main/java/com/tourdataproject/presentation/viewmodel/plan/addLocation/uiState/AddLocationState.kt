package com.tourdataproject.presentation.viewmodel.plan.addLocation.uiState

import com.tourdataproject.presentation.utility.ScreenPurpose
import com.tourdataproject.presentation.viewmodel.base.BaseState

data class AddLocationState(
    override val entryPoint: String? = null,
    override val purpose: String? = null
) : BaseState<AddLocationState> {
    val viewMode: AddLocationViewMode = when (purpose) {
        ScreenPurpose.ADD_SCHEDULE -> AddLocationViewMode.AddScheduleMode
        ScreenPurpose.ADD_STAY -> AddLocationViewMode.AddStayMode
        else -> AddLocationViewMode.AddScheduleMode
    }

    val title: String = when (viewMode) {
        AddLocationViewMode.AddScheduleMode -> "장소 추가"
        AddLocationViewMode.AddStayMode -> "숙소 검색"
    }

    override fun setEntryPoint(entryPoint: String?): AddLocationState =
        this.copy(
            entryPoint = entryPoint
        )

    override fun setPurpose(purpose: String?): AddLocationState =
        this.copy(
            purpose = purpose
        )
}


sealed class AddLocationIntent{
    object OnGetEntry: AddLocationIntent()
}

sealed class AddLocationViewMode {
    object AddScheduleMode : AddLocationViewMode()
    object AddStayMode: AddLocationViewMode()
}