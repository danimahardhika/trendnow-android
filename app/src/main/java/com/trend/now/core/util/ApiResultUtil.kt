package com.trend.now.core.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trend.now.core.network.ApiResult
import com.trend.now.core.ui.state.UiState
import com.trend.now.data.api.response.ErrorResponse
import retrofit2.Response

object ApiResultUtil {
    fun <T> toApiResultError(gson: Gson, response: Response<T>): ApiResult.Error {
        return response.errorBody()?.string()?.let { errorBody ->
            val error = gson.fromJson<ErrorResponse>(
                errorBody,
                object : TypeToken<ErrorResponse>() {}.type
            )
            ApiResult.Error(error.code, error.message)
        } ?: run {
            ApiResult.Error(response.code(), response.message())
        }
    }
}

fun <T> ApiResult<T>.toUiState(): UiState<T> {
    return when (this) {
        is ApiResult.Success -> UiState.Success(this.data)
        is ApiResult.Error -> UiState.Error(this.code, this.message)
    }
}