package com.trend.now.core.ui.state

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val code: Int, val message: String) : UiState<Nothing>()
}