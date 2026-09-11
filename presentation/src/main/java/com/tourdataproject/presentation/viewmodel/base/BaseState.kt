package com.tourdataproject.presentation.viewmodel.base

interface BaseState<Self> {
    val entryPoint: String?
    val purpose: String?
    fun setEntryPoint(entryPoint: String?): Self
    fun setPurpose(purpose: String?): Self
}