package com.trend.now.data.api.response

data class BasicResponse<T>(val success: Boolean, val data: T)