package com.tourdataproject.presentation.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.splash.CheckDatabaseInitUseCase
import com.tourdataproject.presentation.utility.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkDatabaseInitUseCase: CheckDatabaseInitUseCase
) : ViewModel() {

    private val _isDatabaseReady = MutableStateFlow(false)
    val isDatabaseReady = _isDatabaseReady.asStateFlow()

    init {
        Log.d("TOUR_DATA_DEBUG", "4. SplashViewModel init & collect start")
        checkDatabaseInit()
    }

    private fun checkDatabaseInit() {
        viewModelScope.launch {
            checkDatabaseInitUseCase().collectLatest { resource ->
                when (resource) {
                    is DataResource.Success -> {
                        _isDatabaseReady.value = true
                    }
                    is DataResource.Loading -> {
                        _isDatabaseReady.value = false
                    }
                    is DataResource.Error -> {
                        // 에러 처리 로직 (필요시)
                        _isDatabaseReady.value = false
                    }
                }
            }
        }
    }
}